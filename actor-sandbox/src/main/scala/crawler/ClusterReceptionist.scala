package crawler

import akka.actor.{Props, Address, Actor}
import akka.cluster.{ClusterEvent, Cluster}
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import crawler.Receptionist.{Failed, Get}

class ClusterReceptionist extends Actor {

  val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[MemberUp])
  cluster.subscribe(self, classOf[MemberRemoved])

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = awaitingMembers

  val awaitingMembers: Receive = {
    case current: ClusterEvent.CurrentClusterState => {
      val addresses = current.members.toVector map (member  => member.address)
      val notMe = addresses filter(address => address != cluster.selfAddress)
      if(notMe.nonEmpty) context.become(active(notMe))
    }

    case MemberUp(member) if member.address != cluster.selfAddress =>
      context.become(active(Vector(member.address)))

    case Get(url) => sender ! Failed(url, "no nodes available")
  }

  def active(addresses: Vector[Address]): Receive = {
    case MemberUp(member) if member.address != cluster.selfAddress =>
      context.become(active(addresses :+ member.address))

    case MemberRemoved(member, _) => {
      val next = addresses filterNot(address => address == member.address)
      if(next.isEmpty) context.become(awaitingMembers)
      else context.become(active(next))
    }

    case Get(url) if context.children.size < addresses.size =>
      val client = sender
      val address = pick(addresses)
      context.actorOf(Props(new Customer(client, url, address)))
    case Get(url) =>
      sender ! Failed(url, "too many parrallel queries")
  }

  def pick(addresses :Vector[Address]):Address = addresses.head
}
