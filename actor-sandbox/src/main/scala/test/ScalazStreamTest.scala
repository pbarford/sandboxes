package test

import scalaz.concurrent.Task
import scalaz.stream._

object ScalazStreamTest {

  def write(s:String) : Task[Unit] = Task delay {
    println(s)
  }


  def main(args: Array[String]) {

    val sink:Sink[Task,String] = Process.constant(write _)

    val src: Process[Task, String] = Process.range(1, 10) map { _.toString }

    val res: Process[Task, Unit] = src zip sink flatMap {
      case (s, f) => Process eval(f(s))
    }

    res.run.unsafePerformSync


  }
}
