package com.pjb.sandbox

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}

object AkkaStreamsApp extends App {

  implicit val system = ActorSystem("StreamTestSandbox")
  implicit val materializer = ActorMaterializer()
  val s1 = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val A: Outlet[Int]                  = builder.add(Source.single(0)).out
    val B: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2))
    val C: UniformFanInShape[Int, Int]  = builder.add(Merge[Int](2))
    val Ca: Flow[Int, Int, NotUsed] = Flow[Int].map{ i => println(i); i + 10}
    val D: FlowShape[Int, Int]          = builder.add(Flow[Int].map(_ + 1))
    val E: UniformFanOutShape[Int, Int] = builder.add(Balance[Int](2))
    val F: UniformFanInShape[Int, Int]  = builder.add(Merge[Int](2))
    val G: Inlet[Any]                   = builder.add(Sink.foreach(println)).in

                  C     <~ Ca <~      F
    A  ~>  B  ~>  C     ~> Ca ~>      F
    B  ~>  D  ~>  E  ~>  F
    E  ~>  G

    ClosedShape
  })

  s1.run()
}
