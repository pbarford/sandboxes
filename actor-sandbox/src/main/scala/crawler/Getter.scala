package crawler

import akka.actor.{Status, Actor}
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executor
import crawler.Getter.{Abort, Done}
import akka.pattern.{ pipe }

object Getter {
  case object Done
  case object Abort
}

class Getter(url: String, depth: Int) extends Actor {

  implicit val exec = context.dispatcher.asInstanceOf[Executor with ExecutionContext]

  val A_TAG = "(?i)<a ([^>]+)>.+?</a>".r
  val HREF_ATTR = """\s*(?i)href\s*=\s*(?:"([^"]*)"|'([^']*)'|([^'">\s]+))""".r

  WebClient get url pipeTo self
  /*
  val future = WebClient.get(url)

  future.onComplete {
    case Success(body) => self ! body
    case Failure(err) => self ! Status.Failure(err)
  }
  */
  def receive = {
    case body: String =>
      for (link <- findLinks(body))
        context.parent ! Controller.Check(link, depth)
      stop()

    case Abort => stop()
    case _: Status.Failure => stop()

  }

  def stop(): Unit = {
    context.parent ! Done
    context.stop(self)
  }

  def findLinks(body: String): Iterator[String] = {
    for {
      anchor <- A_TAG.findAllMatchIn(body)
      HREF_ATTR(dquot, quot, bare) <- anchor.subgroups
    } yield
      if(dquot != null) dquot
      else if(quot != null) quot
      else bare
  }
}
