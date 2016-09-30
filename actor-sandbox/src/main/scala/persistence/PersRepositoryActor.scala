package persistence

import akka.actor.{ActorLogging, Props, ReceiveTimeout}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import algebra.lattice.BoundedJoinSemilattice
import persistence.PersRepositoryActor.{Persist2, Persisted2}
import persistence.RepositoryActor.{KillMe, Query}

import scala.concurrent.duration._
import scala.reflect.ClassTag

object PersRepositoryActor {
  def props[A : Journaled2](eventId: Long): Props = Props(new PersRepositoryActor[A](eventId))
  case class Persist2[A : Journaled2](id: Long, event: A)
  case class Persisted2[A : Journaled2](id: Long, event: A)
}

class PersRepositoryActor [A : Journaled2](eventId: Long) extends PersistentActor with ActorLogging with  SnapshotHousekeeping {
  val journaled = implicitly[Journaled2[A]]
  import journaled._

  type SideEffect = A => Unit

  var state: Option[A] = None
  context.setReceiveTimeout(60.seconds)

  override def persistenceId: String = s"${persistenceIdPrefix}-${eventId}"

  override def preStart(): Unit = {
    log.info(s"Starting repository ${persistenceId}.")
  }

  override def postStop(): Unit = {
    log.info(s"Stopping repository ${persistenceId}.")
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, RepoEvent(evt)) => {
      log.info(s"Replayed snapshot $evt")
      state = Some(evt)
    }
    case RepoEvent(event) =>
      log.info(s"Replayed $event")
      updateState(None)(event)
    case RecoveryCompleted => log.info(s"Recovery finished for repository actor with persistenceId $persistenceId")
  }

  override def receiveCommand: Receive = performHouseKeeping.orElse({
    case Persist2(id, RepoEvent(event)) =>
      persistAsync(event)(updateState(Some(updateSideEffects(id))))
    case Query(`eventId`) => sender ! state
    case ReceiveTimeout => context.parent ! KillMe
  })

  def updateState(sideEffects: Option[SideEffect])(event: A): Unit = {
    state = Some(merger.join(state.getOrElse(merger.zero), event))
    sideEffects.foreach(_(event))
  }

  val updateSideEffects: Long => SideEffect = { eventId => event =>
    log.info(s"Persisted: $event")
    sender() ! Persisted2(eventId, event)
    trySnapshot()
  }

  def trySnapshot(): Unit = {
    if (System.nanoTime() % 5 == 0) {  //TODO: remember to add some real mechanism for snapshotting.
      state.foreach{s =>
        log.info(s"Saving snapshot with state $s")
        saveSnapshot(s)
      }
    }
  }
}

trait Journaled2[A] {
  def persistenceIdPrefix: String
  def merger: BoundedJoinSemilattice[A]
  def classTag: ClassTag[A]
  object RepoEvent {
    def unapply(any: Any): Option[A] = {
      if (classTag.runtimeClass.isInstance(any)) Some(any.asInstanceOf[A])
      else None
    }
  }
}