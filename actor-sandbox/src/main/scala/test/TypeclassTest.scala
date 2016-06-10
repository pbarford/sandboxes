package test

import test.TypeclassTest.{Expr, Minus, Plus, Number}

object JsonWriter {

  sealed trait JsonValue
  case class JsonObject(entries :Map[String, JsonValue]) extends JsonValue
  case class JsonArray(entries :Seq[JsonValue]) extends JsonValue
  case class JsonString(value :String) extends JsonValue
  case class JsonNumber(value:BigDecimal) extends JsonValue
  case class JsonBoolean(value:Boolean) extends JsonValue
  case object JsonNull extends JsonValue

  trait JsonConvertor[A] {
    def convertToJson(value:A): JsonValue
  }


  implicit val expressionJsonConvert:JsonConvertor[Expr] = new JsonConvertor[Expr] {
    def convertToJson(expr: Expr): JsonValue = {
      expr match {
        case Number(v) => JsonNumber(v)
        case Plus(l, r)  => JsonObject(Map("op" -> JsonString("+"),
          "lhs" -> convertToJson(l),
          "rhs" -> convertToJson(r)))
        case Minus(l, r) => JsonObject(Map("op" -> JsonString("-"),
          "lhs" -> convertToJson(l),
          "rhs" -> convertToJson(r)))
      }
    }
  }

  def writeJson(value: JsonValue):String =
    value match {
      case JsonObject(entries) =>
        val serializedEntries = for ((k, v) <- entries) yield k + ": " + writeJson(v)
        "{ " + (serializedEntries mkString ", ") + "}"

      case JsonArray(entries) =>
        val serializedEntries = entries map ((e) => writeJson(e))
        "[ " + (serializedEntries mkString ", ") + "]"

      case JsonString(v) => "\"" + v + "\""
      case JsonNumber(v) => "\"" + v.toString + "\""
      case JsonBoolean(v) => "\"" + v.toString + "\""
      case JsonNull => "null"
    }

  def write[A](value:A, convert: JsonConvertor[A]):String = writeJson(convert.convertToJson(value))
  def write2[A](value:A)(implicit convert: JsonConvertor[A]):String = writeJson(convert.convertToJson(value))
}

object TypeclassTest {

  sealed trait Expr

  case class Number(value:Int) extends Expr

  case class Plus(lhs:Expr, rhs:Expr) extends Expr

  case class Minus(lhs:Expr, rhs:Expr) extends Expr

  def value(expr: Expr):Int = expr match {
    case Number(v) => v
    case Plus(l, r) => value(l) + value(r)
    case Minus(l, r) => value(l) - value(r)
  }

  def test(): Unit = {
    val e1 = Plus(Number(3), Number(4))
    val e2 = Minus(Number(3), Number(4))

    println(JsonWriter.write(e1, JsonWriter.expressionJsonConvert))
    println(JsonWriter.write(e2, JsonWriter.expressionJsonConvert))
    //println(JsonWriter.write2(e))
  }

  def main(args: Array[String]) {
    println(value(Plus(Number(3), Number(4))))
    test()
  }

}
