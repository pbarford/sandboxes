package com.paddypower.destmgmt

import com.datastax.driver.core.{ResultSet, Session}
import com.datastax.driver.core.querybuilder.{QueryBuilder, BuiltStatement}
import com.google.common.util.concurrent.{FutureCallback, Futures}

import scalaz.{\/-, -\/}
import scalaz.concurrent.Task

object Cassandra {

  case class Cassandra[A](fsa: Session => A) {
    def apply(s:Session):A = fsa(s)
    def map[B](f: A => B): Cassandra[B] = {
      new Cassandra(s => f(fsa(s)))
    }

    def flatMap[B](f: A => Cassandra[B]):Cassandra[B] = {
      new Cassandra(s => f(fsa(s))(s))
    }
  }

  def pure[A](a: A):Cassandra[A] = Cassandra(s => a)

  implicit def cassandra[A](f: Session => A):Cassandra[A] = Cassandra(f)

  def executeStatement(statement:BuiltStatement):Session => Unit = {
    session => {
      session.execute(statement)
    }
  }

  private def executeAsync(statement:BuiltStatement):Session => Task[Unit] = {
    session => {
      Task.async {
        register =>
          Futures.addCallback(session.executeAsync(statement), new FutureCallback[ResultSet] {
            override def onFailure(t: Throwable): Unit = register(-\/(t))
            override def onSuccess(result: ResultSet): Unit = register(\/-())
          })
      }
    }
  }

  def executeAsyncStatement(statement:BuiltStatement):Session => Task[Unit] = {
    executeAsync(statement)
  }

  def executeBatchAsync(statements:Seq[BuiltStatement]):Session => Task[Unit] = {

    statements match {
      case Seq(x, xs@_*) =>
          val batch = QueryBuilder.batch(statements.head)
          for(stmt <- statements.tail) {
            batch.add(stmt)
          }
          executeAsync(batch)
        }
  }

  def shutdown():Session => Unit = {
    session => {
      session.close()
      session.getCluster.close()
    }
  }

  abstract class CassandraProvider {
    def apply[A](f: Cassandra[A]): A
  }
}
