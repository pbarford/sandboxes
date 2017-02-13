package com.pjb.sandbox

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.pjb.sandbox.amqp.AmqpAckSink.{AmqpAckSinkSettings, PublishAndAckMessage}
import com.pjb.sandbox.amqp.{AmqpAckSink, AmqpSource}
import com.pjb.sandbox.amqp.AmqpSource.AmqpSourceSettings
import com.rabbitmq.client.{Channel, ConnectionFactory}

import scala.concurrent.Future

object AkkaAmqpStream extends App {

    implicit val system = ActorSystem("AkkaAmqpStream")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher
    val connectionFactory = {
        val cf = new ConnectionFactory()
        cf.setHost("192.168.99.100")
        cf.setUsername("guest")
        cf.setPassword("guest")
        cf
    }

    val connection = connectionFactory.newConnection()
    val inChannel:Channel = connection.createChannel()
    val outChannel:Channel = connection.createChannel()
    val amqpSourceSettings:AmqpSourceSettings = AmqpSourceSettings("inbound-q", "stream-test", ackOnPush = false)
    val amqpSinkSettings:AmqpAckSinkSettings = AmqpAckSinkSettings("outbound", "")

    def ackMessage : Long => Unit = (tag) => inChannel.basicAck(tag, false)

    AmqpSource.toSource(inChannel, amqpSourceSettings)
          .mapAsync(1) { msg =>
              println(s"${Thread.currentThread().getName} - $msg")
              Future(PublishAndAckMessage(() => ackMessage(msg.deliveryTag) , msg.data.toUpperCase))
          }.runWith(AmqpAckSink.toSink(outChannel, amqpSinkSettings))

}
