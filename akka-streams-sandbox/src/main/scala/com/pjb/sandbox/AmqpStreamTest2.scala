package com.pjb.sandbox

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Balance, Flow, GraphDSL, RunnableGraph, Sink}
import akka.util.Timeout
import com.pjb.sandbox.actors.Actor1.{Actor1Message, Actor1Result}
import com.pjb.sandbox.actors.Actor2.{Actor2Message, Actor2Result}
import com.pjb.sandbox.actors.{Actor1, Actor2}
import com.pjb.sandbox.amqp.AmqpAckSink.AmqpAckSinkSettings
import com.pjb.sandbox.amqp._
import com.rabbitmq.client.{Channel, ConnectionFactory}

import scala.concurrent.Future
import scala.concurrent.duration._

object AmqpStreamTest2 extends App {

  implicit val system = ActorSystem("StreamTestSandbox")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  val connectionFactory = {
    val cf = new ConnectionFactory()
    cf.setHost("localhost")
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf
  }

  val actor1Region: ActorRef = ClusterSharding(system).start(
    typeName = "Actor1",
    entityProps = Actor1.props,
    settings = ClusterShardingSettings(system),
    extractEntityId = Actor1.extractEntityId,
    extractShardId = Actor1.extractShardId)

  val actor2Region: ActorRef = ClusterSharding(system).start(
    typeName = "Actor2",
    entityProps = Actor2.props,
    settings = ClusterShardingSettings(system),
    extractEntityId = Actor2.extractEntityId,
    extractShardId = Actor2.extractShardId)

  val connection = connectionFactory.newConnection()
  val inChannel:Channel = connection.createChannel()
  val outChannel:Channel = connection.createChannel()
  val amqpSourceSettings:AmqpSourceSettings = AmqpSourceSettings("inbound-q", "stream-test", ackOnPush = false)
  val amqpSinkSettings:AmqpAckSinkSettings = AmqpAckSinkSettings("outbound", "")

  def sendActor1 : Message => Future[Actor1Result] = { m =>
    implicit val timeout = Timeout(5 seconds)
    ask(actor1Region, Actor1Message(m.deliveryTag, m.data)).mapTo[Actor1Result]
  }

  def sendActor2 : Actor1Result => Future[Actor2Result] = { m =>
    implicit val timeout = Timeout(5 seconds)
    ask(actor2Region, Actor2Message(m.id, m.payload)).mapTo[Actor2Result]
  }

  def pubAndAckMessage : Actor2Result => Unit = (res) => {
    outChannel.basicPublish("outbound", "", null, res.payload.getBytes)
    inChannel.basicAck(res.id, false)
  }

  def aSink = AmqpAckSink.toSink(outChannel, amqpSinkSettings)
  def flow: Flow[Message, Future[Future[String]], NotUsed] = {
    Flow[Message]
      .map(sendActor1)
        .map(f => f.map(sendActor2))
          .map { f1 =>
            f1.map { f2 =>
              f2.map { r =>
                pubAndAckMessage(r)
                r.payload
              }
            }
          }
  }


  val stream = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val in = AmqpSource2.toSource(inChannel, amqpSourceSettings)
    val balance = builder.add(Balance[Message](2))
    in ~> balance.in
    balance.out(0) ~> flow ~> Sink.foreach[Future[Future[String]]](f1 => f1.map(f2=>f2.map(s => println(s"SINK1 = $s"))))
    balance.out(1) ~> flow ~> Sink.foreach[Future[Future[String]]](f1 => f1.map(f2=>f2.map(s => println(s"SINK2 = $s"))))
    ClosedShape
  })

  stream.run()
}
