package sandbox.akka.app

import akka.actor.ActorSystem
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import sandbox.akka.actors.BetConsumer
import sandbox.akka.actors.fsm.FsmBetAggregator
import sandbox.akka.config.DefaultCassandraConfig
import sandbox.akka.persistence.Journal

object ActorBasedConsumer extends App with DefaultCassandraConfig {

  def start = {
    implicit val system = ActorSystem("ClusterSystem")

    val journal = new Journal(session)
    ClusterSharding(system).start(
      typeName = FsmBetAggregator.shardName,
      entityProps = FsmBetAggregator.props(journal),
      settings = ClusterShardingSettings(system),
      extractEntityId = FsmBetAggregator.extractEntityId,
      extractShardId = FsmBetAggregator.extractShardId)

    system.actorOf(BetConsumer.props(journal))
  }

  start
}
