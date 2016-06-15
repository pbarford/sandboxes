package persistence

import akka.actor.ActorRef

import scalaz.concurrent.Task

trait Journal[A] {
  type EventId = Long

  def write(a:A)(ref:ActorRef):Task[Unit]
  def restore(id:EventId)(ref:ActorRef):Task[Unit]

}
