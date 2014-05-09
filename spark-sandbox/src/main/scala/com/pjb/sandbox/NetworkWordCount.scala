package com.pjb.sandbox
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.storage.StorageLevel

object NetworkWordCount {

  def main(args: Array[String]) {

    StreamingExamples.setStreamingLogLevels()
    val ssc = new StreamingContext("local[2]",
                                   "NetworkWordCount",
                                   Seconds(10),
                                   System.getenv("SPARK_HOME"),
                                   StreamingContext.jarOfClass(this.getClass).toSeq)
    val lines = ssc.socketTextStream("localhost", 9999, StorageLevel.MEMORY_ONLY_SER)

    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1)).reduceByKey((a: Int, b: Int) => a + b)
    wordCounts.print()
    ssc.start()
    ssc.awaitTermination()
  }

}
