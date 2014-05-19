package com.pjb.scala
import com.pjb.scala.impl.DefaultGatherAggregator
import com.pjb.scala.impl.DefaultTask2
import com.pjb.scala.impl.DefaultTask

object Test {

  def main(args : Array[String]) {

    val agg = new DefaultGatherAggregator()
    agg.addTask(new DefaultTask(agg))
    agg.addTask(new DefaultTask2(agg))
    agg.addTask(new DefaultTask(agg))

    agg.start
  }
}