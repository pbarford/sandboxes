package com.pjb.sandbox

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

object TestConsumer extends App {

    implicit val system = ActorSystem("AkkaKafkaConsumer")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("192.168.99.100:9092")
          .withGroupId("akka-test-consumer-output")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics("output"))
          .mapAsync(1) { (msg: CommittableMessage[Array[Byte], String]) =>
              println(s"output-consume: $msg")
              msg.committableOffset.commitScaladsl()
          }.runWith(Sink.ignore)


}
