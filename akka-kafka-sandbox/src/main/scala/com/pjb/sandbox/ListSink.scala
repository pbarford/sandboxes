package com.pjb.sandbox

import akka.kafka.{ConsumerMessage, ProducerSettings}
import akka.kafka.ConsumerMessage.Committable
import akka.kafka.ProducerMessage.Message
import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.stage.{AbstractInHandler, GraphStage, GraphStageLogic}

class ListSink[K,V](settings: ProducerSettings[K, V]) extends GraphStage[SinkShape[List[Message[K, V, ConsumerMessage.Committable]]]]{
    val in:Inlet[List[Message[K, V, ConsumerMessage.Committable]]] = Inlet.create("SeqSink.in")

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
        new GraphStageLogic(shape) {
            val kafkaProducer = settings.createKafkaProducer()

            setHandler(in, new AbstractInHandler {
                override def onPush(): Unit = {
                    val elements: List[Message[K, V, Committable]] = grab(in)
                    elements.foreach { element =>
                        kafkaProducer.send(element.record)
                    }
                    println("commit")
                    elements.last.passThrough.commitScaladsl()
                    pull(in)
                }
            })

            override def preStart(): Unit = pull(in)
        }
    }

    override def shape: SinkShape[List[Message[K, V, Committable]]] = SinkShape.of(in)
}
