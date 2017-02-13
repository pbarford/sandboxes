package com.pjb.sandbox

import akka.actor.{Actor, Props}

object DummyActor {
    def props:Props = Props(new DummyActor)
}
class DummyActor extends Actor {
    override def receive: Receive = {
        case m:Msg =>
            println(m.content)
            sender() ! Result(m.offset, Seq(s"processed-1 ${m.content.toUpperCase}", s"processed-2 ${m.content.toUpperCase}"))
    }
}
