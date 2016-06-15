package persistence

import akka.actor.ActorRef
import com.datastax.driver.core.querybuilder.{BuiltStatement, QueryBuilder}
import com.datastax.driver.core.{ResultSet, Session}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import persistence.RepositoryActor.{Persisted, Restore, RestoreComplete}

import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.Task
import argonaut._
import Argonaut._

class EventJournal(session:Session) extends Journal[TestEvent]{

  implicit def eventCodecJson = casecodec3(TestEvent.apply, TestEvent.unapply)("id", "seqNo", "name")

  override def write(ev: TestEvent)(ref:ActorRef): Task[Unit] = {
    val st = QueryBuilder.insertInto("events")
      .value("id", ev.id)
      .value("seqNo", ev.seqNo)
      .value("data", ev.asJson.nospaces)
      .value("version", 1)
    executeAsyncWrite(st, ev)(ref)
  }

  override def restore(id: EventId)(ref: ActorRef): Task[Unit] = {
    val st = QueryBuilder.select("version", "data")
                          .from("events")
                          .where(QueryBuilder.eq("id", id))
                          .orderBy(QueryBuilder.asc("seqNo"))
    executeAsyncRead(id, st)(ref)
  }

  private def executeAsyncWrite(st:BuiltStatement, ev:TestEvent)(ref:ActorRef):Task[Unit] = {
    Task.async {
      register =>
        Futures.addCallback(session.executeAsync(st), toHandlerWrite(register, ev, ref))
    }
  }

  private def executeAsyncRead(id:EventId, st:BuiltStatement)(ref:ActorRef):Task[Unit] = {
    Task.async {
      register =>
        Futures.addCallback(session.executeAsync(st), toHandlerRead(register, id, ref))
    }
  }

  def toHandlerWrite(k: (Throwable \/ Unit) => Unit, e: TestEvent, ref:ActorRef) = new FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = k(-\/ (t))
    override def onSuccess(v: ResultSet): Unit = k(\/- (ref ! Persisted(e.id, e)))

  }

  def toHandlerRead(k: (Throwable \/ Unit) => Unit, id:EventId, ref:ActorRef) = new FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = k(-\/ (t))
    override def onSuccess(v: ResultSet): Unit = k(\/- (convert(id, ref)(v)))
  }

  def convert(id:EventId, ref:ActorRef) : ResultSet => Unit = { rs =>
    import scala.collection.JavaConversions._
    rs.all().foreach { r =>
      Parse.decodeOption[TestEvent](r.getString("data")) match {
        case Some(ev:TestEvent) => ref ! Restore(id, ev)
        case None => println("decode issue")
      }
    }
    ref ! RestoreComplete(id)
  }
}
