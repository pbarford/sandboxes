package monad

import scalaz.Reader

object ReaderEx extends App {
  def myName(step: String): Reader[String, String] = Reader {step + ", I am " + _}

  def localExample: Reader[String, (String, String, String)] = for {
    a <- myName("First")
    b <- myName("Second") >=> Reader { _ + "ie"}
    c <- myName("Third")
  } yield (a, b, c)

  println(localExample("Paul"))
}
