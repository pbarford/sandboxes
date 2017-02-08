package test.storm

import org.apache.storm.{Config, LocalCluster}
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.tuple.Fields

object TestStorm extends App {

  val builder = new TopologyBuilder()

  builder.setSpout("test", new TestSpout())
  builder.setBolt("count", new WordCount()).fieldsGrouping("test", new Fields("word"))
  builder.setBolt("print", new PrinterBolt()).shuffleGrouping("count")

  val config = new Config
  config.setDebug(true)
  config.setMaxTaskParallelism(3)

  val cluster:LocalCluster = new LocalCluster()
  cluster.submitTopology("word-count", config, builder.createTopology())

  Thread.sleep(60000)
  cluster.shutdown()
}