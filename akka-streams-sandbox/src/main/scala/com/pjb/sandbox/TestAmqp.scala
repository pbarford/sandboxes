package com.pjb.sandbox

import com.rabbitmq.client.ConnectionFactory

object TestAmqp extends App {

  val connectionFactory = {
    val cf = new ConnectionFactory()
    cf.setHost("localhost")
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf
  }
  val connection = connectionFactory.newConnection()
  val channel = connection.createChannel()

  for(i <- 1 to 10000)
    channel.basicPublish("inbound", "", null,  s"$i reee erere erere rerer rerere".getBytes)
  channel.close()
  connection.close()
}
