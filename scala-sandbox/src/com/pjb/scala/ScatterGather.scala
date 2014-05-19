package com.pjb.scala

import actors.Actor
import collection.mutable.ListBuffer

case class DONE(data: Any)

trait ScatterTask extends Actor {
  def invoke(): Any
}

abstract class AbstractScatterTask(agg: GatherAggregator) extends ScatterTask {
  var aggregator = agg
  def act {
    invokeInternal()
  }

  def invokeInternal() {
    aggregator ! DONE (invoke())
  }
}

trait GatherAggregator extends Actor {
  private val scatterTasks : ListBuffer[ScatterTask] = ListBuffer()

  def aggregate()
  def handleData(data: Any)

  def addTask(task : ScatterTask) {
    scatterTasks += task
  }

  def act() {
    var taskCount = scatterTasks.size

    scatterTasks.foreach(task => task.start)

    while(taskCount !=0) {
      receive {
        case DONE(data) =>
          println("task done")
          handleData(data)
          taskCount -= 1
      }
    }
    aggregate()
  }
}
