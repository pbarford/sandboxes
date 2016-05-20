package test

import com.rabbitmq.client._
import test.RabbitQTest.AmqpMessage

import scalaz.concurrent.Task
import scalaz.stream.Cause.{End, Terminated}
import scalaz.stream.Process._
import scalaz.stream.Process
import scalaz.stream._

object RabbitQTest {

  case class AmqpMessage(no:Long, data:String)

  val q1 = async.boundedQueue[AmqpMessage](1000)
  val q2 = async.boundedQueue[AmqpMessage](1000)
  val q3 = async.boundedQueue[AmqpMessage](1000)

  def processQ(q:async.mutable.Queue[AmqpMessage])(implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    for {
      m <- q.dequeue
      _ <- emit(m).toSource to ack
    } yield ()
  }

  def enqueue(qs:Seq[async.mutable.Queue[AmqpMessage]]): Sink[Task, AmqpMessage] = sink.lift[Task,AmqpMessage] { m =>
    m.no.toInt % qs.size match {
      case i:Int =>
        println(s"enqueue ${m.no} to $i")
        qs(i).enqueueOne(m)
    }
  }

  def ack(implicit ch:com.rabbitmq.client.Channel):Sink[Task, AmqpMessage] =
    sink.lift[Task,AmqpMessage] { m:AmqpMessage =>
      Task.delay {
        println(s"acking ${m.no}")
        ch.basicAck(m.no, false)
      }
    }

  def connect:Connection = {
    val cf = new ConnectionFactory()
    cf.setHost("127.0.0.1")
    cf.setPort(5672)
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf.newConnection()
  }

  def qMsg(m:AmqpMessage) = {
    Process.eval(Task.now(m)) to enqueue(Seq(q1,q2,q3))
  }

  def consume(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    Process.eval ( Task.delay {
      ch.basicConsume(queueName, false, new RabbitConsumer(m => qMsg(m).run.unsafePerformSync))
      ()
    })
  }

  def addMessages(implicit ch: com.rabbitmq.client.Channel) = {
    for (i <- 1 to 1000)
      ch.basicPublish("testFo", "", null, "etete".getBytes)
  }

  def main(args: Array[String]) {

    implicit val ch = connect.createChannel()
    val p1 = processQ(q1)
    val p2 = processQ(q2)
    val p3 = processQ(q3)
    val processes = merge.mergeN(3)(Process(p1,p2,p3))

    ch.basicQos(20)
    processes.run.runAsync(_ => ())
    addMessages
    consume("test").run.runAsync(_ => ())

  }
}

class RabbitConsumer(callback : AmqpMessage => Unit)
                    (implicit ch:com.rabbitmq.client.Channel) extends Consumer {

  override def handleCancel(consumerTag: String): Unit = ()
  override def handleRecoverOk(consumerTag: String): Unit = ()
  override def handleCancelOk(consumerTag: String): Unit = ()
  override def handleConsumeOk(consumerTag: String): Unit = ()
  override def handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException): Unit = throw Terminated(End)
  override def handleDelivery(consumerTag: String,
                              envelope: Envelope,
                              properties: AMQP.BasicProperties,
                              body: Array[Byte]): Unit = {
    callback(AmqpMessage(envelope.getDeliveryTag,
                        new String(new String(body, "UTF-8"))))
  }
}
