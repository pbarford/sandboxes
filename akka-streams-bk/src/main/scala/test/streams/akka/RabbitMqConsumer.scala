package test.streams.akka

import akka.actor.{ActorIdentity, ActorPath, ActorRef, ActorSystem, Identify, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.pattern.ask
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import io.scalac.amqp.{Address, Connection, ConnectionSettings, Message}
import test.streams.akka.SelectionAgg.Done
import test.streams.akka.fsm.FsmSelectionAgg

import scala.collection.SortedSet
import scala.collection.immutable.Seq
import scala.concurrent.duration.{Duration, _}

object RabbitMqConsumer {

  case class Bet(betID:String, betType:String, betSource:BetSource, legs:Seq[Leg], totalStake:Double, betCurrency:String)
  case class BetSource (sourceBetPlatform:String, channel:String)
  case class Leg(legNumber:Int, selection:Selection)
  case class Selection(selectionID:String, selectionName:String, price:Price, platformReference:PlatformReference) extends Ordered[Selection] {
    override def compare(that: Selection): Int = {
      if(this.selectionID.toInt < that.selectionID.toInt)
        -1
      else if(this.selectionID.toInt > that.selectionID.toInt)
        1
      else
        0
    }

    override def toString: String = selectionID
  }

  case class Price(decimalPrice:Double, priceDescription:String)
  case class PlatformReference(platformReferenceSubclassID:String,platformReferenceEventTypeID:String, platformReferenceEventID:String, platformReferenceMarketID:String, platformReferenceSelectionID:String)

  case class SelectionBet(betID:String, totalStake:Double, betCurrency:String, betType:String, selections: SortedSet[Selection]) {
    val key:String = selections.foldLeft("multi")((a, s) => s"$a:${s.selectionID}")
    val multi:Boolean = selections.size > 1
    val betPayout =  selections.foldLeft(totalStake)((a,s) => a * ( s.price.decimalPrice))
    val betLiability =  betPayout - totalStake

    def splitBet: Option[List[SelectionBet]] = {
      betType match {
        case "SINGLE" => Some(List(this))
        case "DOUBLE" => double
        case "STRAIGHT TRICAST" => triple
        case "TRIXIE" => trixie
        case "YANKEE" => yankee
        case "LUCKY 15" => lucky15
        case _ => Some(List(this))
      }
    }

    def yankee : Option[List[SelectionBet]] = {
      if(selections.size != 4) {
        None
      } else {
        val d:List[SelectionBet] = doubles.map(ds => SelectionBet(betID, totalStake / 11, betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
        val t:List[SelectionBet] = triples.map(ts => SelectionBet(betID, totalStake / 11, betCurrency, "TRIPLE", SortedSet(ts._1, ts._2, ts._3)))
        Some(d ++ t ++ List(SelectionBet(betID, totalStake / 11, betCurrency, "QUAD", selections)))
      }
    }

    def lucky15 : Option[List[SelectionBet]] = {
      if(selections.size != 4) {
        None
      } else {
        val s: List[SelectionBet] = selections.map(s => SelectionBet(betID, totalStake / 15, betCurrency, "SINGLE", SortedSet(s))).toList
        val d: List[SelectionBet] = doubles.map(ds => SelectionBet(betID, totalStake / 15, betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
        val t: List[SelectionBet] = triples.map(ts => SelectionBet(betID, totalStake / 15, betCurrency, "TRIPLE", SortedSet(ts._1, ts._2, ts._3)))
        Some(s ++ d ++ t ++ List(SelectionBet(betID, totalStake / 15, betCurrency, "QUAD", selections)))
      }
    }

    def trixie : Option[List[SelectionBet]] = {
      if(selections.size != 3) {
        None
      } else {
        val d: List[SelectionBet] = doubles.map(ds => SelectionBet(betID, totalStake / 15, betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
        Some(d ++ List(SelectionBet(betID, totalStake / 15, betCurrency, "TRIPLE", selections)))
      }
    }

    def triple : Option[List[SelectionBet]] = {
      if(selections.size != 3) {
        None
      } else {
        val t: List[SelectionBet] = triples.map(ts => SelectionBet(betID, totalStake / 15, betCurrency, "TRIPLE", SortedSet(ts._1, ts._2, ts._3)))
        Some(t)
      }
    }

    def double : Option[List[SelectionBet]] = {
      if(selections.size != 2) {
        None
      } else {
        val d:List[SelectionBet] = doubles.map(ds => SelectionBet(betID, totalStake / 11, betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
        Some(d)
      }
    }

    private def doubles:List[(Selection, Selection)] = {
      def inner(c: List[(Selection, Selection)], xs: Seq[Selection]):List[(Selection, Selection)] = xs match {
        case x :: xs =>
          inner(c ++ xs.map(y => (x, y)), xs)
        case Nil => c
      }
      inner(List.empty, selections.toList)
    }

    private def triples:List[(Selection, Selection, Selection)] = {
      def step(acc: List[(Selection, Selection,Selection)], head:Selection, tail: Seq[Selection]):List[(Selection, Selection, Selection)] = {
        tail match {
          case x::xs => step(acc ++ xs.map(y => (head, x, y)), head, xs)
          case _ => acc
        }
      }
      def inner(acc: List[(Selection, Selection,Selection)], list: Seq[Selection]):List[(Selection, Selection, Selection)] = list match {
        case x :: xs => inner(acc ++ step(List.empty, x, xs), xs)
        case _ => acc
      }
      inner(List.empty, selections.toList)
    }

    override def toString: String = s"betID [${betID}] key [${key}], stake [$totalStake] multi [${multi}] betPayout [${betPayout}] betLiability [${betLiability}] betType[$betType]"
  }

  val referenceSettings =
    ConnectionSettings(
      addresses         = Seq(Address(host = "192.168.99.100", port = 5672)),
      virtualHost       = "/",
      username          = "guest",
      password          = "guest",
      heartbeat         = None,
      timeout           = Duration.Inf,
      automaticRecovery = false,
      recoveryInterval  = 5.seconds,
      ssl       = None)

  def consume(implicit materializer: Materializer, selectionAggRegion:ActorRef) = {
    val connection = Connection(referenceSettings)
    val queue = connection.consume("streams-playground")
    //Source.fromPublisher(queue).map(_.message).runForeach(x => println(new String(x.body.toArray)))
    //Source.fromPublisher(queue).map(_.message).map(convert).runForeach(b => println(b))
    //Source.fromPublisher(queue).map(_.message).map(convert).map(BetMsg).runWith(Sink.actorSubscriber(SelectionAggWorker.props))

    Source.fromPublisher(queue).map(_.message).map(translate).runWith(Sink.actorRef(selectionAggRegion, onCompleteMessage = Done))

    //Source.fromPublisher(queue).map(_.message).map(translate).runWith(Sink.foreach(b => b.map( s => selectionAggRegion ! s)))

    //Source.fromPublisher(queue).map(_.message).map(convert).map(selections).runWith(Sink.foreach(b => b.map( s => selectionAggRegion ! s)))
    //Source.fromPublisher(queue).map(_.message).map(convert).map(selections).runForeach(_.map(_ => selectionAggRegion ! _))
    //val s: Source[List[SelectionBet], NotUsed] = Source.fromPublisher(queue).map(_.message).map(convert).map(selections)
    //s.runForeach(s => s.map(selectionAggRegion ! _))

    //Source.fromPublisher(queue).map(_.message).map(convert).map(BetMsg).runWith(Sink.actorRefWithAck(SelectionAggWorker.props, Init, Done, Complete))
  }

  def translate: Message => SelectionBet = parseBet andThen toSelectionBet

  def parseBet : Message => Bet = { m =>
    import org.json4s._
    import org.json4s.native.JsonMethods._
    implicit val formats = DefaultFormats
    parse(new String(m.body.toArray)).extract[Bet]
  }

  def toSelectionBet : Bet => SelectionBet = { b =>

    SelectionBet(b.betID, b.totalStake, b.betCurrency, b.betType, b.legs.map(l => l.selection).to[SortedSet])
  }

}

object RabbitApp extends App {
  implicit val system = ActorSystem("ClusterSystem")
  implicit val materializer = ActorMaterializer()

  //startupSharedJournal(system, startStore = true, path = ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store"))

  implicit val selectionAggRegion: ActorRef = ClusterSharding(system).start(
    typeName = FsmSelectionAgg.shardName,
    entityProps = FsmSelectionAgg.props,
    settings = ClusterShardingSettings(system),
    extractEntityId = FsmSelectionAgg.extractEntityId,
    extractShardId = FsmSelectionAgg.extractShardId)

  RabbitMqConsumer.consume

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    // Start the shared journal one one node (don't crash this SPOF)
    // This will not be needed with a distributed journal
    if (startStore) system.actorOf(Props[SharedLeveldbStore], "store")
    // register the shared journal
    import system.dispatcher
    implicit val timeout = Timeout(15.seconds)
    val f = system.actorSelection(path) ? Identify(None)
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.terminate()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.terminate()
    }
  }
}