package com.pjb.sandbox.amqp

import akka.NotUsed
import akka.stream.scaladsl.Sink
import akka.stream.stage.{AbstractInHandler, GraphStage, GraphStageLogic}
import akka.stream.{Attributes, Inlet, SinkShape}
import com.pjb.sandbox.amqp.AmqpAckSink.{AmqpAckSinkSettings, PublishAndAckMessage}
import com.rabbitmq.client.Channel

object AmqpAckSink {

  case class AmqpAckSinkSettings(exchange:String,
                                 routingKey:String)

  case class PublishAndAckMessage(ack: () => Unit,  data:String)

  def toSink(publishChannel:Channel, settings:AmqpAckSinkSettings): Sink[PublishAndAckMessage, NotUsed] =
    Sink.fromGraph(new AmqpAckSink(publishChannel, settings))
}

class AmqpAckSink(publishChannel:Channel, settings:AmqpAckSinkSettings) extends GraphStage[SinkShape[PublishAndAckMessage]] {

  val in:Inlet[PublishAndAckMessage] = Inlet.create("AmqpSink.out")

  override def shape: SinkShape[PublishAndAckMessage] = SinkShape.of(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new AbstractInHandler() {
        override def onPush(): Unit = {
          val msg:PublishAndAckMessage = grab(in)

          publishChannel.txSelect()
          publishChannel.basicPublish(
            settings.exchange,
            settings.routingKey,
            false,
            false,
            null,
            msg.data.getBytes
          )
          publishChannel.txCommit()
          msg.ack()
          pull(in)
        }
      })

      override def preStart(): Unit = {
        pull(in)
      }
    }
  }


}
