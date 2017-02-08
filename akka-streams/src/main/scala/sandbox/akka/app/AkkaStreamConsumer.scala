package sandbox.akka.app

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.ActorMaterializer
import io.scalac.amqp._
import sandbox.akka.actors.fsm.FsmBetAggregator
import sandbox.akka.config.{DefaultCassandraConfig, RabbitConfig}
import sandbox.akka.domain.Model.{Bet, SelectionBet}
import sandbox.akka.domain.ModelUtil
import sandbox.akka.persistence.Journal

import scala.collection.immutable.Seq
import scala.concurrent.duration.{Duration, _}

object Settings {
  def connectionSettings:ConnectionSettings = {
    ConnectionSettings(
      addresses         = Seq(Address(host = "192.168.99.100", port = 5672)),
      virtualHost       = "/",
      username          = "guest",
      password          = "guest",
      heartbeat         = None,
      timeout           = Duration.Inf,
      automaticRecovery = false,
      recoveryInterval  = 5.seconds,
      ssl               = None)
  }
}

object Util {
  def translate: Message => Option[List[SelectionBet]] = parseBet andThen toSelectionBets

  def parseBet : Message => Bet = { m =>
    import org.json4s._
    import org.json4s.native.JsonMethods._
    implicit val formats = DefaultFormats
    val bet = parse(new String(m.body.toArray)).extract[Bet]
    bet
  }

  def toSelectionBets : Bet => Option[List[SelectionBet]] = { b =>
    ModelUtil.splitBet(b)
  }
}

object AkkaStreamConsumer extends App with DefaultCassandraConfig with RabbitConfig {

  def start = {
    implicit val system = ActorSystem("ClusterSystem")
    implicit val materializer = ActorMaterializer()

    val journal = new Journal(session)
    val betAggregatorRegion: ActorRef = ClusterSharding(system).start(
      typeName = FsmBetAggregator.shardName,
      entityProps = FsmBetAggregator.props(journal),
      settings = ClusterShardingSettings(system),
      extractEntityId = FsmBetAggregator.extractEntityId,
      extractShardId = FsmBetAggregator.extractShardId)


    val journalBets = Flow[Option[List[SelectionBet]]].map {
      case Some(bets) => println("journalling")
                         journal.persistBets(bets)
                         bets
      case _ => List.empty[SelectionBet]
    }

    val connection = Connection(Settings.connectionSettings)
    connection.queueDeclare(Queue(queueName))
    val queue = connection.consume(queueName)
    Source.fromPublisher(queue).map(_.message).map(Util.translate).via(journalBets).runForeach(process(betAggregatorRegion))
  }

  def process(betAggregatorRegion: ActorRef) : List[SelectionBet] => Unit = { bets =>
    bets.foreach(betAggregatorRegion ! _)
  }

 start

}
