package test.storm.bet

import org.apache.storm.topology.{BasicOutputCollector, OutputFieldsDeclarer}
import org.apache.storm.topology.base.BaseBasicBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

class SelectionBasicAgg extends BaseBasicBolt {

  var aggregates = scala.collection.mutable.Map.empty[String,AggregateRoot]
  case class AggregateRoot(totalLiability:Double, totalBets:Int)

  override def execute(tuple: Tuple, collector: BasicOutputCollector): Unit = {
    println(s"----> SelectionAgg -> execute $tuple")
    tuple.getSourceStreamId match {
      case "bet" =>
        val s = tuple.getString (0)
        val root = aggregates.getOrElse (s, AggregateRoot (0.0, 0) )
        val update = AggregateRoot (root.totalLiability + calculateLiability (tuple.getDouble (1), tuple.getDouble (2) ), root.totalBets + 1)
        println (s"updating $s --> $root to $update")
        aggregates += (s -> update)
        collector.emit (new Values (s, Double.box (update.totalLiability), Int.box (update.totalBets) ) )
    }

  }

  private def calculateLiability(stake:Double, price:Double):Double = {
    stake * (price - 1)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer.declare(new Fields("selection", "totalLiability", "totalBets"))
  }
}
