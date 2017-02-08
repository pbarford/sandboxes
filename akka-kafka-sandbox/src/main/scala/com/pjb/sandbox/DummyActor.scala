package com.pjb.sandbox

import akka.actor.{Actor, Props}

object DummyActor {
    def props:Props = Props(new DummyActor)
}
class DummyActor extends Actor {
    override def receive: Receive = {
        case m:Msg => println(m.content)
            sender() ! Ack(m.offset)
    }
}
