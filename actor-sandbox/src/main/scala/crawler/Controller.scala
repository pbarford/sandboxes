package crawler

import akka.actor._
import crawler.Controller.{Result, Check}

import scala.concurrent.duration._
import crawler.Controller.Result
import crawler.Controller.Check
import akka.util.Timeout
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext

object Controller {
  case class Check(url: String, depth: Int)
  case class Result(value: Iterable[String])
}

class Controller extends Actor with ActorLogging {
  implicit val exec = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  //context.setReceiveTimeout(10.seconds)
  context.system.scheduler.scheduleOnce(10.seconds, self, Timeout)

  var cache = Set.empty[String]
  var children = Set.empty[ActorRef]

  def receive = {

    case Check(url, depth) =>
      log.debug("{} checking {}", depth, url)
      if(!cache(url) && depth > 0)
        children += context.actorOf(Props(new Getter(url, depth - 1)))
      cache += url

    case Getter.Done =>
      children -= sender
      if(children.isEmpty) context.parent ! Result(cache)

    case ReceiveTimeout => children foreach (_ ! Getter.Abort)
    case Timeout => children foreach (_ ! Getter.Abort)
  }
}
