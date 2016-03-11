package com.paddypower.destmgmt

import com.datastax.driver.core.{Cluster, Session}
import com.datastax.driver.core.querybuilder.{BuiltStatement, QueryBuilder}

object Journal {

  case class Journal[A](x: Session => A) {
    def apply(s:Session) = x(s)
    def map[B](f: A => B): Journal[B] = {
      new Journal(s => f(x(s)))
    }

    def flatMap[B](f: A => Journal[B]):Journal[B] = {
      new Journal(s => f(x(s))(s))
    }
  }

  def pure[A](a: A):Journal[A] = Journal(s => a)

  implicit def cassandra[A](f: Session => A):Journal[A] = Journal(f)

  def insert(stmt:BuiltStatement):Session => Unit = {
    s => {
      s.execute(stmt)
    }
  }

  abstract class JournalProvider {
    def apply[A](f: Journal[A]): A
  }
  def mkProvider(points:String, user:String, pass:String, keyspace:String) = new JournalProvider {

    val session = Cluster.builder()
                        .addContactPoint(points)
                        .withCredentials(user, pass)
                        .build().connect(keyspace)

    override def apply[A](f: Journal[A]): A = {
      f(session)
    }
  }

  lazy val cassandraDev = mkProvider("127.0.0.1", "", "", "dm_keyspace_dev")

  def runInDev[A](f: JournalProvider => A): A = f(cassandraDev)

  def main(args: Array[String]) {
    runInDev(process())
  }

  def process():JournalProvider => Unit = {
    val s = StatefulCassandraBatch

    var r = s.add(dummy(1)).run(List.empty)
    for(i <- 2 to 10) {
      r = s.add(dummy(i)).run(r._1)
    }
    println(s"no of statements = ${r._2}")
    journal => {
      for(r <- r._1) {
        journal(insert(r))
      }
    }
  }

  def dummy(id:Int):BuiltStatement = {
    QueryBuilder.insertInto("inboundmessages").value("eventid", id)
      .value("seqno", 1)
      .value("headers", "")
      .value("data", "")
      .value("received_timestamp", java.lang.Long.valueOf(System.currentTimeMillis))
      .value("version", 1)
  }
}
