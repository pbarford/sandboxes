package com.paddypower.destmgmt

import com.datastax.driver.core.{Row, Cluster}
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.paddypower.destmgmt.Cassandra.CassandraProvider

import com.paddypower.destmgmt.Cassandra._

import scalaz.{\/, \/-, -\/}

case class Snapshot(uptoSeqNo:Int)

class EventSource(correlationKey:String) {

  def restore() : CassandraProvider => Unit  = {
    cp => {
      val r = for {
        s <- getSnapshot(cp)
        es <- getEvents(s)(cp)
      } yield es

      println(r)
    }
  }

  def persist = {
  }

  private def getEvents(s: Snapshot)(implicit cp :CassandraProvider): String \/ Seq[Int] = {
    val statement = selectEventsFromSeqNo(s.uptoSeqNo)
    \/-(Seq.empty[Int])
  }

  private def getSnapshot(implicit cp :CassandraProvider): String \/ Snapshot = {
    cp(executeAsyncSelect(getLatestSnapshotStatement)).unsafePerformSyncAttempt match {
      case -\/(t) => -\/(s"error : ${t.getMessage}")
      case \/-(rs) => {
        rs.one match {
          case r: Row =>
            \/-(Snapshot(r.getInt("uptoseqno")))
          case _ =>
            -\/("not found")
        }
      }
    }
  }

  def getLatestSnapshotStatement = {
    QueryBuilder.select("uptoseqno")
                .from("snapshots")
                .where(QueryBuilder.eq("key", correlationKey)).limit(1)
  }

  def selectEventsFromSeqNo(seqNo:Int) = {
    println(s"selecting from seqno $seqNo")
    QueryBuilder.select("seqno")
                .from("events")
                .where(QueryBuilder.eq("key", correlationKey))
                .and(QueryBuilder.gte("seqNo", seqNo))
  }

}

object EventSource {

  def cassandraProvider(points:String, user:String, pass:String, keyspace:String) = new CassandraProvider {

    val session = Cluster.builder()
      .addContactPoint(points)
      .withCredentials(user, pass)
      .build()
      .connect(keyspace)

    override def apply[A](f: Cassandra[A]): A = {
      f(session)
    }
  }

  lazy val cassandraLocalDev = cassandraProvider("127.0.0.1", "", "", "dm_keyspace_dev")

  def runInLocalDev[A](f: CassandraProvider => A): A = f(cassandraLocalDev)

  def main(args: Array[String]) {

    val es = new EventSource("test.1234")
    runInLocalDev(es.restore())
    println("ok")
  }
}
