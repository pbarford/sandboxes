package test

import scalaz.{\/-, -\/}
import scalaz.concurrent.Task
import scalaz.stream._

object StreamConcurrency {

  val fetchPage :Int => Task[String] = (n => Task delay(s"fetched $n"))
  val fetchChannel = Process.constant(fetchPage)

  def fetch(n:Int):Task[String] =
    Task delay {
      println(s"${Thread.currentThread().getName} fetched [$n]")
      s"fetched [$n]"
    }

  def fetch2(n:Int):Task[String] =
    Task delay {
      println(s"${Thread.currentThread().getName} fetched [$n]")
      s"fetched [$n]"
    }

  val ns: Process[Task,Int] = Process.range(0,10)

  val adjusted = ns map { i => i * 2 }  filter( _ < 10 )

  val pages : Process[Task, Process[Task,String]] = {
    adjusted map { num =>
      Process.eval(fetch(num))
    }
  }

  val p : Process[Task, String] = merge.mergeN(2)(pages)

  val lines = Process.range(0,10).map("line" + _.toString)

  val cnt = new java.util.concurrent.atomic.AtomicInteger(0)

  val printme: Channel[Task, String, Int] = io.resource(Task.delay(())) {
    resource => Task.delay(()) } /* nothing to close */ {
    resource => Task.delay {
      case s => Task {
        val n = cnt.incrementAndGet()
        println(s"${Thread.currentThread().getName} - String = $s, Counter = $n")
        Thread.sleep((math.random * 500).toInt)
        n
      }
    }
  }

  /*
   * Rather than using `.through`, which is deterministic, we'll just use
   * regular zipping to feed the channel. We can decide later how much
   * nondeterminism to allow when sequencing the actions.
   */
  val actions: Process[Task,Task[Int]] =
    lines.zipWith(printme)((line,chan) => chan(line))

  /* Just convert the inner `Task` to a `Process`. */
  val nestedActions: Process[Task, Process[Task,Int]] =
    actions.map(Process.eval)

  /*
   * `mergeN` has the same sort of signature as `join`, but allows for
   * nondeterminism, rather than just concatenating the streams in order.
   */
  val concurrentActions: Process[Task,Int] =
    merge.mergeN(3)(nestedActions) // allows only two 'open' streams

  def main(args: Array[String]) {

    val z = ns through fetchChannel
    println(z.runLog.attemptRun)

    val s = p.runLog.attemptRun match {
      case -\/(t) => println(t.getMessage)
      case \/-(p) => println(p)
    }

    println { concurrentActions.runLog.attemptRun }
  }
}
