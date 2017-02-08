package streams

import fs2.{Handle, Pipe, Pull, Scheduler, Sink, Strategy, Stream, Task}

import scala.concurrent.duration._

object Test extends App {

  def lines[F[_]]: Pipe[F,String,String] = in => in.flatMap(x => Stream.emits(x.lines.toList))

  def ints[F[_]]: Pipe[F, String, Int] = in => in.flatMap(x => Stream.emit(x.toInt + 10))

  def sink[F[_]]:Sink[F, String] = in => in.map(x => println(s"out = $x"))

  def sink2[F[_],A](prefix:String):Sink[F, A] = in => in.map(x => println(s"${Thread.currentThread().getName} $prefix > $x"))

  def mytake[F[_], A](n:Int): Pipe[F,A,A] = {
    def go(n:Int): Handle[F,A] => Pull[F,A,Unit] = handle => {
      if (n <= 0)
        Pull.done
      else
        // handle.receive1 { case (a, h) => Pull.output1(a).flatMap { _ => go(n-1)(h) } }
        handle.receive1 { case (a, h) => Pull.output1(a) >> go(n-1)(h) }
    }
    in => in.pull(go(n))
  }

  def mytakec[F[_], A](n:Int): Pipe[F,A,A] = {
    def go(n:Int): Handle[F,A] => Pull[F,A,Unit] = handle => {
      if (n <= 0) Pull.done
      else
        handle.receive {
          case (c, h) =>
            if(c.size <= n )
              Pull.output(c) >> go(n-c.size)(h)
            else
              Pull.output(c.take(n))
        }
    }
    in => in.pull(go(n))
  }

  def log[A](prefix:String): Pipe[Task,A,A] = t => t.evalMap { (x: A) => Task.delay{println(s"$prefix> $x"); x}}

  implicit val strategy : Strategy = Strategy.fromFixedDaemonPool(8)
  implicit val scheduler: Scheduler = Scheduler.fromFixedDaemonPool(4)

  def randomDelays[A](max:FiniteDuration):Pipe[Task,A,A] = s => s.evalMap { a =>
    val delay = Task.delay(scala.util.Random.nextInt(max.toMillis.toInt))
    delay.flatMap( d => Task.now(a).schedule(d.millis))
  }

  val t: Stream[Nothing, String] = Stream("1", "2", "3")

  val t2: Stream[Task, String] = Stream("1", "2", "3").repeat.covary[Task].take(5)

  t2.to(sink).runLog.unsafeRun()

  println(t2.observe(sink2[Task, String]("A")).runLog.unsafeRun())
  println(t.pure.through(ints).toList)
  //pipe.take(2)(t).toList

  println(Stream("hello\ndoes this\nwoork correctly\nyes!!!").pure.through(lines).toList)
  println(Stream(1,2,3).repeat.take(10).toList)

  println(Stream(2,3,4,5,6,7,8,9).pure.through(mytake(4)).toList)
  println(Stream(1,2,3).pure.repeat.through(mytake(10)).chunks.toList)
  println(Stream(1,2,3).repeat.throughPure(mytakec(10)).chunks.toList)

  Stream(1,2,3).through(log("")).run.unsafeRun()

  //println(Stream.range(1, 20).through(randomDelays(1.second)).observe(sink2[Task,Int]("A")).runLog.unsafeRun())

  val a = Stream.range(1, 5).through(randomDelays(1.second)).observe(sink2[Task,Int]("A"))
  val b =  Stream.range(1, 5).through(randomDelays(1.second)).observe(sink2[Task,Int]("B"))
  val c =  Stream.range(1, 5).through(randomDelays(1.second)).observe(sink2[Task,Int]("C"))

  a.interleave(b).run.unsafeRun()

  println("*****************************")
  a.merge(b).through(log("merge")).run.unsafeRun()

  println("*****************************")
  a.either(b).through(log("either")).run.unsafeRun()

  println("*****************************")
  ((a merge b) merge c).through(log("merge")).run.unsafeRun()

  val streams :Stream[Task, Stream[Task,Int]] = Stream(a,b,c)
  println("*****************************")
  println(fs2.concurrent.join(3)(streams).through(log("join")).runLog.unsafeRun())
}
