package com.pjb.sandbox

import akka.actor.ActorRef
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.pattern.ask
import akka.util.Timeout
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class DummyService(actor: ActorRef, implicit val ec:ExecutionContext) {

    def process(record:ConsumerRecord[Array[Byte], String]):Future[Ack] = {
        implicit val timeout = Timeout(5 seconds)
        ask(actor, Msg(offset= record.offset(),content = record.value())).mapTo[Ack]
    }

    def process(msg:CommittableMessage[Array[Byte], String]):Future[CommittableMessage[Array[Byte], String]] = {
        implicit val timeout = Timeout(5 seconds)
        ask(actor, Msg(offset= msg.record.offset(),content = msg.record.value())).map(ack => msg)
    }
}
