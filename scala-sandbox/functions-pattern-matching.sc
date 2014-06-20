
val f: String => String = { case "ping" => "pong" }

f("ping")

val g: PartialFunction[String, String] = {case "ping" => "pong"}

g.isDefinedAt("abc")

val h: PartialFunction[List[Int], String] = {
  case Nil => "one"
  case x :: rest =>
    rest match {
      case Nil => "two"
    }
}
 
h.isDefinedAt(List(1,2,3,3))
h(List(1,2))