package test

import java.util.concurrent.Executors

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.rabbitmq.client.{AMQP, _}
import test.SignalTest.{FeedMessage, Incident}

import scala.collection.immutable.SortedSet
import scalaz.{\/, \/-}
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.Cause.{End, Terminated}
import scalaz.stream.{sink, _}
import scalaz.stream.Process._

object SignalTest {

  case class FeedMessage(deliveryTag:Long, incident:Incident)

  case class Incident(eventId:Long, feedSeqNo : Long, action:String) extends Ordered[Incident] {
    override def compare(that: Incident): Int = {
      if (this.feedSeqNo == that.feedSeqNo) 0
      else if (this.feedSeqNo > that.feedSeqNo) 1
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

  val pricingCalls = async.signalOf[Map[Long,SortedSet[PricingCall]]](Map.empty)
  val currentPricingCalls = pricingCalls.continuous

  val pricingTicks = async.signalOf[SortedSet[PricingCall]](SortedSet.empty)

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

  def queueFeedMessage(m:FeedMessage) = {
    Process.eval(Task.now(m)) to enqueue(Seq(q1))
  }

  def consume(queueName:String)(implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    Process.eval ( Task.delay {
      ch.basicConsume(queueName, false, new FeedConsumer(m => queueFeedMessage(m).run.unsafePerformSync))
      ()
    })
  }

  def callPricing: (FeedMessage,
                    Process[Task, Map[Long, SortedSet[PricingCall]]]) => Process[Task, String \/ FeedMessage] = (msg, pricingCalls) => {

//    msg.no % 10 match {
//      case 0 => Process.eval(Task.now(-\/("divisible by 10 error")))
//      case _ => Process.eval(Task.now(\/-(msg)))
//    }


    emit(\/-(msg)).toSource

  }

  def ack(implicit ch:com.rabbitmq.client.Channel):Sink[Task, FeedMessage] =
    sink.lift[Task,FeedMessage] { m:FeedMessage =>
      Task.delay {
        println(s"${Thread.currentThread().getName} acking ${m.deliveryTag}")
        ch.basicAck(m.deliveryTag, false)
      }
    }

  def processQ(q:async.mutable.Queue[FeedMessage],
              currentPricingCalls:Process[Task, Map[Long, SortedSet[PricingCall]]])
             (implicit ch:com.rabbitmq.client.Channel): Process[Task, Unit] = {
    for {
      feedMessage <- q.dequeue
      pricingMessage <- callPricing(feedMessage,currentPricingCalls)
      _ <- Process.eval(Task.delay(feedMessage)) to ack
    } yield ()
  }

  def main(args: Array[String]) {

    implicit val ch = connect.createChannel()
    ch.basicQos(100)

    val tf = new ThreadFactoryBuilder().setNameFormat("rmq-thread-%d").build()
    val pool = Executors.newFixedThreadPool(10, tf)
    implicit val pS = Strategy.Executor(pool)

    val m = Map[Long, SortedSet[PricingCall]](1L -> SortedSet(PricingCall(1L, 1L, 1L, 1L), PricingCall(1L, 1L, 1L, 2L)))
    println(m.lastPricingCallFor(1L))
    println(m.lastPricingCallFor(2L))

    val processes = merge.mergeN(30)(Process(consume("test"),
                                     processQ(q1, currentPricingCalls)))
    processes.run.runAsync(_ => ())
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
