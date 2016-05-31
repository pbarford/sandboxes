package test

import java.util.concurrent.Executors

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.rabbitmq.client.{AMQP, _}
import test.SignalTest.{FeedMessage, Incident}

import scala.concurrent.duration._
import scala.collection.immutable.SortedSet
import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.Cause.{End, Terminated}
import scalaz.stream._
import scalaz.stream.Process._
import scalaz.stream.time._
import scalaz.stream.async.mutable.Signal
import scalaz.stream.async.signalOf

object SignalTest {

  import ProcessExtras._
  case class FeedMessage(deliveryTag:Long, incident:Incident)

  case class Incident(eventId:Long, seqNo : Long, action:String) extends Ordered[Incident] {
    override def compare(that: Incident): Int = {
      if (this.seqNo == that.seqNo) 0
      else if (this.seqNo > that.seqNo) 1
      else -1
    }
  }

  case class GameState(eventId:Long, incidents: SortedSet[Incident])

  case class PricingTick(eventId:Long, feedSeqNo:Long, tickNo:Int, tickTime:Long) extends Ordered[PricingTick] {
    override def compare(that: PricingTick): Int ={
      if(this.tickTime == that.tickTime) 0
      else if(this.tickTime > that.tickTime) 1
      else -1
    }
  }

  case class PricingCall(eventId:Long, feedSeqNo:Long, gameTime:Long, timeOfCall:Long) extends Ordered[PricingCall] {
    override def compare(that: PricingCall): Int ={
      if(this.timeOfCall == that.timeOfCall) 0
      else if(this.timeOfCall > that.timeOfCall) 1
      else -1
    }
  }
  val tf = new ThreadFactoryBuilder().setNameFormat("rmq-thread-%d").build()
  val pool = Executors.newFixedThreadPool(10, tf)

  implicit val pS = Strategy.Executor(pool)
  implicit val sc = Executors.newScheduledThreadPool(1)

  val pricingCalls:Signal[Map[Long,PricingCall]] = signalOf[Map[Long,PricingCall]](Map.empty)
  val updatedPricingCalls: Process[Task, Map[Long, PricingCall]] = pricingCalls.discrete

  implicit class MapOps[Long,PricingCall](m :Map[Long, SortedSet[PricingCall]]) {
    def lastPricingCallFor(id:Long) = m.get(id).map(set => set.last)
  }

  val q1 = async.boundedQueue[FeedMessage](10000)

  def connect:Connection = {
    val cf = new ConnectionFactory()
    cf.setHost("127.0.0.1")
    cf.setPort(5672)
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf.newConnection()
  }

  def enqueue(qs:Seq[async.mutable.Queue[FeedMessage]]): Sink[Task, FeedMessage] = sink.lift[Task,FeedMessage] { m =>
    m.deliveryTag.toInt % qs.size match {
      case i: Int => qs(i).enqueueOne(m)
    }
  }

  def enqueueToQ(q:async.mutable.Queue[FeedMessage]): Sink[Task, FeedMessage] = sink.lift[Task,FeedMessage] { m =>
    q.enqueueOne(m)
  }

  def queueFeedMessage(m:FeedMessage) = {
    Process.eval(Task.now(m)) to enqueue(Seq(q1))
  }

  def consume(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    Process.eval ( Task.delay {
      ch.basicConsume(queueName, false, new FeedConsumer(m => queueFeedMessage(m).run.unsafePerformSync))
      ()
    })
  }

  def callPricing: FeedMessage => Process[Task, String \/ PricingCall] =
    (msg) => {
      println(s"${Thread.currentThread().getName} callPricing [$msg]")
      msg.deliveryTag % 2 match {
      case 0 =>
        val pc = PricingCall(msg.incident.eventId, msg.incident.seqNo,  System.currentTimeMillis(), System.currentTimeMillis())
        emit(\/-(pc)).toSource

      case _ => Process.eval(Task.now(-\/(s"($msg) skip call to pricing")))
    }
  }

