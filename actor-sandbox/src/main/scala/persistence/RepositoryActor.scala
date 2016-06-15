package persistence

import akka.actor.{Actor, ActorLogging, ActorRef, FSM, Props, ReceiveTimeout, Stash}
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

  sealed trait RepositoryStates
  case object Restoring extends RepositoryStates
  case object Running extends RepositoryStates

  case class RepositoryState[A](current:Option[A], lastSender:Option[ActorRef])
}

class RepositoryActor[A : Journaled](eventId: Long, journal:Journal[A]) extends Actor with FSM[RepositoryStates, RepositoryState[A]] with ActorLogging with Stash {

  val journaled = implicitly[Journaled[A]]
  import journaled._

  type SideEffect = (A, Option[ActorRef])  => Unit

  override def preStart(): Unit = {
    startWith(Restoring, RepositoryState(None,None))
    journal.restore(eventId)(self).runAsync(_ => ())
    initialize()
  }

  when(Restoring) {
    case Event(Restore(id, RepoEvent(ev)), _) =>
      log.info(s"recover [$ev]")
      stay() using (RepositoryState(updateState(None, None)(ev), None))
    case Event(RestoreComplete(id), _) =>
      log.info(s"recover completed [$id]")
      unstashAll()
      goto(Running) using stateData
    case _ =>
      stash()
      stay()
  }

  when(Running) {
    case Event(Persist(id, RepoEvent(ev)), _) =>
      journal.write(ev)(self).runAsync(_ => ())
      stay() using (RepositoryState(stateData.current, Some(sender())))
    case Event(Persisted(id, RepoEvent(ev)), _) =>
      stay() using (RepositoryState(updateState(Some(updateSideEffects(id)), stateData.lastSender)(ev), None))
    case Event(Query(`eventId`), _) =>
      sender() ! stateData.current
      stay()
    case Event(ReceiveTimeout, _) =>
      context.parent ! KillMe
      stay()
  }

  def updateState(sideEffects: Option[SideEffect], lastSender:Option[ActorRef])(event: A): Option[A] = {
    sideEffects.foreach(effect => effect(event, lastSender))
    Some(merger.join(stateData.current.getOrElse(merger.zero), event))
  }

  val updateSideEffects: Long => SideEffect = { eventId => (event, lastSender) =>
    log.info(s"persisted [$event]")
    lastSender match {
      case Some(ref) => ref ! Persisted(eventId, event)
      case None => log.info("no lastSender")
    }
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