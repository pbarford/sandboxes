package spark

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object KafkaWordCount extends App {

  val sparkConf = new SparkConf().setAppName("KafkaWordCount").setMaster("local[1]")
  val kafkaParams = Map[String, String]("metadata.broker.list" -> "192.168.99.102:9092")
  val ssc = new StreamingContext(sparkConf, Seconds(5))
  ssc.checkpoint("checkpoint")

  val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, Set("test2"))
  val lines = messages.map(_._2)
  val words = lines.flatMap(_.split(" "))
  val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _)
  wordCounts.print()

  ssc.start()
  ssc.awaitTermination()
}

