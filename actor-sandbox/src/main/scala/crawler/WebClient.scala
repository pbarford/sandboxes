package crawler

import com.ning.http.client.{ProxyServer, AsyncHttpClient}
import java.util.concurrent.Executor
import scala.concurrent.{Promise, Future}

object WebClient {

  private val client = new AsyncHttpClient

  case class BadStatus(code: Int) extends Throwable

  def get(url: String)(implicit exec: Executor): Future[String] = {
    val f = client.prepareGet(url).setProxyServer(new ProxyServer("127.0.0.1", 3128)).execute()
    val p = Promise[String]()
    f.addListener(new Runnable {
      def run(): Unit = {
        val response = f.get()
        if(response.getStatusCode < 400)
          p.success(response.getResponseBodyExcerpt(131072))
        else
          p.failure(BadStatus(response.getStatusCode))
      }
    }, exec)
    p.future
  }

  def shutdown() = {
    client.close()
  }

}

