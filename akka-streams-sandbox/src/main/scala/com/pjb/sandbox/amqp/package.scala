package com.pjb.sandbox

package object amqp {
  case class AmqpSourceSettings(queue:String,
                                consumerTag:String,
                                ackOnPush:Boolean,
                                bufferSize: Int = 10,
                                noLocal:Boolean = false,
                                exclusive:Boolean = false,
                                arguments: Map[String, AnyRef] = Map.empty)
  case class Message(deliveryTag:Long, data:String)
}
