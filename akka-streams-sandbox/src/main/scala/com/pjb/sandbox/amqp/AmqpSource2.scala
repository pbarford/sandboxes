package com.pjb.sandbox.amqp

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{AbstractOutHandler, AsyncCallback, GraphStage, GraphStageLogic}
import com.rabbitmq.client._

object AmqpSource2 {
  def toSource(channel: Channel, settings: AmqpSourceSettings): Source[Message, NotUsed] =
    Source.fromGraph(new AmqpSource2(channel, settings))
}

class AmqpSource2 (channel:Channel, settings:AmqpSourceSettings) extends GraphStage[SourceShape[Message]] {

  val out:Outlet[Message] = Outlet.create("AmqpSource2.in")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      val consumerCallback: AsyncCallback[Message] = getAsyncCallback(handleDelivery)
      val shutdownCallback: AsyncCallback[Option[ShutdownSignalException]] = getAsyncCallback[Option[ShutdownSignalException]] {
        case Some(ex) => failStage(ex)
        case None => completeStage()
      }
      override def preStart(): Unit = {
        init()
      }

      def init():Unit = {
        println("init")
        channel.addShutdownListener(new ShutdownListener {
          override def shutdownCompleted(cause: ShutdownSignalException) = shutdownCallback.invoke(Some(cause))
        })
      }

      setHandler(out, new AbstractOutHandler {
        override def onPull(): Unit = {
          if(isAvailable(out)) {
            try {
              val msg = channel.basicGet(settings.queue, settings.ackOnPush)
              consumerCallback.invoke(Message(msg.getEnvelope.getDeliveryTag, new String(msg.getBody)))
            }
            catch {
              case npe: NullPointerException => onPull()
            }
          } else onPull()
        }
      })


      def handleDelivery(message: Message): Unit = push(out, message)
    }
  }

  override def shape: SourceShape[Message] = SourceShape.of(out)
}
