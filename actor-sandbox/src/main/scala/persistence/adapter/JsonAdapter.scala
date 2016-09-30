package persistence.adapter

import akka.persistence.journal.{EventAdapter, EventSeq}
import org.json4s.native.Serialization
import persistence.HubEvent

class JsonAdapter extends EventAdapter {
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import native.Serialization.{write => swrite}
  implicit val formats = Serialization.formats(NoTypeHints)

  val validManifest = s"${HubEvent.getClass.getName}-v1"

  override def fromJournal(event: Any, manifest: String): EventSeq = (event, manifest) match {
    case (s:Array[Byte], validManifest) =>
      println(s"fromJournal $s --> v1")
      EventSeq(parse(new String(s, "UTF-8")).extract[HubEvent])
    case _ =>
      println(s"fromJournal error [${event.getClass.getName}]")
      throw new RuntimeException("ficked")
  }

  override def manifest(event: Any): String = s"${event.getClass.getName}-v1"

  override def toJournal(event: Any): Array[Byte] = {
    println(s"toJournal $event")
    swrite(event.asInstanceOf[AnyRef]).getBytes
  }
}
