package com.pjb.sandbox

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

object AkkaKafkaStream4 extends App {

    implicit val system = ActorSystem("AkkaKafkaSandbox")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher
    val actor = system.actorOf(DummyActor.props)
    val service = new DummyService(actor, ec)

    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("192.168.99.100:9092")
          .withGroupId("akka-test-4")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
          .withBootstrapServers("192.168.99.100:9092")


    val stream = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._
        val in = Consumer.committableSource(consumerSettings, Subscriptions.topics("input"))
        val flow = Flow[CommittableMessage[Array[Byte], String]].mapAsync(1) { m => service.processPs(m) }
        val out = new ListSink(producerSettings)
        in ~> flow ~> out
        ClosedShape
    })
    stream.run()

    /*
    Consumer.committableSource(consumerSettings, Subscriptions.topics("input"))
          .mapAsync(1) { msg =>
              println(s"consume: $msg")
              service.processPs(msg)
          }.runWith(new ListSink(producerSettings))

    Consumer.committableSource(consumerSettings, Subscriptions.topics("input"))
          .mapAsync(1) { msg =>
              println(s"consume: $msg")
              service.processP(msg)
          }.runWith(Producer.commitableSink(producerSettings))
    */


}
