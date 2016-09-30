package storm

import org.apache.storm.topology.{BasicOutputCollector, OutputFieldsDeclarer}
import org.apache.storm.topology.base.BaseBasicBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

class WordCount extends BaseBasicBolt {

  var counts = scala.collection.mutable.Map.empty[String,Int]

  override def execute(input: Tuple, collector: BasicOutputCollector): Unit = {
    println(s"----> WordCount -> execute $input")
    val sentence:String = input.getString(0)


    for(w <- sentence.split(" ")) {
      val c = counts.getOrElse(w, 0)
      counts += (w -> (c + 1))
      collector.emit(new Values(w, Int.box(counts.getOrElse(w, 0))))
    }
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer.declare(new Fields("word", "count"));
  }
}
