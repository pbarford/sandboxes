package test.util

object JsonUtils extends App {

  def renderJson : AnyRef => String = { s =>
    import net.liftweb.json._
    import net.liftweb.json.Serialization.write
    implicit val formats = DefaultFormats
    write(s)
  }

  case class Test(name:String, age:Int)
  println(renderJson(Test("paulo", 42)))

}
