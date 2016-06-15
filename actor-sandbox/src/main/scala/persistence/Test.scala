package persistence

import akka.actor.{Actor, ActorSystem, Props}
import com.datastax.driver.core.{Cluster, Session}
import persistence.TestEvent._
import persistence.RepositoryActor.{Persist, Persisted, Query}

object Test {

  def session(points:String, user:String, pass:String, keyspace:String): Session  = {

   Cluster.builder()
      .addContactPoint(points)
      .build()
      .connect(keyspace)
  }

  def main(args: Array[String]) {

    val journal = new EventJournal(session("127.0.0.1", "", "", "test"))
    val sys = ActorSystem.create("test")

    val t = sys.actorOf(Props(new TestActor(journal)))
    t ! Test
  }
}

class TestActor(journal:EventJournal) extends Actor {

  val repo = context.actorOf(RepositoryActor.props(1234L, journal))

  override def receive: Receive = {
    case Test =>
      repo ! Query(1234)
    case Persisted(id, ev) =>
      println(s"** persisted [$ev]")
    case Some(ev:TestEvent) =>
      println(s"** state [$ev]")
      repo ! Persist(1234, TestEvent(1234, ev.seqNo + 1, s"update [${ev.seqNo+1}]"))
    case None =>
      println("** no state")
      repo ! Persist(1234, TestEvent(1234, 1, "update [1]"))

    case _ => println("unknown!!!!")
  }
}
