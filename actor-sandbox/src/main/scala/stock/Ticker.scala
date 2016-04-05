package stock

import java.text.SimpleDateFormat

import com.datastax.driver.core.Session
import com.ning.http.client.{ProxyServer, Response, AsyncCompletionHandler, AsyncHttpClient}
import stock.Cassandra.{Cassandra, CassandraProvider}
import scala.concurrent.duration._
import scalaz._

import scalaz.concurrent._
import scalaz.stream._
import time._
import scalaz.stream.Cause._
import com.datastax.driver.core.Cluster


object Ticker {

  private val client = new AsyncHttpClient
  private val dtf1 = new SimpleDateFormat("yyyy.MM.dd")
  private val dtf2 = new SimpleDateFormat("HH:mm:ss.SSS")
  private val session:Session = Cluster.builder().addContactPoint("127.0.0.1").build().connect("stocks")
  private val stmt = session.prepare("insert into quotes (symbol, trading_date, quote_timestamp, time, bid, ask, exchange) values (?, ?, ?, ?, ?, ?, ?);")
  implicit val cassandraProvider = new CassandraProvider {
    override def apply[A](f: Cassandra[A]): A = f(session)
  }

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
    implicit val sc = new java.util.concurrent.ScheduledThreadPoolExecutor(1)
    awakeEvery(3 second) flatMap {
      _ =>
        Process.eval(
          Option(get("\"AAPL\"")
            .flatMap(resp => transform(resp.getResponseBody)).map(quote => quote))
            .getOrElse(throw Terminated(End))
        )
    }
/*
    Process.repeatEval (
        Option(get("\"AAPL\"")
          .flatMap(resp => transform(resp.getResponseBody)).map(quote => quote))
          .getOrElse(throw Terminated(End))

    )*/
  }

  def date():(Long, String, String) = {
    val time = System.currentTimeMillis()
    val dte = new java.util.Date(time)
    (time, dtf1.format(dte), dtf2.format(dte))
  }

  def insertQuote(quote:Quote):Session => Unit = {
    s => {
      val dte = date()
      s.execute(stmt.bind(quote.Symbol,
                          dte._2,
                          java.lang.Long.valueOf(dte._1),
                          dte._3,
                          quote.Bid,
                          quote.Ask,
                          quote.StockExchange))
    }
  }

  def cassandraWriter(implicit ca:CassandraProvider): Sink[Task, Quote] = {
    Process.repeatEval {
      Task.delay {
        quote: Quote =>
          Task.delay {
            ca(insertQuote(quote))
          }
      }
    }
  }

  def transform(body:String):Task[Quote] = {
    import org.json4s._
    import org.json4s.native.JsonMethods._
    implicit val formats = DefaultFormats
    val res = parse(body).extract[HttpResponse]
    Task now res.query.results.quote
  }

  def process():Process[Task, Unit] = {
    read.repeat to cassandraWriter
  }

  def main(args: Array[String]) {
    process.run.unsafePerformSyncAttempt
  }

}
