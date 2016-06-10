package persistence

import algebra.lattice.BoundedJoinSemilattice
import scala.reflect.ClassTag

object EventX {

  case class Event(id:Int, seqNo:Int, name:String)

  implicit val boundedJoinSemilattice = new BoundedJoinSemilattice[Event] {
    override def zero: Event = {
      Event(0, 0, "initName")
    }

    override def join(lhs: Event, rhs: Event): Event = rhs
  }

  implicit val journaled = new Journaled[Event] {
    override def merger: BoundedJoinSemilattice[Event] = boundedJoinSemilattice
    override def classTag: ClassTag[Event] = implicitly[ClassTag[Event]]
  }
}