package test.storm.bet

import org.apache.storm.{Config, LocalCluster}
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.tuple.Fields
import test.storm.PrinterBolt

object TestBet extends App {

  val builder = new TopologyBuilder()

  builder.setSpout("bet_spout", new BetSpout())
  builder.setBolt("selection_agg", new SelectionAgg(),10).fieldsGrouping("bet_spout", "bet", new Fields("selection"))
  builder.setBolt("bet_print", new PrinterBolt()).fieldsGrouping("selection_agg", "agg", new Fields("selection"))

  val config = new Config
  config.setDebug(true)
  config.setMaxTaskParallelism(3)

  val cluster:LocalCluster = new LocalCluster()
  cluster.submitTopology("bet_aggregator", config, builder.createTopology())

  Thread.sleep(60000)
  cluster.shutdown()
}