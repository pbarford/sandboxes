package test

object Reader {


  case class Reader[C,A](g: C => A) {
    def apply(c:C) = g(c)
    def map[B](f: A => B):Reader[C,B] =
      new Reader(c => f(g(c)))

    def flatMap[B](f: A => Reader[C,B]):Reader[C,B] =
      new Reader(c => f(g(c))(c))

    def pure[A](a: A):Reader[C, A] = Reader(c => a)
  }

  implicit def reader[A,B](f: A => B) = Reader(f)


  case class Config(host: String, port: Int) {
    def prettyPrint(prefix: String, msg: String): String =
      List(prefix, ": ", msg, " on ", host, ":", port.toString).mkString
  }

  type ConfigReader[A] = Reader[Config, A]

  private def doStuff(prefix: String, msg: String): ConfigReader[String] =
    reader(_.prettyPrint(prefix, msg))

  def run() {

    val config = Config("somehost.com", 1337)

    val doAllTheStuff = for {
      cool <- doStuff("cool", "foo")
      more <- doStuff("more", "bar")
    } yield List(cool, more)

    doAllTheStuff(config).foreach(println)
  }




  def main(args: Array[String]) {
   run()
  }

}
