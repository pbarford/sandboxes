package test

import com.rabbitmq.client._
import test.RabbitQTest.AmqpMessage

import scalaz.concurrent.Task
import scalaz.stream.Cause.{End, Terminated}
import scalaz.stream.Process._
import scalaz.stream.Process
import scalaz.stream._

object RabbitQTest {

  case class AmqpMessage(no:Long, data:String, confirm: Boolean => Unit)

  val q1 = async.boundedQueue[AmqpMessage](1000)
  val q2 = async.boundedQueue[AmqpMessage](1000)
  val q3 = async.boundedQueue[AmqpMessage](1000)

  def processQ(q:async.mutable.Queue[AmqpMessage]): Process[Task, Unit] = {
    for {
      m <- q.dequeue
      _ <- emit(m.confirm(true))
    } yield ()
  }

  def enqueue(qs:Seq[async.mutable.Queue[AmqpMessage]]): Sink[Task, AmqpMessage] = sink.lift[Task,AmqpMessage] { m =>
    m.no.toInt % qs.size match {
      case i:Int =>
        println(s"adding ${m.no} to $i")
        qs(i).enqueueOne(m)
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

  def handleConfirm(acknowledge:Boolean, channel: com.rabbitmq.client.Channel, deliveryTag: Long): Unit =  {
    acknowledge match {
      case true =>
        println(s"${Thread.currentThread().getName} acking [$deliveryTag]")
        channel.basicAck(deliveryTag, false)

      case false =>
        println(s"${Thread.currentThread().getName} nacking [$deliveryTag]")
        channel.basicNack(deliveryTag, false, false)
    }
  }

  def read(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, AmqpMessage] = {
    Process.repeatEval ( Task.delay {
      Option(ch.basicGet(queueName, false))
        .map(msg => AmqpMessage(msg.getEnvelope.getDeliveryTag,
          new String(new String(msg.getBody, "UTF-8")),
          confirm => handleConfirm(confirm, ch, msg.getEnvelope.getDeliveryTag)))
        .getOrElse(throw Terminated(End))
    })
  }

  def q(m:AmqpMessage) = {
    Process.eval(Task.now(m)) to enqueue(Seq(q1,q2,q3))
  }

  def read2(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    Process.eval ( Task.delay {
      ch.basicConsume(queueName, false, new RabbitConsumer(m => q(m).run.unsafePerformSync))
      ()
    })
  }

  def process(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit]= {
    read(queueName).repeat to enqueue(Seq(q1, q2, q3))
  }

  def process2(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit]= {
    read2(queueName)
  }

  def main(args: Array[String]) {

    val p1 = processQ(q1)
    val p2 = processQ(q2)
    val p3 = processQ(q3)
    val printMFlow = merge.mergeN(3)(Process(p1,p2,p3))

    implicit val ch = connect.createChannel()
    ch.basicQos(20)
    printMFlow.run.runAsync(_ => ())
    add
    process2("test").run.runAsync(_ => ())
    
  }

  def add(implicit ch: com.rabbitmq.client.Channel) = {
    for (i <- 1 to 1000)
      ch.basicPublish("testFo", "", null, "etete".getBytes)
  }
}

class RabbitConsumer(callback : AmqpMessage => Unit)
                    (implicit ch:com.rabbitmq.client.Channel) extends Consumer {

  def handler(acknowledge:Boolean, deliveryTag: Long): Unit =  {
    acknowledge match {
      case true =>
        println(s"${Thread.currentThread().getName} acking [$deliveryTag]")
        ch.basicAck(deliveryTag, false)

      case false =>
        println(s"${Thread.currentThread().getName} nacking [$deliveryTag]")
        ch.basicNack(deliveryTag, false, false)
    }
  }

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
                        new String(new String(body, "UTF-8")),
                        confirm => handler(confirm, envelope.getDeliveryTag)))
  }

}
