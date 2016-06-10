package persistence

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Stash}
import algebra.lattice.BoundedJoinSemilattice
import persistence.RepositoryActor._

import scala.reflect.ClassTag

object RepositoryActor {
  def props[A : Journaled](eventId: Long, journal:Journal[A]): Props = Props(new RepositoryActor[A](eventId, journal))
  case class Persist[A : Journaled](id: Long, event: A)
  case class Persisted[A : Journaled](id: Long, event: A)

  case class Restore[A](id: Long, event: A)
  case class RestoreComplete(id: Long)
  case class Query(eventId: Long)
  case object KillMe
}

class RepositoryActor[A : Journaled](eventId: Long, journal:Journal[A]) extends Actor with ActorLogging with Stash {

  val journaled = implicitly[Journaled[A]]
  import journaled._

  var state: Option[A] = None
  type SideEffect = A => Unit

  journal.restore(eventId)(self).runAsync(_ => ())

  def init: Receive = {
    case Restore(id, ev:A) =>
      println(s"recover [$ev]")
      updateState(None)(ev)
    case RestoreComplete(id) =>
      println(s"recover completed [$id]")
      context.become(running)
      unstashAll()
    case _ => stash
  }

  override def receive: Receive = init

  def running: Receive = {
    case Persist(id, RepoEvent(event)) =>
      journal.write(event)(persistComplete(sender(), Some(updateSideEffects(id)))).runAsync(_ => ())
    case Query(`eventId`) =>
      println(s"query [$eventId]")
      sender ! state
    case ReceiveTimeout => context.parent ! KillMe
  }

  def persistComplete(rcv: ActorRef, sideEffects: Option[SideEffect])(event: A): Unit = {
    updateState(sideEffects)(event)
    rcv ! Persisted(eventId, event)
  }

  def updateState(sideEffects: Option[SideEffect])(event: A): Unit = {
    state = Some(merger.join(state.getOrElse(merger.zero), event))
    sideEffects.foreach(_(event))
  }

  val updateSideEffects: Long => SideEffect = { eventId => event =>
    println(s"persisted [$event]")
  }
}


trait Journaled[A] {
  def merger: BoundedJoinSemilattice[A]
  def classTag: ClassTag[A]
  object RepoEvent {
    def unapply(any: Any): Option[A] = {
      if (classTag.runtimeClass.isInstance(any)) Some(any.asInstanceOf[A])
      else None
    }
  }
}