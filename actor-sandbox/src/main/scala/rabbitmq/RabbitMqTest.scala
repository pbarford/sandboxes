package rabbitmq

import com.rabbitmq.client.{ConnectionFactory, Connection}
import scalaz.concurrent._
import scalaz.stream._
import scalaz.stream.Cause._
object RabbitMqTest {

  val q = async.unboundedQueue[Message2]

  case class Message1(deliveryTag:Long, data:String)
  case class Message2(data:String, ack: () => Unit, nack: () => Unit)
  case class Message3(data:String, ack: Boolean => Unit)
  case class Message4(data:String, deliveryTag:Long, ch:com.rabbitmq.client.Channel)

  def connect:Connection = {
    val cf = new ConnectionFactory()
    cf.setHost("127.0.0.1")
    cf.setPort(5672)
    cf.setUsername("docker")
    cf.setPassword("docker")
    cf.newConnection()
  }

  def read1(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Message1] = {
    Process.repeatEval ( Task.delay {
      Option(ch.basicGet(queueName, false))
          .map(msg => Message1(msg.getEnvelope.getDeliveryTag, new String(new String(msg.getBody, "UTF-8"))))
          .getOrElse(throw Terminated(End))

    })
  }

  def read2(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Message2] = {

    Process.repeatEval ( Task.delay {
      Option(ch.basicGet(queueName, false))
        .map(msg => Message2(new String(new String(msg.getBody, "UTF-8")),
                            () => ack(ch, msg.getEnvelope.getDeliveryTag),
                            () => nack(ch, msg.getEnvelope.getDeliveryTag)))
        .getOrElse(throw Terminated(End))

    })

  }

  def read3(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Message3] = {
    Process.repeatEval ( Task.delay {
      Option(ch.basicGet(queueName, false))
        .map(msg => Message3(new String(new String(msg.getBody, "UTF-8")),
                             acknowledge => messageConfirm(acknowledge, ch, msg.getEnvelope.getDeliveryTag)))
        .getOrElse(throw Terminated(End))

    })
  }

  def read4(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Message4] = {
    Process.repeatEval ( Task.delay {
      Option(ch.basicGet(queueName, false))
        .map(msg => Message4(new String(new String(msg.getBody, "UTF-8")),
                              msg.getEnvelope.getDeliveryTag,
                              ch))
        .getOrElse(throw Terminated(End))
    })
  }

  def ack(channel: com.rabbitmq.client.Channel, deliveryTag: Long): Unit =  {
    println(s"acking [$deliveryTag]")
    channel.basicAck(deliveryTag, false)
  }

  def messageConfirm(acknowledge:Boolean, channel: com.rabbitmq.client.Channel, deliveryTag: Long): Unit =  {
    acknowledge match {
      case true =>
        println(s"acking [$deliveryTag]")
        channel.basicAck(deliveryTag, false)

      case false =>
        println(s"nacking [$deliveryTag]")
        channel.basicNack(deliveryTag, false, false)
    }
  }

  def outMessage1(m:Message4) : Task[Unit] = Task delay { println(m.data)}
  def outMessage2(m:Message3) : Task[Unit] = Task delay {
    println(m.data)
    m.ack(true)
  }

  def handleMessage(m:Message4) : Task[Unit] = Task delay {
    println(s"acking [${m.deliveryTag}]")
    m.ch.basicAck(m.deliveryTag, false)
  }

  def acknowledgeSink():Sink[Task, Message4] = Process.constant(handleMessage _)
  def outputSink():Sink[Task, Message4] = Process.constant(outMessage1 _)

  def outputSink2():Sink[Task, Message3] = Process.constant(outMessage2)

  def nack(channel: com.rabbitmq.client.Channel, deliveryTag: Long): Unit =  {
    println(s"nacking [$deliveryTag]")
    channel.basicNack(deliveryTag, false, false)
  }

  def process1(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit] = {

    read1(queueName).repeat map {
      m => println(m.data)
      m
    } map {
      m => ch.basicAck(m.deliveryTag, false)
    }
  }

  def process2(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit] = {
    read2(queueName).repeat map {
      m => println(m.data)
      m
    } map {
      m => m.ack()
    }

  }

  def process3(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit] = {
    read3(queueName).repeat map {
      m => println(m.data)
      m
    } map {
      m => m.ack(true)
    }
  }

  def process3a(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit]= {
    read3(queueName).repeat to outputSink2
  }

  def process4(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit]= {

    read4(queueName).repeat observe outputSink to acknowledgeSink
  }

  def main(args: Array[String]) {
    implicit val ch = connect.createChannel()
    try {
      process3a("test").run.unsafePerformSyncAttempt
    }
    finally {
      ch.close()
    }
  }

}
