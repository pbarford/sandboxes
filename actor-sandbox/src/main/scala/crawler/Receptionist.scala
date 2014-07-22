package crawler

import akka.actor.{Props, ActorRef, Actor}
import crawler.Receptionist.{Get, Result, Failed}

object Receptionist {
  case class Failed(url: String, msg: String)
  case class Get(url: String)
  case class Result(url: String, links: Iterable[String])
}

class Receptionist extends Actor {
  case class Job(client: ActorRef, url: String)
  var reqNo = 0

  def receive = waiting

  val waiting: Receive = {
    case Get(url) => context.become(runNext(Vector(Job(sender, url))))
  }

  def running(queue: Vector[Job]): Receive = {
    case Controller.Result(links) =>
      val job = queue.head
      job.client ! Result(job.url, links)
      context.stop(sender)
      context.become(runNext(queue.tail))
    case Get(url) =>
      context.become(enqueueJob(queue, Job(sender, url)))
  }

  def runNext(queue: Vector[Job]): Receive = {
    reqNo += 1
    if(queue.isEmpty) waiting
    else {
      val controller = context.actorOf(Props[Controller], s"c$reqNo")
      controller ! Controller.Check(queue.head.url, 2)
      running(queue)
    }
  }

  def enqueueJob(queue: Vector[Job], job: Job): Receive = {
    if(queue.size > 3) {
      sender ! Failed(job.url, "queue too big")
      running(queue)
    } else running(queue :+ job)
  }
}
