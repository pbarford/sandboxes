package crawler

import akka.actor.{ReceiveTimeout, Props, Actor}
import akka.cluster.{ClusterEvent, Cluster}
import scala.concurrent.duration._
import crawler.Receptionist.{Failed, Result, Get}

class ClusterMain extends Actor {

  val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[ClusterEvent.MemberUp])
  cluster.subscribe(self, classOf[ClusterEvent.MemberRemoved])
  cluster.join(cluster.selfAddress)

  val receptionist = context.actorOf(Props[ClusterReceptionist], "receptionist")
  context.watch(receptionist)

  def getLater(d: FiniteDuration, url: String): Unit = {
    import context.dispatcher
    context.system.scheduler.scheduleOnce(d, receptionist, Get(url))
  }

  getLater(Duration.Zero, "http://www.google.com")

  def receive = {
    case ClusterEvent.MemberUp(member) => {
      if(member.address != cluster.selfAddress)
        println(s"member joined : $member.address")
        getLater(1.seconds, "http://www.google.com")
        getLater(2.seconds, "http://www.google.com/0")
        getLater(2.seconds, "http://www.google.com/1")
        getLater(3.seconds, "http://www.google.com/2")
        getLater(4.seconds, "http://www.google.com/3")
        context.setReceiveTimeout(3.seconds)
    }

    case Result(url, links) =>
      println(links.toVector.sorted.mkString(s"Results for '$url':\n", "\n", "\n"))
    case Failed(url, reason) =>
      println(s"Failed to fetch : $url --> $reason")
    case ReceiveTimeout =>
      cluster.leave(cluster.selfAddress)
    case ClusterEvent.MemberRemoved(m, _) =>
      context.stop(self)
  }
}
