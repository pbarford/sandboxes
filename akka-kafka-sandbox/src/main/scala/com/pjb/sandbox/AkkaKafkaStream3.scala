package com.pjb.sandbox

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

object AkkaKafkaStream3 extends App {

    implicit val system = ActorSystem("AkkaKafkaSandbox")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher
    val actor = system.actorOf(DummyActor.props)
    val service = new DummyService(actor, ec)

    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("192.168.99.100:9092")
          .withGroupId("akka-test3")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics("input"))
          .mapAsync(1) { msg =>
              println(s"consume: $msg")
              service.process(msg)
          }.map(msg => msg.committableOffset.commitScaladsl())
          .runWith(Sink.ignore)
}
