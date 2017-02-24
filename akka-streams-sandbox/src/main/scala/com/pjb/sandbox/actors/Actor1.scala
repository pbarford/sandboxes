package com.pjb.sandbox.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import com.pjb.sandbox.actors.Actor1.{Actor1Message, Actor1Result}

object Actor1 {

  val numberOfShards : Int = 100

  case class Actor1Message(id:Long, payload:String)
  case class Actor1Result(id:Long, payload:String)

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg@Actor1Message(id, payload) ⇒ (id.toString, msg)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case Actor1Message(id, _) ⇒ (id % numberOfShards).toString
  }

  def props : Props = Props(new Actor1)
}

class Actor1 extends Actor with ActorLogging {
  override def receive: Receive = {
    case Actor1Message(id, payload) =>
      //log.info(s"output [${payload.replaceAll(" ", ".")}]")
      sender() ! Actor1Result(id, payload.replaceAll(" ", "."))
  }
}
