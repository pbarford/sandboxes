package test

import java.util.concurrent.Executors

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.rabbitmq.client._
import test.RabbitQTest.AmqpMessage

import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.Cause.{End, Terminated}
import scalaz.stream.Process._
import scalaz.stream.Process
import scalaz.stream._

object RabbitQTest {

  import ProcessExtras._

  case class AmqpMessage(no:Long, data:String)

  val q1 = async.boundedQueue[AmqpMessage](10000)
  val q2 = async.boundedQueue[AmqpMessage](10000)
  val q3 = async.boundedQueue[AmqpMessage](10000)
  val q4 = async.boundedQueue[AmqpMessage](10000)
  val q5 = async.boundedQueue[AmqpMessage](10000)
  val q6 = async.boundedQueue[AmqpMessage](10000)
  val q7 = async.boundedQueue[AmqpMessage](10000)
  val q8 = async.boundedQueue[AmqpMessage](10000)
  val q9 = async.boundedQueue[AmqpMessage](10000)
  val q10 = async.boundedQueue[AmqpMessage](10000)

  def processQ(q:async.mutable.Queue[AmqpMessage])(implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    for {
      msg <- q.dequeue
      validMsg <- checkMsg(msg) or logErrorAndAck(msg.no)
      _ <- emit(validMsg).toSource to ack
    } yield ()
  }

  def logErrorAndAck(tag:Long)(implicit ch:com.rabbitmq.client.Channel): String => Process[Task, Nothing] =
    msg => {
      println(s"${Thread.currentThread().getName} logErrorAndAck [$msg] acking [${tag}]")
      emit(ch.basicAck(tag, false)).drain
  }

  def checkMsg: AmqpMessage => Process[Task, String \/ AmqpMessage] = msg => {
    msg.no % 10 match {
      case 0 => Process.eval(Task.now(-\/("divisible by 10 error")))
      case _ => Process.eval(Task.now(\/-(msg)))
    }
  }

  def enqueue(qs:Seq[async.mutable.Queue[AmqpMessage]]): Sink[Task, AmqpMessage] = sink.lift[Task,AmqpMessage] { m =>
    m.no.toInt % qs.size match {
      case i: Int => qs(i).enqueueOne(m)
    }
  }

  def ack(implicit ch:com.rabbitmq.client.Channel):Sink[Task, AmqpMessage] =
    sink.lift[Task,AmqpMessage] { m:AmqpMessage =>
      Task.delay {
        println(s"${Thread.currentThread().getName} acking ${m.no}")
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
    Process.eval(Task.now(m)) to enqueue(Seq(q1,q2,q3,q4,q5,q6,q7,q8,q9,q10))
  }

  def consume(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    Process.eval ( Task.delay {
      ch.basicConsume(queueName, false, new RabbitConsumer(m => qMsg(m).run.attemptRun))
      ()
    })
  }

  def addMessages(implicit ch: com.rabbitmq.client.Channel):Process[Task,Unit] = {
    for {
      i <- Process.range(1, 40000)
      _ <- emit(ch.basicPublish("testFo", "", null, "etete".getBytes))

    } yield ()
  }

  def main(args: Array[String]) {
    implicit val ch = connect.createChannel()
    ch.basicQos(10000)
    val tf = new ThreadFactoryBuilder().setNameFormat("rmq-thread-%d").build()
    val pool = Executors.newFixedThreadPool(30, tf)
    implicit val pS = Strategy.Executor(pool)
    val processes = merge.mergeN(30)(Process(addMessages,
                                             consume("test"),
                                             processQ(q1),
                                             processQ(q2),
                                             processQ(q3),
                                             processQ(q4),
                                             processQ(q5),
                                             processQ(q6),
                                             processQ(q7),
                                             processQ(q8),
                                             processQ(q9),
                                             processQ(q10)))
    processes.run.runAsync(_ => ())
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
