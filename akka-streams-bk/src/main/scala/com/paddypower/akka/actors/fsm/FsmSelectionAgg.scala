package com.paddypower.akka.actors.fsm

import akka.actor.{Actor, ActorRef, FSM, Props, Stash}
import akka.cluster.sharding.ShardRegion
import com.paddypower.akka.actors.fsm.FsmSelectionAgg._
import com.paddypower.akka.domain.Model.{Bet, SelectionBet}
import com.paddypower.akka.persistence.Journal

import scalaz.{-\/, \/, \/-}

object FsmSelectionAgg {

  case class Done(betId:String)
  case object Complete
  case object Init

  case class PersistedBet(bet:Bet, sender:ActorRef)
  case class PersistedSelectionBet(selectionBet:SelectionBet, sender:ActorRef)

  case class Restored(noOfBets:Int, profit:Double, loss:Double, currency:String)

  sealed trait SelectionAggState
  case object Restoring extends SelectionAggState
  case object Running extends SelectionAggState

  sealed trait SelectionAggData
  case class Rollup(noOfBets:Int, profit:Double, loss:Double, currency:String) extends SelectionAggData

  val numberOfShards = 100
  val shardName = "FsmSelectionAgg"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg:SelectionBet => (msg.key, msg)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case msg:SelectionBet => math.abs(msg.key.hashCode % numberOfShards).toString
  }

  def props:Props = Props(new FsmSelectionAgg)
}

class FsmSelectionAgg extends Actor with Stash with FSM[SelectionAggState, SelectionAggData] {

  val journal = new Journal

  startWith(Restoring, Rollup(0, 0.0, 0.0, ""))

  when(Restoring) {
    case Event(r:Restored, _) =>
      println("moving to running")
      goto(Running) using Rollup(r.noOfBets, r.profit, r.loss, r.currency)
    case Event(_, _) => stash()
      stay()
  }

  when(Running) {
    case Event(s:SelectionBet, r:Rollup) =>
      println("persist")
      journal.persistBet(s, persistHandler(self ! PersistedSelectionBet(s, sender()), persistFailurePrint))
      stay()
    case Event(p:PersistedSelectionBet, r:Rollup) =>
      val update = Rollup(r.noOfBets + 1, formatD(r.profit + p.selectionBet.totalStake), formatD(r.loss + p.selectionBet.betLiability), r.currency)
      println(s"persistSuccess : state going from [ $r --> $update ]")
      stay() using update
  }

  onTransition {
    case _ -> Restoring =>
      println("restore")
      self ! Restored(10, 23.4, 124.1, "EUR")

    case Restoring -> Running =>
      println(s"running...")
      unstashAll()
  }

  initialize()

  def persistHandler(persistSuccessAction: => Unit, persistFailureAction: Throwable => Unit): (Throwable \/ Unit) => Unit = {
    case -\/(t) => persistFailureAction(t)
    case \/-(()) => persistSuccessAction
  }

  def persistFailurePrint : Throwable => Unit = t => println(s"persistFailure $t")

  def formatD: Double => Double = d => BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

}
