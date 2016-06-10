package persistence

import akka.actor.ActorRef
import com.datastax.driver.core.querybuilder.{BuiltStatement, QueryBuilder}
import com.datastax.driver.core.{ResultSet, Session}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import persistence.RepositoryActor.{Restore, RestoreComplete}

import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.Task
import argonaut._
import Argonaut._
import persistence.EventX.Event

class EventJournal(session:Session) extends Journal[Event]{

  implicit def eventCodecJson = casecodec3(Event.apply, Event.unapply)("id", "seqNo", "name")

  override def write(ev: Event)(cb: Event => Unit): Task[Unit] = {
    val st = QueryBuilder.insertInto("events")
      .value("id", ev.id)
      .value("seqNo", ev.seqNo)
      .value("data", ev.asJson.nospaces)
    executeAsyncWrite(st, ev)(cb)
  }

  override def restore(id: EventId)(ref: ActorRef): Task[Unit] = {
    println(s"restore [$id]")
    val st = QueryBuilder.select("data").from("events").where(QueryBuilder.eq("id", id)).orderBy(QueryBuilder.asc("seqNo"))
    executeAsyncRead(id, st)(ref)
  }

  private def executeAsyncWrite(st:BuiltStatement, ev:Event)(cb : Event => Unit):Task[Unit] = {
    Task.async {
      register =>
        Futures.addCallback(session.executeAsync(st), toHandlerWrite(register, ev, cb))
    }
  }

  private def executeAsyncRead(id:EventId, st:BuiltStatement)(ref:ActorRef):Task[Unit] = {
    Task.async {
      register =>
        println(s"executeAsyncRead --> [${st.toString}]")
        Futures.addCallback(session.executeAsync(st), toHandlerRead(register, id, ref))
    }
  }

  def toHandlerWrite(k: (Throwable \/ Unit) => Unit, e: Event, cb : Event => Unit) = new FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = k(-\/ (t))
    override def onSuccess(v: ResultSet): Unit = k(\/- (cb(e)))
  }


  def toHandlerRead(k: (Throwable \/ Unit) => Unit, id:EventId, ref:ActorRef) = new FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = k(-\/ (t))
    override def onSuccess(v: ResultSet): Unit = k(\/- (convert(id, ref)(v)))
  }


  def convert(id:EventId, ref:ActorRef) : ResultSet => Unit = { rs =>
    import scala.collection.JavaConversions._
    rs.all().foreach { r =>
      Parse.decodeOption[Event](r.getString(0)) match {
        case Some(ev:Event) => ref ! Restore(id, ev)
        case None => println("decode issue")
      }
    }
    ref ! RestoreComplete(id)
  }
}
