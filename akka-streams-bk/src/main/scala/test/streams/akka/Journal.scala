package test.streams.akka

import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.Task

class Journal {
  val r = scala.util.Random
  def bet[F](f: => Unit):Task[Unit] = {
    Task.async {
      cb =>
      if (r.nextInt(2) == 0) {
        println("success")
        cb(\/-(f))
      } else {
        println("failure")
        cb(-\/(new Exception("error")))
      }
    }
  }

  val t1 = Task.async[Unit] {
    cb =>
      if (r.nextInt(2) == 0) {
        cb(\/-(()))
      } else {
        cb(-\/(new Exception("error")))
      }
  }

  def bet2(callback : (Throwable \/ Unit) => Unit): Unit = t1.runAsync(callback)

}

object JournalTest extends App {

  def x(i :Int) = println(s"done $i")

  val j = new Journal()
  for(i <- 1 to 10) {
    //j.bet(x(i)).runAsync(res)
    j.bet2(res)
  }

  def res: (Throwable \/ Unit) => Unit = {
    case -\/(e) => println(e)
    case \/-(()) => println("ok")
  }
}
