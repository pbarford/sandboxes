package com.paddypower.akka.persistence

import com.datastax.driver.core.querybuilder.{BuiltStatement, QueryBuilder}
import com.datastax.driver.core.{ResultSet, Session}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.paddypower.akka.domain.Model.SelectionBet

import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.Task

class Journal(session:Session) {

  private def persistTask: BuiltStatement => Task[Unit] = { statement =>
    Task.async[Unit] {
      cb =>
        Futures.addCallback(session.executeAsync(statement), toHandlerUnit(cb))
    }
  }

  private def toHandlerUnit(k: (Throwable \/ Unit) => Unit) = new FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = k(-\/ (t))
    override def onSuccess(v: ResultSet): Unit = k(\/- (v))
  }

  private def insertStatement: SelectionBet => BuiltStatement = { statement =>
    QueryBuilder.insertInto("SELECTION_BET").value("", "")
  }

  def persistBet(s:SelectionBet, callback : (Throwable \/ Unit) => Unit): Unit = {

    persistTask(insertStatement(s)).runAsync(callback)
  }
}
