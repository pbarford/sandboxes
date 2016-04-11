package rabbitmq

import com.rabbitmq.client.{ConnectionFactory, Connection}
import scalaz.concurrent._
import scalaz.stream._
import scalaz.stream.Cause._
object RabbitMqTest {

  val q = async.unboundedQueue[Message2]

  case class Message(deliveryTag:Long, data:String)

  case class Message2(data:String, ack: () => Unit, nack: () => Unit)
  case class Message3(data:String, ack: Boolean => Unit)

  def connect:Connection = {
    val cf = new ConnectionFactory()
    cf.setHost("127.0.0.1")
    cf.setPort(5672)
    cf.setUsername("docker")
    cf.setPassword("docker")
    cf.newConnection()
  }

  def read(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Message] = {
    Process.repeatEval ( Task.delay {
      Option(ch.basicGet(queueName, false))
          .map(msg => Message(msg.getEnvelope.getDeliveryTag, new String(new String(msg.getBody, "UTF-8"))))
          .getOrElse(throw Terminated(End))

    })
  }



  def read2(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Message2] = {
    /*
    Process.repeatEval (
      q enqueueOne Option(ch.basicGet(queueName, false))
      .map(msg => Message2(new String(new String(msg.getBody, "UTF-8")), () => ack(ch, msg.getEnvelope.getDeliveryTag)))
      .getOrElse(throw Terminated(End))
    )
    */

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

  def nack(channel: com.rabbitmq.client.Channel, deliveryTag: Long): Unit =  {
    println(s"nacking [$deliveryTag]")
    channel.basicNack(deliveryTag, false, false)
  }

  /*
  def confirm(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit] = {
    Process.eval {
      m: Message => ch.basicAck(m.deliveryTag, false)
    }
  }
  */

  def process(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit] = {

    //read(queueName).zip(confirm)

    read(queueName).repeat map { m => println(m.data)
      m
    } map { m => ch.basicAck(m.deliveryTag, false)
    }
  }

  //val readFromQueue:Process[Task, Message2] = q.dequeue

  def process2(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit] = {
    read2(queueName).repeat map { m => println(m.data)
      m
    } map { m => m.ack()
    }

  }

  def process3(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit] = {
    read3(queueName).repeat map { m => println(m.data)
      m
    } map { m => m.ack(true)
    }

  }

  def main(args: Array[String]) {
    implicit val ch = connect.createChannel()
    try {
      process3("test").run.unsafePerformSyncAttempt
    }
    finally {
      ch.close()
    }

/*
    val msgs = try {
      read("test").chunkAll.runLast.run.getOrElse(Vector[Message]())
    }
    finally {
      ch.close()
    }

    for (msg <- msgs) {
      println(msg.data)
    }
*/
  }

}
