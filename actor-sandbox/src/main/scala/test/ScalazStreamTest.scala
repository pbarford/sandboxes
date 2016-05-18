package test

import scalaz.stream.async.mutable.Signal
import scalaz.{\/-, -\/}
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream._
import scalaz.stream.Process._

import scalaz.stream.time._
import scala.concurrent.duration._

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

    val w = writer.logged(src).mapW("debug : " + _)
                              .drainW(io.stdOutLines)
                              .map(s"info " + ).to(io.stdOutLines)

    w.run.unsafePerformSync

    val z = writer.logged(Process.range(1, 10)).mapW("debug : " + _.toString )
                                  .drainW(io.stdOutLines)
                                  .map(_+1)
                                  .map(_.toString).to(io.stdOutLines)

    z.run.unsafePerformSync



    val y = src.flatMap(i => emit(-\/(s"w:$i")) ++ emit(\/-(s"r:$i")))
                .mapW("Debug : " + _)
                .drainW(io.stdOutLines)
                .map("Info : "  + _)
                .to(io.stdOutLines)

    y.run.unsafePerformSync

    /*
    val input: Process[Task,Boolean] = awakeEvery(300 milliseconds)(Strategy.DefaultStrategy, Strategy.DefaultTimeoutScheduler).map(_ => (math.random < 0.3))
    val counter = input.scan( (0L,0L) )( (count, event) => ( count._1+1, count._2 + (if (event) { 1L } else { 0L }) ) )
    val sig = async.signal[(Long,Long)]
    val snk = sig.sink
    val counterToSignal = counter
      .map( x => async.mutable.Signal.Set( x ) : async.mutable.Signal.Msg[(Long, Long)])
        .to(snk)

    Task.fork( counterToSignal.run ).runAsync( _ => () ) //Run this in a separate thread
    Thread.sleep(5000)
    println(sig.get.run)
*/

    val sig2:Signal[Boolean] = async.signalOf[Boolean](false)

    val onChange: Process[Task,Boolean] = sig2.discrete

    val p = Process.eval(Task.delay{println("ok2")})
    val p2 :Process[Task,Boolean] = Process.eval(Task.now{false})

    Task.fork( Process.sleepUntil(onChange)(p).repeat.run ).runAsync( _ => () )
    sig2.set(true).unsafePerformSync
    sig2.set(false).unsafePerformSync
    Thread.sleep(1)
    sig2.set(true).unsafePerformSync
    Thread.sleep(1)
    sig2.set(false).unsafePerformSync

    Thread.sleep(100)


  }

}
