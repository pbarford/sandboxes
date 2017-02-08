package com.pjb.sandbox

import java.util.concurrent.atomic.AtomicLong

import akka.Done
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.concurrent.Future

class InMemCommitLog(initialOffSet:Long = 0) {

    private val offset = new AtomicLong(initialOffSet)

    def save(record: ConsumerRecord[Array[Byte], String]): Future[Done] = {
        println(s"InMemCommitLog.save: $record")
        offset.set(record.offset)
        Future.successful(Done)
    }

    def saveOffset(ack:Ack): Future[Done] = {
        println(s"InMemCommitLog.save: $ack")
        offset.set(ack.offset)
        Future.successful(Done)
    }

    def loadOffset(): Future[Long] =
        Future.successful(offset.get)


}
