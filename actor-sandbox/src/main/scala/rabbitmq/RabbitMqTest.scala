package rabbitmq

import com.rabbitmq.client.{ConnectionFactory, Connection}
import scalaz.concurrent._
import scalaz.stream._
import scalaz.stream.Cause._
object RabbitMqTest {

  case class Message(deliveryTag:Long, data:String)

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

  def process(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit] =
      read(queueName).repeat map { m => println(m.data)
                        m } map { m => ch.basicAck(m.deliveryTag, false)
        }


  def main(args: Array[String]) {
    implicit val ch = connect.createChannel()


    try {
      process("test").run.unsafePerformSyncAttempt
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
