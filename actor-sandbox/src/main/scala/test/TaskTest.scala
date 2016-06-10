package test

import com.ning.http.client.{ProxyServer, Response, AsyncCompletionHandler, AsyncHttpClient}

import scalaz._
import scalaz.concurrent._
import Scalaz._

object TaskTest {

  def task1:Task[String] = Task now "paul";
  def task2:Task[Int] = Task now 42;
  def task3:Task[String] = Task now "anita";

  private val asyncHttp = new AsyncHttpClient

  def get(s: String):Task[Response] =
    Task.async(k => asyncHttp.prepareGet(s)
                              .setProxyServer(new ProxyServer("127.0.0.1", 3128))
                              .execute(toHandler(k)))

  def toHandler(k: (Throwable \/ Response) => Unit) = new AsyncCompletionHandler[Unit] {
    override def onCompleted(r: Response): Unit = k(\/-(r))
    override def onThrowable(t: Throwable): Unit = k(-\/(t))
  }

  def doIt:Task[String] =
    for {
      n <- task1
      a <- task2
      w <- task3
    } yield s"$n is $a and is married to $w"

  def main(args: Array[String]) {

    val details = doIt.attemptRun
    println(details)

    val r = get("http://www.bbc.co.uk").run
    println(r.getStatusText)

  }
}
