package test

trait Printable[A] {

  def pformat(value:A):String
}

case class Cat(name:String, age:Int, color:String)

object Printable {
  def pformat[A](value:A)(implicit printable: Printable[A]): String = printable.pformat(value)
  def print[A](value:A)(implicit printable: Printable[A]): Unit = println(pformat(value))
}

object PrintableSyntax {
  implicit class PrintableOps[A](value:A) {
    def pformat(implicit printable: Printable[A]): String = printable.pformat(value)
    def print(implicit printable: Printable[A]): Unit = println(pformat)
  }
}

object DefaultPrintable {

  implicit val stringPrintable = new Printable[String] {
    override def pformat(value: String): String = s"string: $value"
  }

  implicit val catPrintable = new Printable[Cat] {
    override def pformat(value: Cat): String = s"${value.name} is a ${value.age} ${value.color} cat."
  }
}

object PrintableTest {
  def main (args: Array[String]) {
    testInterfaceObject()
    testInterfaceSyntax()
  }

  def testInterfaceObject(): Unit = {
    import DefaultPrintable._
    println(Printable.pformat("hello"))
    Printable.print(Cat("tabby", 4, "brown"))
  }

  def testInterfaceSyntax():Unit = {
    import DefaultPrintable._
    import PrintableSyntax._

    println("test" pformat)
    Cat("tommy", 5, "ginger") print
  }
}
