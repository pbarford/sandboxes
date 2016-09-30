package persistence.serializer

import akka.serialization.SerializerWithStringManifest
import org.json4s.native.Serialization
import persistence.HubEvent

class JsonSerializer extends SerializerWithStringManifest {

  case object EventDeserializationFailure

  import org.json4s._
  import org.json4s.native.JsonMethods._
  import native.Serialization.{write => swrite}
  implicit val formats = Serialization.formats(NoTypeHints)

  final val HubEventManifest = s"${classOf[HubEvent].getName}:v1"

  override def identifier: Int = 697306

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case HubEventManifest =>
      parse(new String(bytes, "UTF-8")).extract[HubEvent]
    case _ =>
      println("fromBinary 2")
      throw new RuntimeException("ficked")

  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case _ => println("toBinary")
      swrite(o).getBytes
  }

  override def manifest(o: AnyRef): String = s"${o.getClass.getName}:v1"

}
