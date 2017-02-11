package com.pjb.sandbox

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.pjb.sandbox.amqp.AmqpAckSink.{AmqpAckSinkSettings, PublishMessage}
import com.pjb.sandbox.amqp.{AmqpAckSink, AmqpSource}
import com.pjb.sandbox.amqp.AmqpSource.AmqpSourceSettings
import com.rabbitmq.client.{Channel, ConnectionFactory}

object AmqpStreamTest extends App {

  implicit val system = ActorSystem("StreamTestSandbox")
  implicit val materializer = ActorMaterializer()

  val connectionFactory = {
    val cf = new ConnectionFactory()
    cf.setHost("localhost")
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf
  }

  val connection = connectionFactory.newConnection()
  val inChannel:Channel = connection.createChannel()
  val outChannel:Channel = connection.createChannel()
  val amqpSourceSettings:AmqpSourceSettings = AmqpSourceSettings("inbound-q", "stream-test", false)
  val amqpSinkSettings:AmqpAckSinkSettings = AmqpAckSinkSettings("outbound", "")

  def ackMessage(tag:Long) = inChannel.basicAck(tag, false)

  AmqpSource.toSource(inChannel, amqpSourceSettings)
    .map { msg =>
      println(msg)
      PublishMessage(msg.deliveryTag, msg.data.toUpperCase)
    }.runWith(AmqpAckSink.toSink(ackMessage, outChannel, amqpSinkSettings))

}
