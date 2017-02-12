package com.pjb.sandbox.amqp

import akka.NotUsed
import akka.stream.scaladsl.Sink
import akka.stream.stage.{AbstractInHandler, GraphStage, GraphStageLogic}
import akka.stream.{Attributes, Inlet, SinkShape}
import com.pjb.sandbox.amqp.AmqpAckSink.{AmqpAckSinkSettings, PublishMessage}
import com.rabbitmq.client.Channel

object AmqpAckSink {

  case class AmqpAckSinkSettings(exchange:String,
                                 routingKey:String)

  case class PublishMessage(deliveryTag:Long, data:String)

  def toSink(ack:  Long => Unit, publishChannel:Channel, settings:AmqpAckSinkSettings): Sink[PublishMessage, NotUsed] =
    Sink.fromGraph(new AmqpAckSink(ack, publishChannel, settings))
}

class AmqpAckSink(ack:  Long => Unit, publishChannel:Channel, settings:AmqpAckSinkSettings) extends GraphStage[SinkShape[PublishMessage]] {

  val in:Inlet[PublishMessage] = Inlet.create("AmqpSink.out")

  override def shape: SinkShape[PublishMessage] = SinkShape.of(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new AbstractInHandler() {
        override def onPush(): Unit = {
          val element:PublishMessage = grab(in)

          publishChannel.txSelect()
          publishChannel.basicPublish(
            settings.exchange,
            settings.routingKey,
            false,
            false,
            null,
            element.data.getBytes
          )
          publishChannel.txCommit()
          ack(element.deliveryTag)
          pull(in)
        }
      })

      override def preStart(): Unit = {
        pull(in)
      }
    }
  }


}