  def ack(implicit ch:com.rabbitmq.client.Channel):Sink[Task, FeedMessage] =
    sink.lift[Task,FeedMessage] { m:FeedMessage =>
      Task.delay {
        if(m.deliveryTag !=0) {
          println(s"${Thread.currentThread().getName} ack [${m.deliveryTag}]")
          ch.basicAck(m.deliveryTag, false)
        }
      }
    }

  def logMsgAndAck(tag:Long)(implicit ch:com.rabbitmq.client.Channel): String => Process[Task, Nothing] =
    msg => {
      println(s"${Thread.currentThread().getName} logMsgAndAck [$msg] ack [${tag}]")
      emit(ch.basicAck(tag, false)).drain
    }

  def log[A]:Sink[Task,A] = sink.lift[Task,A] { msg =>
    Task.delay { println(s"${Thread.currentThread().getName} log [$msg]") }
  }

  def setPricingCall(pricingCall: PricingCall) = {

    pricingCalls.compareAndSet { m =>
      println(s"${Thread.currentThread().getName} setPricingCall [$pricingCall]")
      Some(m.get + (pricingCall.eventId -> pricingCall))
    }
  }

  def processQ(q:async.mutable.Queue[FeedMessage])
             (implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    for {
      feedMessage     <- q.dequeue
      pricingCall     <- callPricing(feedMessage) or logMsgAndAck(feedMessage.deliveryTag)
      _               <- eval(setPricingCall(pricingCall))
      _               <- eval(Task.delay(feedMessage)) to ack
    } yield ()
  }

  def main(args: Array[String]) {

    implicit val ch = connect.createChannel()
    ch.basicQos(100)

    val m = Map[Long, SortedSet[PricingCall]](1L -> SortedSet(PricingCall(1L, 1L, 1L, 1L), PricingCall(1L, 1L, 1L, 2L)))
    println(m.lastPricingCallFor(1L))
    println(m.lastPricingCallFor(2L))

    val processes = merge.mergeN(30)(Process(consume("test"),
                                     processQ(q1),
                                     checker
      ))

    processes.run.runAsync(_ => ())

    awakeEvery(5 seconds).map(x => generateTick(x).run.unsafePerformSync).run.runAsync(_ => ())

    awakeEvery(2 seconds).map(z => checker2(Some(z)).run.unsafePerformSync).run.runAsync(_ => ())
  }

  def generateTick(z:Duration): Process[Task,Unit] = {
    println(s"${Thread.currentThread().getName} generateTick [$z]")
    val x = FeedMessage(0L, Incident(0L, 0L, "TICK"))
    emit(x).toSource to enqueueToQ(q1)
  }

  def checker: Process[Task, Unit] = {
    println (s"${Thread.currentThread().getName} checker")
    for {
      c <- updatedPricingCalls
    } yield c.foreach {case (key, value) => println (s"${Thread.currentThread().getName} checker --> $key --> $value")}
  }

  def checker2(z:Option[Duration] = None): Process[Task, Unit] = {
    println (s"${Thread.currentThread().getName} checker2 awoke @ [$z]")
    for {
      c:Map[Long,PricingCall] <- eval(pricingCalls.get)
    } yield c.foreach {case (key, value) => println (s"${Thread.currentThread().getName} checker2 --> $key --> $value")}
  }
}

class FeedConsumer(callback : FeedMessage => Unit)
                    (implicit ch:com.rabbitmq.client.Channel) extends Consumer {
  import org.json4s._
  import org.json4s.native.JsonMethods._
  implicit val formats = DefaultFormats

  override def handleCancel(consumerTag: String): Unit = ()
  override def handleRecoverOk(consumerTag: String): Unit = ()
  override def handleCancelOk(consumerTag: String): Unit = ()
  override def handleConsumeOk(consumerTag: String): Unit = ()
  override def handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException): Unit = throw Terminated(End)
  override def handleDelivery(consumerTag: String,
                              envelope: Envelope,
                              properties: AMQP.BasicProperties,
                              body: Array[Byte]): Unit = {
    val i = parse(new String(body, "UTF-8")).extract[Incident]
    callback(FeedMessage(envelope.getDeliveryTag, i))
  }
}
