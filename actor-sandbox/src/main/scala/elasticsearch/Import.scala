package elasticsearch

import java.security.SecureRandom
import javax.net.ssl.{TrustManager, SSLContext}

import com.datastax.driver.core.querybuilder.{QueryBuilder, BuiltStatement}
import com.datastax.driver.core.{SSLOptions, Cluster}
import com.ning.http.client.AsyncHttpClient
import com.paddypower.destmgmt.Cassandra.{Cassandra, CassandraProvider}

import java.net.Socket
import java.security.cert.X509Certificate
import javax.net.ssl.{SSLEngine, X509ExtendedTrustManager}

import com.paddypower.destmgmt.Cassandra._


object Import {

  private val client = new AsyncHttpClient

  def cassandraProvider(points:String, user:String, pass:String, keyspace:String) = new CassandraProvider {

    val session = Cluster.builder()
      .addContactPoint(points)
      .withSSL(trustAllWithoutCertOptions)
      .withCredentials(user, pass)
      .build()
      .connect(keyspace)

    override def apply[A](f: Cassandra[A]): A = {
      f(session)
    }
  }

  private def trustAllWithoutCertOptions: SSLOptions = {
    val context = SSLContext.getInstance("TLS")
    context.init(null, Array[TrustManager](new TrustAllManager()), new SecureRandom)
    new SSLOptions(context, SSLOptions.DEFAULT_SSL_CIPHER_SUITES)
  }

  lazy val cassandraOat = cassandraProvider("10.105.160.13", "dm_oat", "465m1bvNorzw0ft41mI8", "dm_keyspace_oat")

  def runInOat[A](f: CassandraProvider => A): A = f(cassandraOat)

  def process():CassandraProvider => Unit = {
    cp => {
      val rs = cp(executeAsyncSelect(get(4046739))).run

        while(!rs.isExhausted) {
          val r = rs.one()
          val data = r.getString("data")
          val seqno = r.getInt("seqno")
          val doc = "{ \"sequenceNo\" : " + seqno + ", " + data.substring(1, data.length)

          client.preparePost("http://localhost:9200/dm/event").setBody(doc).execute()
        }

      println("done")
      cp(shutdown())
    }
  }

  def get(eventId:Int):BuiltStatement = {
    QueryBuilder.select("data", "seqno").from("inboundmessages").where(QueryBuilder.eq("eventid", eventId))

  }

  def main(args: Array[String]) {
    runInOat(process())
  }

}

class TrustAllManager extends X509ExtendedTrustManager {
  override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String, socket: Socket): Unit = {}

  override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String, sslEngine: SSLEngine): Unit = {}

  override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String, socket: Socket): Unit = {}

  override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String, sslEngine: SSLEngine): Unit = {}

  override def getAcceptedIssuers: Array[X509Certificate] = null

  override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

  override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
}
