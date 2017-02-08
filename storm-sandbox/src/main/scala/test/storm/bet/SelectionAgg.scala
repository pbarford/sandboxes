package test.storm.bet

import java.util

import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

class SelectionAgg extends BaseRichBolt {

  var collector:Option[OutputCollector] = None
  var aggregates = scala.collection.mutable.Map.empty[String,AggregateRoot]

  case class AggregateRoot(totalLiability:Double, totalBets:Int)

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    this.collector = Some(collector)
  }

  override def execute(tuple: Tuple): Unit = {
    println(s"----> SelectionAgg -> execute $tuple")

    tuple.getSourceStreamId match {
      case "bet" =>
        val s = tuple.getString (0)
        val root = aggregates.getOrElse (s, AggregateRoot (0.0, 0) )
        val update = AggregateRoot (root.totalLiability + calculateLiability (tuple.getDouble (1), tuple.getDouble (2) ), root.totalBets + 1)
        println (s"updating $s --> $root to $update")
        aggregates += (s -> update)
        collector.map { c =>
          c.emit("agg", tuple, new Values(s, Double.box(update.totalLiability), Int.box(update.totalBets)))
          c.ack(tuple)
        }
    }
  }

  private def calculateLiability(stake:Double, price:Double):Double = {
    stake * (price - 1)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer.declareStream("agg", new Fields("selection", "totalLiability", "totalBets"))
  }
}
