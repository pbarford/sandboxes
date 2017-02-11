package com.pjb.sandbox.amqp

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.stage.{AbstractOutHandler, GraphStage, GraphStageLogic}
import akka.stream.{Attributes, Outlet, SourceShape}
import com.pjb.sandbox.amqp.AmqpSource.{AmqpSourceSettings, Message}
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client._

import scala.collection.mutable

object AmqpSource {
  case class AmqpSourceSettings(queue:String,
                            consumerTag:String,
                            ackOnPush:Boolean,
                            bufferSize: Int = 10,
                            noLocal:Boolean = false,
                            exclusive:Boolean = false,
                            arguments: Map[String, AnyRef] = Map.empty)
  case class Message(deliveryTag:Long, data:String)

  def toSource(channel: Channel, settings: AmqpSourceSettings): Source[Message, NotUsed] =
    Source.fromGraph(new AmqpSource(channel, settings))
}

class AmqpSource(channel:Channel, settings:AmqpSourceSettings) extends GraphStage[SourceShape[Message]] {

  val out:Outlet[Message] = Outlet.create("AmqpSource.in")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      private val queue = mutable.Queue[Message]()


      override def preStart(): Unit = {
        init()
      }

      def init() = {
        println("init")
        val consumerCallback = getAsyncCallback(handleDelivery)
        val shutdownCallback = getAsyncCallback[Option[ShutdownSignalException]] {
          case Some(ex) => failStage(ex)
          case None => completeStage()
        }

        val consumer = new DefaultConsumer(channel) {
          override def handleCancel(consumerTag: String): Unit = {
            shutdownCallback.invoke(None)
          }

          override def handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException): Unit = {
            shutdownCallback.invoke(Option(sig))
          }

          override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]): Unit = {
            consumerCallback.invoke(Message(envelope.getDeliveryTag, new String(body)))
          }
        }

        import scala.collection.JavaConverters._
        channel.basicConsume(
          settings.queue,
          false,
          settings.consumerTag,
          settings.noLocal,
          settings.exclusive,
          settings.arguments.asJava,
          consumer
        )
      }

      setHandler(out, new AbstractOutHandler {
        override def onPull() = {
          if (queue.nonEmpty) {
            pushMessage(queue.dequeue())
          }
        }
      })


      def handleDelivery(message: Message): Unit =
      if (isAvailable(out)) {
          pushMessage(message)
      } else {
        println("queuing")
        if (queue.size + 1 > settings.bufferSize) {
          failStage(new RuntimeException(s"Reached maximum buffer size ${settings.bufferSize}"))
        } else {
          queue.enqueue(message)
        }
      }

      def pushMessage(message: Message): Unit = {
        push(out, message)
        if(settings.ackOnPush) channel.basicAck(message.deliveryTag, false)
      }
    }
  }

  override def shape: SourceShape[Message] = SourceShape.of(out)
}
