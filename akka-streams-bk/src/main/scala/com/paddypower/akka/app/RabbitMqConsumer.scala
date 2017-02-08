package com.paddypower.akka.app

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.ActorMaterializer
import com.paddypower.akka.actors.fsm.FsmSelectionAgg
import com.paddypower.akka.actors.fsm.FsmSelectionAgg.Done
import com.paddypower.akka.config.DefaultCassandraConfig
import com.paddypower.akka.domain.Model.{Bet, SelectionBet}
import io.scalac.amqp.{Address, Connection, ConnectionSettings, Message}

import scala.collection.SortedSet
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

object RabbitMqConsumer extends App with DefaultCassandraConfig {

  def start = {
    implicit val system = ActorSystem("ClusterSystem")
    implicit val materializer = ActorMaterializer()

    val Journal
    val selectionAggRegion: ActorRef = ClusterSharding(system).start(
      typeName = FsmSelectionAgg.shardName,
      entityProps = FsmSelectionAgg.props,
      settings = ClusterShardingSettings(system),
      extractEntityId = FsmSelectionAgg.extractEntityId,
      extractShardId = FsmSelectionAgg.extractShardId)


    val connection = Connection(Settings.connectionSettings)
    val queue = connection.consume("streams-playground")
    Source.fromPublisher(queue).map(_.message).map(Util.translate).runWith(Sink.actorRef(selectionAggRegion, onCompleteMessage = Done))
  }

 start

}
