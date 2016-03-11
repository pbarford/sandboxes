package test

import scalaz._
import Scalaz._

import scalaz.concurrent.Task, Task._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ScalazTest {

  case class KnightPos(c: Int, r: Int) {
    def possibleMoves: List[KnightPos] =
      for {
        KnightPos(c2, r2) <- List(KnightPos(c + 2, r - 1), KnightPos(c + 2, r + 1),
          KnightPos(c - 2, r - 1), KnightPos(c - 2, r + 1),
          KnightPos(c + 1, r - 2), KnightPos(c + 1, r + 2),
          KnightPos(c - 1, r - 2), KnightPos(c - 1, r + 2)) if (((1 |-> 8) contains c2) && ((1 |-> 8) contains r2))
      } yield KnightPos(c2, r2)

    def in3: List[KnightPos] =
      for {
        first <- possibleMoves
        second <- first.possibleMoves
        third <- second.possibleMoves
      } yield third
    def canReachIn3(pos: KnightPos): Boolean = in3 contains pos
  }

  def ackermannT(m: Int, n: Int): Task[Int] = {
    (m, n) match {
      case (0, _) => now(n + 1)
      case (m, 0) => suspend(ackermannT(m - 1, 1))
      case (m, n) =>
        suspend(ackermannT(m, n - 1)).flatMap { x =>
          suspend(ackermannT(m - 1, x)) }
    }
  }

  def isBigGang(x: Int): (Boolean, String) =
    (x > 9, "Compared gang size to 9.")

  /*
  implicit class PairOps[A](pair: (A, String)) {
    def applyLog[B](f: A => (B, String)): (B, String) = {
      val (x, log) = pair
      val (y, newlog) = f(x)
      (y, log ++ newlog)
    }
  }
  */

  implicit class PairOps[A, B: Monoid](pair: (A, B)) {
    def applyLog[C](f: A => (C, B)): (C, B) = {
      val (x, log) = pair
      val (y, newlog) = f(x)
      (y, log |+| newlog)
    }
  }

  def ackermannF(m: Int, n: Int): Future[Int] = {
    (m, n) match {
      case (0, _) => Future(n + 1)
      case (m, 0) => Future(ackermannF(m - 1, 1)).flatMap(identity)
      case (m, n) => for {
        x <- Future(ackermannF(m, n - 1))
        y <- x
        z <- Future(ackermannF(m - 1, y))
        r <- z
      } yield r
    }
  }

  def logNumber(x: Int): Writer[List[String], Int] =
    x.set(List("Got number: " + x.shows))

  def multiWithLog: Writer[List[String], Int] = for {
    a <- logNumber(3)
    b <- logNumber(5)
  } yield a * b

  def gcd(a: Int, b: Int): Writer[List[String], Int] =
    if (b == 0) for {
        _ <- List("Finished with " + a.shows).tell
      } yield a
    else
      List(a.shows + " mod " + b.shows + " = " + (a % b).shows).tell flatMap { _ => gcd(b, a % b) }

  def gcd2(a: Int, b: Int): Writer[Vector[String], Int] =
    if (b == 0) for {
      _ <- Vector("Finished with " + a.shows).tell
    } yield a
    else for {
      result <- gcd2(b, a % b)
      _ <- Vector(a.shows + " mod " + b.shows + " = " + (a % b).shows).tell
    } yield result

  def vectorFinalCountDown(x: Int): Writer[Vector[String], Unit] = {
    import annotation.tailrec
    @tailrec def doFinalCountDown(x: Int, w: Writer[Vector[String], Unit]): Writer[Vector[String], Unit] = x match {
      case 0 => w flatMap { _ => Vector("0").tell }
      case x => doFinalCountDown(x - 1, w flatMap { _ =>
        Vector(x.shows).tell
      })
    }
    val t0 = System.currentTimeMillis
    val r = doFinalCountDown(x, Vector[String]().tell)
    val t1 = System.currentTimeMillis
    r flatMap { _ => Vector((t1 - t0).shows + " msec").tell }
  }

  def listFinalCountDown(x: Int): Writer[List[String], Unit] = {
    import annotation.tailrec
    @tailrec def doFinalCountDown(x: Int, w: Writer[List[String], Unit]): Writer[List[String], Unit] = x match {
      case 0 => w flatMap { _ => List("0").tell }
      case x => doFinalCountDown(x - 1, w flatMap { _ =>
        List(x.shows).tell
      })
    }
    val t0 = System.currentTimeMillis
    val r = doFinalCountDown(x, List[String]().tell)
    val t1 = System.currentTimeMillis
    r flatMap { _ => List((t1 - t0).shows + " msec").tell }
  }

  def main (args: Array[String]): Unit = {
    println(3.some >> 4.some)
    println(3.some >>= { x => "!".some >>= { y => (x.shows + y).some } })

    println(for {
              x <- 3.some
              y <- "!".some
            } yield (x.shows + y)
           )

    println(for {
              n <- List(1, 2)
              ch <- List('a', 'b')
            } yield (n, ch)
           )

    println(List(1, 2, 3) <+> List(4, 5, 6))

    println((1 |-> 50) filter { x => x.shows contains '7' })

    println(KnightPos(6,2).possibleMoves)

    println(KnightPos(6, 2) canReachIn3 KnightPos(6, 1))
    println(KnightPos(6, 2) canReachIn3 KnightPos(7, 3))

    println((3, "Smallish gang.") applyLog isBigGang)
    println(3.set("smallish gang"))
    println("smallish gang".tell)

    println(multiWithLog run)

    println(gcd(8, 3).run)
    println(gcd2(8, 3).run)

    println(vectorFinalCountDown(10000).run._1.last)
    println(listFinalCountDown(10000).run._1.last)

    val f = ({(_: Int) * 2} |@| {(_: Int) + 10}) {_ + _}
    println(f(3))

    val a = Kleisli { (x: Int) => (x + 1).some }
    val b = Kleisli { (x: Int) => (x * 100).some }

    println(4.some flatMap (a <=< b))
  }
}
