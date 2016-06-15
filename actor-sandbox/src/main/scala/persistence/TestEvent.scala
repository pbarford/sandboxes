package persistence

import algebra.lattice.BoundedJoinSemilattice
import scala.reflect.ClassTag

case class TestEvent(id:Long, seqNo:Int, name:String)

object TestEvent {

  implicit val boundedJoinSemilattice = new BoundedJoinSemilattice[TestEvent] {
    override def zero: TestEvent = {
      TestEvent(0, 0, "initName")
    }

    override def join(lhs: TestEvent, rhs: TestEvent): TestEvent = rhs
  }

  implicit val journaled = new Journaled[TestEvent] {
    override def merger: BoundedJoinSemilattice[TestEvent] = boundedJoinSemilattice
    override def classTag: ClassTag[TestEvent] = implicitly[ClassTag[TestEvent]]
  }
}