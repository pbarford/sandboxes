package persistence

import akka.actor.{Actor, ActorSystem, Props}
import com.datastax.driver.core.{Cluster, Session}
import persistence.PersRepositoryActor.{Persist2, Persisted2}
import persistence.TestEvent._
import persistence.RepositoryActor.{KillMe, Persist, Persisted, Query}

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
  val repo2 = context.actorOf(PersRepositoryActor.props[HubEvent](1234L))

  override def receive: Receive = {
    case Test =>
      //repo ! Query(1234)
      repo2 ! Query(1234)
    case Persisted(id, ev) =>
      println(s"** persisted [$ev]")

    case Persisted2(id, ev) =>
      println(s"** persisted2 [$ev]")

    case Some(ev:TestEvent) =>
      println(s"** state [$ev]")
      repo ! Persist(1234, TestEvent(1234, ev.seqNo + 1, s"update [${ev.seqNo+1}]"))

    case Some(ev:HubEvent) =>
      println(s"** state [$ev]")
      repo2 ! Persist2(1234, HubEvent(1234, ev.seqNo + 1, s"update [${ev.seqNo+1}]"))
    case None =>
      println("** no state")
      repo2 ! Persist2(1234, HubEvent(1234, 1, "update [1]"))


    case KillMe => println("killme")
    case _ => println("unknown!!!!")
  }
}
