package test

import com.rabbitmq.client.{ConnectionFactory, Connection}

import scala.util.Random
import scalaz.concurrent.Task
import scalaz.stream.Cause.{End, Terminated}
import scalaz.stream._
import scalaz.stream.Process._
import scalaz.stream.Process
import scalaz.stream.async.mutable.{Queue, Signal}

object QueueTest {

  case class AmqpMessage(no:Long, data:String, confirm: Boolean => Unit)

  def delayAndPrint2(name:String, q:async.mutable.Queue[Int]): Process[Any, (String, Int)] = for {
    n <- q.dequeue
    _ <- eval(Task.delay(Thread.sleep(Random.nextInt(3000))))
    _ <- emit(s"$name - ${Thread.currentThread}: $n") to io.stdOutLines
  } yield (name, n)


  def a(name:String, n:Int) = {
    Process.eval(Task.delay {
      println(s"$name - ${Thread.currentThread.getName}: value=$n")
      (name, n)
    })
  }

  def delayAndPrint(name:String, q:async.mutable.Queue[Int]): Process[Task, (String, Int)] = {
    val s: Process[Task, (String, Int)] = for {
      n <- q.dequeue
      p <- a(name,n)
    } yield p
    s
  }

  def printM(q:async.mutable.Queue[AmqpMessage]): Process[Task, Unit] = {
    for {
      m <- q.dequeue
      _ <- Process.emit(m.confirm(true))
    } yield ()
  }


  def enqueue(q1:async.mutable.Queue[Int],q2:async.mutable.Queue[Int],q3:async.mutable.Queue[Int]): Sink[Task, Int] = sink.lift[Task,Int] { i =>
    if(i < 100)
      q1.enqueueOne(i)
    else if (i < 200)
      q2.enqueueOne(i)
    else
      q3.enqueueOne(i)
  }

  def renqueue(qs:Seq[async.mutable.Queue[AmqpMessage]]): Sink[Task, AmqpMessage] = sink.lift[Task,AmqpMessage] { m =>
    m.no % qs.size match {
      case 0 =>
        println("enq 1")
        qs(0).enqueueOne(m)
      case 1 =>
        println("enq 2")
        qs(1).enqueueOne(m)
      case 2 =>
        println("enq 3")
        qs(2).enqueueOne(m)
      case 3 =>
        println("enq 4")
        qs(3).enqueueOne(m)
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

  val aq1 = async.boundedQueue[AmqpMessage](1000)
  val aq2 = async.boundedQueue[AmqpMessage](1000)
  val aq3 = async.boundedQueue[AmqpMessage](1000)

  def process(queueName:String)(implicit ch:com.rabbitmq.client.Channel):Process[Task, Unit]= {
    read(queueName).repeat to renqueue(Seq(aq1, aq2, aq3))
  }

  def main(args: Array[String]) {

    val q1 = async.boundedQueue[Int](1000)
    val q2 = async.boundedQueue[Int](1000)
    val q3 = async.boundedQueue[Int](1000)

    val sig:Signal[Queue[Int]] = async.signalOf[Queue[Int]](async.boundedQueue[Int](1000))
    val s: Process[Task,Queue[Int]] = sig.continuous

    val p1 = delayAndPrint("p1", q1)
    val p2 = delayAndPrint("p2", q2)
    val p3 = delayAndPrint("p3", q3)

    val pm1 = printM(aq1)
    val pm2 = printM(aq2)
    val pm3 = printM(aq3)

    val enqueueAll = merge.mergeN(300)(Process.range(1,300).map(Process.emit(_) to enqueue(q1,q2,q3)))
    enqueueAll.run.unsafePerformSync

    val delayAndPrintFlow = merge.mergeN(3)(Process(p1,p2,p3))

    val printMFlow = merge.mergeN(3)(Process(pm1,pm2,pm3))

    delayAndPrintFlow.run.unsafePerformAsync(_ => ())
    //delayAndPrintFlow.runLog.unsafePerformSync

    Thread.sleep(1000)

    implicit val ch = connect.createChannel()
    try {
      printMFlow.run.runAsync(_ => ())
      process("test").run.unsafePerformSync
    }
    finally {
      println("close")
      ch.close()
    }

  }
}
