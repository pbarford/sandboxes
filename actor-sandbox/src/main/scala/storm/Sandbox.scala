package storm

import com.rabbitmq.client.Channel
import org.apache.storm.spout.SpoutOutputCollector

import scala.collection.mutable

object FsmState {
  case class State[S , D](stateName: S, stateData: D) {
    def using(nextStateData: D): FsmState.State[S, D] = {
      copy(stateData = nextStateData)
    }
    def copy(stateName: S = stateName, stateData: D = stateData): FsmState.State[S, D] = {
      new FsmState.State(stateName, stateData)
    }
  }
  case class Event[D](data: D)
}

abstract class FsmState[S, D] {

  type State = FsmState.State[S, D]
  type Event = FsmState.Event[D]
  type StateFunction = PartialFunction[Event, State]
  type TransitionHandler = PartialFunction[(S, S), Unit]

  final def when(stateName: S)(stateF: StateFunction): Unit = register(stateName, stateF)

  final def startWith(stateName: S, stateData: D): Unit = currentState = FsmState.State(stateName, stateData)

  final def goto(nextStateName: S): State = applyState(FsmState.State(nextStateName, currentState.stateData))

  final def stay(): State = goto(currentState.stateName)


  private var currentState: State = _
  private var nextState: State = _
  private val stateFunctions = mutable.Map[S, StateFunction]()

  private def register(name: S, function: StateFunction): Unit = {
    if (stateFunctions contains name) {
      stateFunctions(name) = stateFunctions(name) orElse function
    } else {
      println(s"register [$name] [$function]")
      stateFunctions(name) = function
    }
  }

  final def initialize(): Unit = if (currentState != null) makeTransition(currentState)


  private def applyState(nextState: State): State = {
    makeTransition(nextState)
    nextState
  }

  private def makeTransition(nextState: State): Unit = {
    if (stateFunctions.contains(nextState.stateName)) {
      if (currentState.stateName != nextState.stateName) {
        this.nextState = nextState
        handleTransition(currentState.stateName, nextState.stateName)
      }
    }
  }

  final class TransformHelper(func: StateFunction) {
    def using(andThen: PartialFunction[State, State]): StateFunction =
      func andThen (andThen orElse { case x ⇒ x })
  }


  final def onTransition(transitionHandler: TransitionHandler): Unit = transitionEvent :+= transitionHandler

  implicit final def total2pf(transitionHandler: (S, S) ⇒ Unit): TransitionHandler =
    new TransitionHandler {
      def isDefinedAt(in: (S, S)) = true
      def apply(in: (S, S)) { transitionHandler(in._1, in._2) }
    }

  final def transform(func: StateFunction): TransformHelper = new TransformHelper(func)

  private var transitionEvent: List[TransitionHandler] = Nil

  private def handleTransition(prev: S, next: S) {
    val tuple = (prev, next)
    println(s"handleTransition [$tuple]")
    stateFunctions(next)

    for (te <- transitionEvent) { if (te.isDefinedAt(tuple)) te(tuple) }
  }
}

sealed trait State
case object Uninitialised extends State
case object Initialised extends State

sealed trait SpoutData
case object Non extends SpoutData
case class Initialise(s:String) extends SpoutData
case class Open(channel:Channel, collector:SpoutOutputCollector)

class SpoutFsm(streamId:String) extends FsmState[State, SpoutData] {

  when(Uninitialised) {
    case FsmState.Event(m:Initialise) =>
      println("ok")
      goto(Initialised) using(m)
  }

  when(Initialised) {
    case FsmState.Event(Initialise(_)) => println("ok")
      stay
    case _ => println("anon")
      ???
  }

  initialize
  startWith(Uninitialised, Non)
  goto(Initialised) using Initialise("s")



}

object Sandbox extends App {

  val s = new SpoutFsm("test")
  //s.become(init(Init("s")))

}