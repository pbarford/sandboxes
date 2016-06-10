package persistence

import akka.actor.ActorRef

import scalaz.concurrent.Task

trait Journal[A] {
  type EventId = Long

  def write(a:A)(cb : A => Unit):Task[Unit]
  def restore(id:EventId)(ref:ActorRef):Task[Unit]

}
