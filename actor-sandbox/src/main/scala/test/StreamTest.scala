package test

import scalaz.concurrent.Task
import scalaz.stream._

object StreamTest {
  sealed trait Loglevel
  case object Info extends Loglevel
  case object Debug extends Loglevel
  case object Warning extends Loglevel

  case class Line(level: Loglevel, line: String)

  val outInfo = io.stdOutLines.contramap((l: Line) => "I: " + l.line)
  val outDebug = io.stdOutLines.contramap((l: Line) => "D: " + l.line)
  val outWarning = io.stdOutLines.contramap((l: Line) => "W: " + l.line)

  def main(args: Array[String]) {

/*
    val q = async.boundedQueue[Int](10)

    val left = q.dequeue
    val right = q.dequeue

    (Process(1, 2, 3, 4, 5) to q.enqueue).run.run

    val both = (left map { i => s"left: $i" }) merge (right map { i => s"right: $i" })
    (both take 5).to(io.stdOutLines).runLog.run


    val q1 = async.boundedQueue[String](5)

    (Process("TEST1", "TEST2") to q1.enqueue).run.run
    val src:Process[Task, String] = q1.dequeue
    src.to(io.stdOutLines).runLog.unsafePerformSync
*/

    val q2 = async.boundedQueue[String](100)
    val ss:Process[Task, String] = q2.dequeue

    val ls = for(n <- 1 to 5) yield s"TESTING: $n"
    q2.enqueueAll(ls).unsafePerformSync

    println("ok")

    ss.to(io.stdOutLines).runLog.unsafePerformSync
    println("done")

    val p = Process(5, 4, 3, 2, 1)

    val results = p collect {
      case 1 => "one"
      case 2 => "two"
      case 3 => "three"
    } filter { _.length > 3 } map { _.toUpperCase }

    //prinln(results.toSource.runLog.unsafePerformSync)

    val names = Process("one", "two", "three")
    def nums(n: Int):Process[Nothing, Int] = Process(n) ++ nums(n + 1)

    val s = names zip nums(1)

    val zipped = outInfo.zip(outDebug).zip(outWarning).map {
      case ((fInfo, fDebug), fWarning) =>
        (l: Line) => l.level match {
          case Info    => fInfo(l)
          case Debug   => fDebug(l)
          case Warning => fWarning(l)
        }
    }

    val lines = List(
      Line(Info, "Hello"),
      Line(Warning, "Oops"),
      Line(Debug, "ui ui"),
      Line(Info, "World"))

    //Process.emitAll(lines).toSource.to(zipped).run.unsafePerformSync

    def awriter(p:String): Sink[Task, String] = {
      Process.repeatEval {
        Task.delay {
          s: String =>
            Task delay {
              println(s"$p $s")
            }
        }
      }
    }

    val o1 = awriter("t1")
    val o2 = awriter("t2")

    val t = Process("1", "2", "3")
    def process():Process[Task, Unit] = {

      //t.toSource.observe(o1) to o2
      t.toSource.to(o1).onComplete(Process.eval(Task { print("done")}).drain)
    }

    //process.run.unsafePerformSync

  }
}
