package com.paddypower.destmgmt

import com.datastax.driver.core.{ResultSet, Session}
import com.datastax.driver.core.querybuilder.{QueryBuilder, BuiltStatement}
import com.google.common.util.concurrent.{FutureCallback, Futures}

import scalaz.{\/, \/-, -\/}
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
          /*
          Futures.addCallback(session.executeAsync(statement), new FutureCallback[ResultSet] {
            override def onFailure(t: Throwable): Unit = register(-\/(t))
            override def onSuccess(result: ResultSet): Unit = register(\/-())
          })
          */
          Futures.addCallback(session.executeAsync(statement), toHandlerUnit(register))

      }
    }
  }

  def toHandlerUnit(k: (Throwable \/ Unit) => Unit) = new FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = k(-\/ (t))
    override def onSuccess(v: ResultSet): Unit = k(\/- (v))
  }

  def toHandlerResultSet(k: (Throwable \/ ResultSet) => Unit) = new FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = k(-\/ (t))
    override def onSuccess(v: ResultSet): Unit = k(\/- (v))
  }

  private def selectAsync(statement:BuiltStatement):Session => Task[ResultSet] = {
    session => {
      Task.async {
        register =>
          /*
          Futures.addCallback(session.executeAsync(statement), new FutureCallback[ResultSet] {
            override def onFailure(t: Throwable): Unit = {
              register(-\/(t))
            }
            override def onSuccess(results: ResultSet): Unit = register(\/- (results))
          })
          */
          Futures.addCallback(session.executeAsync(statement), toHandlerResultSet(register))
      }
    }
  }

  def executeAsyncStatement(statement:BuiltStatement):Session => Task[Unit] = {
    executeAsync(statement)
  }

  def executeAsyncSelect(statement:BuiltStatement):Session => Task[ResultSet] = {
    selectAsync(statement)
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
