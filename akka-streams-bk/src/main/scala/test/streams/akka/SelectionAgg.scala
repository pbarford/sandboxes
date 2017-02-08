package test.streams.akka

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.Passivate
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy, RequestStrategy}
import test.streams.akka.RabbitMqConsumer.{Bet, SelectionBet}
import test.streams.akka.SelectionAgg.{BetMsg, Done, PersistedBet, PersistedSelectionBet}

import scalaz.{-\/, \/, \/-}

object SelectionAgg {
  //case class Msg(b:Bet, replyTo:ActorRef)
  case class BetMsg(bet:Bet) {
    def apply(b:Bet) = BetMsg(b)
  }
  case class Done(betId:String)
  case object Complete
  case object Init
  case class PersistedBet(bet:Bet, sender:ActorRef)
  case class PersistedSelectionBet(selectionBet:SelectionBet, sender:ActorRef)

  val numberOfShards = 100
  val shardName = "SelectionAgg"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg:BetMsg => (msg.bet.betID, msg)
    case msg:SelectionBet => (msg.key, msg)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case msg:BetMsg => math.abs(msg.bet.betID.hashCode % numberOfShards).toString
    case msg:SelectionBet => math.abs(msg.key.hashCode % numberOfShards).toString
  }

  def props:Props = Props(new SelectionAgg)
}

object SelectionAggWorker {
  def props:Props = Props(new SelectionAggWorker)
}

class SelectionAggWorker  extends ActorSubscriber {

  val MaxQueueSize = 10
  //var queue = Map.empty[String, ActorRef]
  var queue = Map.empty[String, Bet]

  val router = {
    val routees = Vector.fill(3) {
      ActorRefRoutee(context.actorOf(Props[SelectionAgg]))
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override protected def requestStrategy: RequestStrategy = new MaxInFlightRequestStrategy(max = MaxQueueSize) {
    override def inFlightInternally: Int = queue.size
  }

  override def receive: Receive = {
    case OnNext(BetMsg(b)) =>
      queue += (b.betID -> b)
      router.route(b, self)
    case Done(betId)=>
      //queue(betId) ! "ack"
      println(s"done -> ${queue(betId)}")
      queue -= betId
  }
}

class SelectionAgg extends Actor {

  val journal = new Journal

  override def preStart(): Unit = {
    println("started")
  }

  override def postStop(): Unit = {
    println("stopped")
  }

  override def receive: Receive = {
    case b:Bet =>
      println(s"SelectionAgg:Bet --> $b")
      sender() ! Done(b.betID)
    case m:BetMsg =>
      println(s"SelectionAgg:BetMsg --> $m")
      //sender() ! Done(m.bet.betID)
      //context.parent ! Passivate(stopMessage = PoisonPill)
      //journal.bet(persisted(sender() ! Done(m.bet.betID))).attemptRun
      //journal.bet2(persistHandler(sender() ! Done(m.bet.betID)))
      journal.bet2(persistHandler(self ! PersistedBet(m.bet, sender())))

    case s:SelectionBet =>
      println(s"SelectionAgg:SelectionBet --> $s")
      //sender() ! Done(s.betID)
      //context.parent ! Passivate(stopMessage = PoisonPill)
      //journal.bet(persisted(sender() ! Done(s.betID))).attemptRun
      //journal.bet2(persistHandler(sender() ! Done(s.betID)))
      journal.bet2(persistHandler(self ! PersistedSelectionBet(s, sender())))

    case p:PersistedBet=>
      println(s"SelectionAgg:PersistedBet --> ${p.bet}")
      p.sender ! Done(p.bet.betID)

    case p:PersistedSelectionBet=>
      println(s"SelectionAgg:PersistedSelectionBet --> ${p.selectionBet}")
      p.sender ! Done(p.selectionBet.betID)
  }

  /*
  def persisted(confirmation: => Unit):Unit = {
    println("confirming")
    confirmation
    context.parent ! Passivate(stopMessage = PoisonPill)
  }
  */

  def persistHandler(postPersistAction: => Unit): (Throwable \/ Unit) => Unit = {
    case -\/(e) => println(e)
    case \/-(()) => postPersistAction
                    println("persisted")
  }
}
