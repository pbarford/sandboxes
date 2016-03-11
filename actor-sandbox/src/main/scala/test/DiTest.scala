package test


object DiTest {

  case class Config(host: String, port: Int) {
    def prettyPrint(prefix: String, msg: String): String =
      List(prefix, ": ", msg, " on ", host, ":", port.toString).mkString
  }

  class ReaderMonad[C, A](g: C => A) {

    def apply(c: C) = g(c)

    def map[B](f: A => B): ReaderMonad[C, B] =
      new ReaderMonad(c => f(g(c)))


    def flatMap[B](f: A => ReaderMonad[C, B]): ReaderMonad[C, B] =
      new ReaderMonad(c => f(g(c))(c))

    def pure[A](a :A): C => A = c => a

  }

  object ReaderMonad {
    def apply[A, B](b: B) =
      new ReaderMonad[A, B](a => b)

    def ask[A]: ReaderMonad[A, A] =
      new ReaderMonad(identity)
  }

  type ConfigReader[A] = ReaderMonad[Config, A]

  private def doStuff(prefix: String, msg: String): ConfigReader[String] =
    ReaderMonad.ask.map(_.prettyPrint(prefix, msg))

  private def doCoolStuff(msg: String): ConfigReader[String] =
    doStuff("cool", msg)

  private def doMoreStuff(msg: String): ConfigReader[String] =
    doStuff("more", msg)

  def run() {
    val config = Config("somehost.com", 1337)
    val doAllTheStuff = for {
      cool <- doCoolStuff("foo")
      more <- doMoreStuff("bar")
    } yield List(cool, more)


    // execute all functions with one configuration
    doAllTheStuff(config).foreach(println)
  }

  def main(args: Array[String]) {
    run()
  }
}
