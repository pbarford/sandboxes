package com.paddypower.destmgmt

import com.datastax.driver.core.{Cluster}
import com.datastax.driver.core.querybuilder.{QueryBuilder, BuiltStatement}
import com.paddypower.destmgmt.Cassandra._

import scalaz.{\/, \/-, -\/}
object TestCassandra {

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

  def process():CassandraProvider => Unit = {

    /*
    val cbs = CassandraBatchState
    var r = cbs.add(CassandraBatchState.dummy(1)).run(List.empty)
    for(i <- 2 to 10) {
      r = cbs.add(CassandraBatchState.dummy(i)).run(r._1)
    }
    println(s"no of statements = ${r._2}")

    journal => {
      for {
        r <- r._1
      } yield journal(executeStatement(r))
      journal(shutdown)
    }

    val stmts = for {
      i <- 1 to 10
      s <- Seq(dummy(i))
    } yield s
    println(s"no of statements = ${stmts.size}")
    */

    cassandraProvider => {

      /*
      val tasks = for {
        l <- CassandraBatchState.add(dummy(11)).run(List.empty)._1
        seqno <- 12 to 30
        s <- CassandraBatchState.add(dummy(seqno)).run(List(l))._1
      } yield cassandraProvider(executeAsyncStatement(s))
      tasks.map { task => task.unsafePerformSync }
      */

      //Sync execute
      for {
        seqno <- 1 to 10
        stmt <- Seq(dummy(seqno))
      } yield cassandraProvider(executeStatement(stmt))

      //Async execute
      val tasks = for {
        seqno <- 12 to 30
        stmt <- Seq(dummy(seqno))
      } yield cassandraProvider(executeAsyncStatement(stmt))
      tasks.map { task => task.attemptRun }

      //Batch Async execute
      val stmts = for {
        seqno <- 31 to 32
        stmt <- Seq(bad(seqno))
      } yield stmt
      val batchTask = cassandraProvider(executeBatchAsync(stmts))
      batchTask.attemptRun match {
        case -\/(e) => println(s"error ${e.getMessage}")
        case \/-(a) => println("ok")
      }

      cassandraProvider(shutdown())
    }


  }

  def dummy(seq:Int):BuiltStatement = {
    QueryBuilder.insertInto("inboundmessages").value("eventid", 1)
                                              .value("seqno", seq)
                                              .value("headers", s"testing $seq")
                                              .value("data", s"testing $seq")
                                              .value("received_timestamp", java.lang.Long.valueOf(System.currentTimeMillis))
                                              .value("version", 1)
  }

  def bad(seq:Int):BuiltStatement = {
    QueryBuilder.insertInto("inboundmessages").value("eveyntid", 1)
                                              .value("seqno", seq)
                                              .value("headers", s"testing $seq")
                                              .value("data", s"testing $seq")
                                              .value("received_timestamp", java.lang.Long.valueOf(System.currentTimeMillis))
                                              .value("version", 1)
  }

  def main(args: Array[String]) {
    runInLocalDev(process())
  }
}
