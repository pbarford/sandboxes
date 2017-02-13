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

    def saveResult(result:Result): Future[Result] = {
        println(s"InMemCommitLog.save: $result")
        offset.set(result.offset)
        Future.successful(result)
    }

    def loadOffset(): Future[Long] =
        Future.successful(offset.get)


}
