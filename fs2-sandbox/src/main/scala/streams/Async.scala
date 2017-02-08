package streams

import fs2._
import fs2.async.mutable.Signal

import scala.concurrent.duration._

object Async extends App {

  implicit val strategy : Strategy = Strategy.fromFixedDaemonPool(8)
  implicit val scheduler: Scheduler = Scheduler.fromFixedDaemonPool(4)

  def sink2[F[_],A](prefix:String):Sink[F, A] = in => in.map(x => println(s"${Thread.currentThread().getName} $prefix > $x"))

  def randomDelays[Int](max:FiniteDuration, sig:Signal[Task,Int]):Pipe[Task,Int,Int] = s => s.evalMap { (a: Int) =>
    val delay = Task.delay(scala.util.Random.nextInt(max.toMillis.toInt))
    delay.flatMap( d => Task.now{ sig.set(a).unsafeRun(); a}.schedule(d.millis))
  }

  def log[A](prefix:String): Pipe[Task,A,A] = t => t.evalMap { (x: A) => Task.delay{println(s"$prefix> $x"); x}}

  val x = async.signalOf[Task,Int](1)
  //x.flatMap(x1 => x1.get.map(i => println(i))).unsafeRun()
  //Stream.eval(x).flatMap { x1 => x1.discrete }.through(log("signal")).run.unsafeRunAsyncFuture()

  val s = x.unsafeRun()

  s.discrete.through(log("signal")).run.unsafeRunAsyncFuture()

  val a = Stream.range(1, 5).through(randomDelays(1.second, s)).observe(sink2[Task,Int]("A"))

  a.run.unsafeRun()

}
