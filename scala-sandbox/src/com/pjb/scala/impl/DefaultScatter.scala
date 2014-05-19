package com.pjb.scala.impl
import com.pjb.scala.AbstractScatterTask
import com.pjb.scala.GatherAggregator

class DefaultTask(agg: GatherAggregator) extends AbstractScatterTask(agg) {
  def invoke():Any = {
    println("invoked")
    return "blah"
  }
}

class DefaultTask2(agg: GatherAggregator) extends AbstractScatterTask(agg) {

  def invoke():Any = {
    println("invoked 2")
    return "boo"
  }
}