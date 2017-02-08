package test.streams.akka.fsm

import akka.actor.{Actor, FSM, Stash}
import test.streams.akka.fsm.FsmMultiBetMonitor.{MultiBetMonitorData, MultiBetMonitorState}

object FsmMultiBetMonitor {
  sealed trait MultiBetMonitorState

  sealed trait MultiBetMonitorData
}

class FsmMultiBetMonitor extends Actor with Stash with FSM[MultiBetMonitorState, MultiBetMonitorData] {
  override def receive: Receive = {
    ???
  }
}
