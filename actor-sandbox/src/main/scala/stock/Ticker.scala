package stock

import com.ning.http.client.{ProxyServer, Response, AsyncCompletionHandler, AsyncHttpClient}

import scalaz._

import scalaz.concurrent._
import scalaz.stream._
import scalaz.stream.Cause._

object Ticker {

  private val client = new AsyncHttpClient

  case class HttpResponse(query:Query)
  case class Query(count:Int, created:String, results: Result)
  case class Result(quote:Quote)
  case class Quote(Symbol:String,
                   Name:String,
                   Ask:String,
                   Bid:String,
                   DaysLow:String,
                   DaysHigh:String,
                   Currency:String,
                   Change_PercentChange:String,
                   StockExchange:String)

  def get(symbol: String):Task[Response] =
    Task.async(k =>
                client.prepareGet(s"http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(${symbol})%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json")
                      .setProxyServer(new ProxyServer("127.0.0.1", 3128))
                      .execute(toHandler(k))
    )

  def toHandler(k: (Throwable \/ Response) => Unit) = new AsyncCompletionHandler[Unit] {
    override def onCompleted(r: Response): Unit = k(\/-(r))
    override def onThrowable(t: Throwable): Unit = k(-\/(t))
  }

  def read():Process[Task, Quote] = {
    Process.repeatEval (
      Option(get("\"PPB.L\"")
        .flatMap (resp => transform(resp.getResponseBody)).map(quote => quote))
        .getOrElse(throw Terminated(End))
    )
  }

  def transform(body:String):Task[Quote] = {
    import org.json4s._
    import org.json4s.native.JsonMethods._
    implicit val formats = DefaultFormats
    val res = parse(body).extract[HttpResponse]
    Task now res.query.results.quote
  }

  def process():Process[Task, Unit] = {
    val out = io.stdOutLines
    read.repeat.map(quote => quote.toString) to out
  }

  def main(args: Array[String]) {
    process.run.unsafePerformSyncAttempt
  }

}
