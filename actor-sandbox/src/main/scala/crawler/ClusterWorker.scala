package crawler

import akka.actor.Actor
import akka.cluster.{ClusterEvent, Cluster}

class ClusterWorker extends Actor {

  val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[ClusterEvent.MemberRemoved])
  val main = cluster.selfAddress.copy(port= Some(2552))
  cluster.join(main)

  def receive = {
    case ClusterEvent.MemberRemoved(member, _) => {
      if(member.address == main) context.stop(self)
    }
  }

  override def postStop(): Unit = {
    WebClient.shutdown()
  }
}
