
abstract class JSON
case class JSeq (elems:List[JSON]) extends JSON
case class JObj (bindings: Map[String, JSON]) extends JSON
case class JNum (num:Double) extends JSON
case class JStr (str:String) extends JSON
case class JBool (b: Boolean) extends JSON
case object JNull extends JSON

object PatternMatching {
  def main(args: Array[String]) {
    val data = JObj(Map(
        "firstname" -> JStr("Paul"),
        "lastname" -> JStr("Barford"),
        "active" -> JBool(true),
        "age" -> JNum(40),
        "address" -> JObj(Map(
            "town" -> JStr("killiney"),
            "city" -> JStr("dublin")
        )),
        "phoneNumbers" -> JSeq(List(
            JObj(Map(
              "type" -> JStr("home"), "number" -> JStr("01-5487485")
            )),
            JObj(Map(
              "type" -> JStr("mobile"), "number" -> JStr("086-8144517")
            ))
        ))
    ))
    println(show(data))

    val f : PartialFunction[String, String] = {case "ping" => "pong"}
    println(f.isDefinedAt("paul"))
    println(f.isDefinedAt("ping"))
    println(f("ping"))

    val f2: PartialFunction[List[Int], String] = {
      case Nil => "one"
      case x :: y :: rest => "two"
    }

    val f3: PartialFunction[List[Int], String] = {
      case Nil => "one"
      case x :: rest => rest match { case Nil => "two" case rest => "haha"}
    }


    println(f2.isDefinedAt(List(1,2,3)))
    println(f3.isDefinedAt(List(1,2,3)))
    println(f3(List(1,2,3)))
  }

  def show(json: JSON): String = json match {
    case JSeq(elems) =>
      "[" + (elems map show mkString ", ") + "]"
    case JObj(bindings) =>
      val assocs = bindings map {
        case (key, value) => "\"" + key + "\": " + show(value)
      }
      "{" + (assocs mkString ", ") + "}"
    case JNum(num) => num.toString
    case JStr(str) => "\"" + str + "\""
    case JBool(b) => b.toString
    case JNull => "null"
  }


}
