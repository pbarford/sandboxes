package storm

import org.apache.storm.topology.base.BaseBasicBolt
import org.apache.storm.topology.{BasicOutputCollector, OutputFieldsDeclarer}
import org.apache.storm.tuple.Tuple

class PrinterBolt extends BaseBasicBolt {
  override def execute(input: Tuple, collector: BasicOutputCollector): Unit = {
   println(s"----> PrinterBolt -> tuple : $input")
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
  }
}