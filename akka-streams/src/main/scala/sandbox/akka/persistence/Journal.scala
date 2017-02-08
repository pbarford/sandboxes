package sandbox.akka.persistence

import com.datastax.driver.core.querybuilder.{Batch, BuiltStatement, QueryBuilder}
import com.datastax.driver.core.{ResultSet, Session}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import sandbox.akka.domain.Model.{Selection, SelectionBet}
import sandbox.akka.domain.ModelUtil

import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.Task

class Journal(session:Session) {

  private def persistTask: Batch => Task[Unit] = { statement =>
    Task.async[Unit] {
      cb =>
        Futures.addCallback(session.executeAsync(statement), toHandlerUnit(cb))
    }
  }

  private def toHandlerUnit(k: (Throwable \/ Unit) => Unit) = new FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = k(-\/ (t))
    override def onSuccess(v: ResultSet): Unit = k(\/- (v))
  }

  private def generateStatementForBet : SelectionBet => List[BuiltStatement] = { bet =>
    bet.selections.size match {
      case 1 => List(insertSelectionBetStatement( bet,bet.selections.head))
      case x:Int if x > 1 => List(insertMultiBetStatement(bet))
      case _ => List.empty[BuiltStatement]
    }
  }

  private def generateBatch: SelectionBet => Batch = { bet =>

    //val statements = generateStatementForBet(bet) ++ bet.selections.map(s => insertBetStatement(bet, s)).toList
    val statements = generateStatementForBet(bet)
    statements match {
      case Seq(x, xs@_*) =>
        val batch = QueryBuilder.batch(statements.head)
        for(stmt <- statements.tail) {
          batch.add(stmt)
        }
        batch
    }
  }

  private def generateBatchForStatements: List[BuiltStatement] => Batch = { statements =>
    statements match {
      case Seq(x, xs@_*) =>
        val batch = QueryBuilder.batch(statements.head)
        for (stmt <- statements.tail) {
          batch.add(stmt)
        }
        batch
    }
  }

  private def insertSelectionBetStatement(selectionBet: SelectionBet, selection: Selection):BuiltStatement = {
    QueryBuilder.insertInto("SELECTION_BET").value("SELECTION_ID", selection.getSelectionId)
                                            .value("BET_ID", selectionBet.betID)
                                            .value("BET_TIMESTAMP", selectionBet.betTimeStamp)
                                            .value("ACTION", "PLACED")
                                            .value("STAKE", selectionBet.stake)
                                            .value("PRICE_TYPE", selection.price.priceType)
                                            .value("PRICE", selection.price.decimalPrice.getOrElse(0.0))
                                            .value("BET_TYPE", selectionBet.betType)
                                            .value("SUB_KEY", selectionBet.key)
                                            .value("CUR", selectionBet.betCurrency)
  }

  private def insertMultiBetStatement(selectionBet: SelectionBet):BuiltStatement = {
    QueryBuilder.insertInto("MULTI_BET").value("MULTI_KEY", selectionBet.key)
      .value("BET_ID", selectionBet.betID)
      .value("BET_TIMESTAMP", selectionBet.betTimeStamp)
      .value("ACTION", "PLACED")
      .value("PROFIT_LOSS", ModelUtil.formatD(selectionBet.betLiability))
      .value("PAYOUT_TOTAL", ModelUtil.formatD(selectionBet.betPayout))
      .value("BET_TYPE", selectionBet.betType)
      .value("SUB_BET_TYPE", selectionBet.subType)
      .value("CUR", selectionBet.betCurrency)
  }

  private def insertBetStatement(selectionBet: SelectionBet, selection: Selection):BuiltStatement = {
    QueryBuilder.insertInto("BET").value("BET_ID", selectionBet.betID)
      .value("SUB_KEY", selectionBet.key)
      .value("SELECTION_ID", selection.getSelectionId)
      .value("BET_TIMESTAMP", selectionBet.betTimeStamp)
      .value("ACTION", "PLACED")
      .value("STAKE", selectionBet.stake)
      .value("PRICE_TYPE", selection.price.priceType)
      .value("PRICE", selection.price.decimalPrice.getOrElse(0.0))
      .value("BET_TYPE", selectionBet.betType)
      .value("CUR", selectionBet.betCurrency)
  }

  def persistBets(bets:List[SelectionBet], callback : (Throwable \/ Unit) => Unit): Unit = {
    val x: List[BuiltStatement] = for {
      b <- bets
      s <- b.selections
    } yield insertBetStatement(b, s)
     persistTask(generateBatchForStatements(x)).runAsync(callback)
  }

  def persistBets(bets:List[SelectionBet]): Unit = {
    val x: List[BuiltStatement] = for {
      b <- bets
      s <- b.selections
    } yield insertBetStatement(b, s)
    persistTask(generateBatchForStatements(x)).attemptRun match {
      case -\/(t) => println(t)
      case \/-(()) => println("persisted")
    }
  }

  def persistBet(s:SelectionBet, callback : (Throwable \/ Unit) => Unit): Unit = {
    persistTask(generateBatch(s)).runAsync(callback)
  }

}
