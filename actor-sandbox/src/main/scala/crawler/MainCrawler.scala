package crawler

import akka.actor.{Props, ReceiveTimeout, Actor}
import crawler.Receptionist.{Get, Failed, Result}
import scala.concurrent.duration._

class MainCrawler extends Actor {

  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  context.setReceiveTimeout(10.seconds)
  receptionist ! Get("http://www.stackoverflow.com")
  receptionist ! Get("http://www.google.com")

  def receive = {
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Results for '$url':\n", "\n", "\n"))
    case Failed(url, msg) =>
      println(s"Failed to fetch '$url' --> $msg \n")
    case ReceiveTimeout =>
      context.stop(self)
  }

  override def postStop(): Unit = {
    WebClient.shutdown()
  }
}
