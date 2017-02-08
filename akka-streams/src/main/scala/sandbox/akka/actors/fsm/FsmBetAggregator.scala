package sandbox.akka.actors.fsm

import akka.actor.{Actor, ActorRef, FSM, Props, Stash}
import akka.cluster.sharding.ShardRegion
import sandbox.akka.actors.fsm.FsmBetAggregator._
import sandbox.akka.domain.Model.{Bet, SelectionBet}
import sandbox.akka.domain.ModelUtil
import sandbox.akka.persistence.Journal

import scalaz.{-\/, \/, \/-}

object FsmBetAggregator {

  case class Done(betId:String)
  case object Complete
  case object Init

  case class PersistedBet(bet:Bet, sender:ActorRef)
  case class PersistedSelectionBet(selectionBet:SelectionBet, sender:ActorRef)

  case class Restored(key:String, multiBet:Boolean, noOfBets:Int, profit:Double, loss:Double, currency:String)

  sealed trait SelectionAggState
  case object Restoring extends SelectionAggState
  case object Running extends SelectionAggState

  sealed trait SelectionAggData
  case class Rollup(key:String, multiBet:Boolean, noOfBets:Int, profit:Double, loss:Double, currency:String) extends SelectionAggData

  val numberOfShards = 100
  val shardName = "FsmBetAggregator"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg:SelectionBet => (msg.key, msg)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case msg:SelectionBet => math.abs(msg.key.hashCode % numberOfShards).toString
  }

  def props(journal:Journal):Props = Props(new FsmBetAggregator(journal))
}

class FsmBetAggregator(journal:Journal) extends Actor with Stash with FSM[SelectionAggState, SelectionAggData] {

  startWith(Restoring, Rollup("", multiBet = false, 0, 0.0, 0.0, ""))

  when(Restoring) {
    case Event(r:Restored, _) =>
      println("moving to running")
      goto(Running) using Rollup(r.key, r.multiBet, r.noOfBets, r.profit, r.loss, r.currency)
    case Event(_, _) => stash()
      stay()
  }

  when(Running) {
    case Event(s:SelectionBet, r:Rollup) =>
      println(s"persist [${s.betID}-${s.key}]")
      journal.persistBet(s, persistHandler(self ! PersistedSelectionBet(s, sender()), persistFailurePrint))
      stay()
    case Event(p:PersistedSelectionBet, r:Rollup) =>
      val update = Rollup(p.selectionBet.key, p.selectionBet.multi, r.noOfBets + 1, ModelUtil.formatD(r.profit + p.selectionBet.stake), ModelUtil.formatD(r.loss + p.selectionBet.betLiability), r.currency)
      println(s"persistSuccess : state going from [ $r --> $update ]")
      stay() using update
  }

  onTransition {
    case _ -> Restoring =>
      self ! Restored("", multiBet = false, 0, 0.0, 0.0, "")

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

}
