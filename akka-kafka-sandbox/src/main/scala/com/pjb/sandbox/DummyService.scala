package com.pjb.sandbox

import akka.actor.ActorRef
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.kafka.ProducerMessage
import akka.kafka.ProducerMessage.Message
import akka.pattern.ask
import akka.util.Timeout
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class DummyService(actor: ActorRef, implicit val ec:ExecutionContext) {

    def process(record:ConsumerRecord[Array[Byte], String]):Future[Result] = {
        implicit val timeout = Timeout(5 seconds)
        ask(actor, Msg(offset= record.offset(), content = record.value())).mapTo[Result]
    }

    def process(msg:CommittableMessage[Array[Byte], String]):Future[CommittableMessage[Array[Byte], String]] = {
        implicit val timeout = Timeout(5 seconds)
        ask(actor, Msg(offset= msg.record.offset(),content = msg.record.value())).map(ack => msg)
    }

    def processPs(msg:CommittableMessage[Array[Byte], String]): Future[List[Message[Array[Byte], String, CommittableOffset]]] = {
        implicit val timeout = Timeout(5 seconds)
        ask(actor, Msg(offset= msg.record.offset(),content = msg.record.value())).mapTo[Result].map { result: Result =>
            result.content.map { s =>
                ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
                    "output",
                    msg.record.key(),
                    s
                ), msg.committableOffset)
            }.toList
        }
    }

    def processP(msg:CommittableMessage[Array[Byte], String]): Future[Message[Array[Byte], String, CommittableOffset]] = {
        implicit val timeout = Timeout(5 seconds)
        ask(actor, Msg(offset= msg.record.offset(),content = msg.record.value())).mapTo[Result].map { result: Result =>
            ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
                "output",
                msg.record.key(),
                result.content.head
            ), msg.committableOffset)
        }
    }
}
