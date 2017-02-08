package com.pjb.sandbox

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

object AkkaKafkaStream1 extends App {

    implicit val system = ActorSystem("AkkaKafkaSandbox")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    val actor = system.actorOf(DummyActor.props)
    val service = new DummyService(actor, ec)

    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("192.168.99.100:9092")
          .withGroupId("akka-test1")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val db = new InMemCommitLog(13)
    db.loadOffset().foreach { fromOffset =>
        val partition = 0
        val sub = Subscriptions.assignmentWithOffset(new TopicPartition("input", partition) -> fromOffset)

        Consumer.plainSource(consumerSettings, sub)
              .mapAsync(1)(db.save)
              .runWith(Sink.ignore)
    }


}
