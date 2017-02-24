package com.pjb.sandbox.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import com.pjb.sandbox.actors.Actor2.{Actor2Message, Actor2Result}

object Actor2 {
  val numberOfShards : Int = 100

  case class Actor2Message(id:Long, payload:String)
  case class Actor2Result(id:Long, payload:String)

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg@Actor2Message(id, payload) ⇒ (id.toString, msg)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case Actor2Message(id, _) ⇒ (id % numberOfShards).toString
  }

  def props : Props = Props(new Actor2)
}

class Actor2 extends Actor with ActorLogging {
  override def receive: Receive = {
    case Actor2Message(id, payload) =>
      log.info(s"output [${payload.toUpperCase}]")
      sender() ! Actor2Result(id, payload.toUpperCase)
  }
}
