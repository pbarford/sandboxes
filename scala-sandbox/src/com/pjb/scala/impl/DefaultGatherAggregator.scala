package com.pjb.scala.impl
import scala.collection.mutable.ListBuffer
import com.pjb.scala.GatherAggregator

class DefaultGatherAggregator extends GatherAggregator {
  private var dataList : ListBuffer[Any] = ListBuffer()

  def handleData(data: Any) {
    dataList += data
  }

  def aggregate() {
    println("aggregate")
    dataList.foreach(data => println("data [" + data + "]"))
  }

}