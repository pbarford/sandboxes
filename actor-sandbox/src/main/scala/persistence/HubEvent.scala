package persistence

import algebra.lattice.BoundedJoinSemilattice

import scala.reflect.ClassTag

case class HubEvent (id:Long, seqNo:Int, name:String)

object HubEvent {

  implicit val boundedJoinSemilattice = new BoundedJoinSemilattice[HubEvent] {
    override def zero: HubEvent = {
      HubEvent(0, 0, "initName")
    }

    override def join(lhs: HubEvent, rhs: HubEvent): HubEvent = rhs
  }

  implicit val journaled = new Journaled2[HubEvent] {
    override def persistenceIdPrefix: String = "hub-event-repository"
    override def merger: BoundedJoinSemilattice[HubEvent] = boundedJoinSemilattice
    override def classTag: ClassTag[HubEvent] = implicitly[ClassTag[HubEvent]]
  }
}