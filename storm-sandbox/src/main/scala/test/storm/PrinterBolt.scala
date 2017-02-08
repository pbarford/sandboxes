package test.storm

import java.util

import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.Tuple

class PrinterBolt extends BaseRichBolt {

  var _collector:Option[OutputCollector] = None

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    _collector = Some(collector)
  }

  override def execute(input: Tuple): Unit = {
    input.getSourceStreamId match {
      case "agg" =>
        println(s"----> PrinterBolt -> AGG tuple : $input")
        _collector.map(c => c.ack(input))

      case _ =>
        println(s"----> PrinterBolt -> tuple : $input")
        _collector.map(c => c.ack(input))
    }

  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
  }
}