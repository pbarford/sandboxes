package crawler
import akka.pattern.{ pipe }
import akka.actor.{ActorRef, Actor}
import crawler.Cache.{Result, Get}
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext


object Cache {
  case class Get(url: String)
  case class Result(client: ActorRef, url: String, body: String)
}

class Cache extends Actor {
  implicit val exec = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  var cache = Map.empty[String, String]

  def receive = {
    case Get(url) =>
      if(cache contains url) sender ! cache(url)
      else {
        val client = sender
        WebClient get url map (body => Result(client, url, body)) pipeTo self
      }
    case Result(client, url, body) =>
      cache += url -> body
      client ! body
  }

}
