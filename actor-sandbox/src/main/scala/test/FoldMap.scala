package test

import scala.language.higherKinds
import scala.collection.Iterable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

import scalaz.Monoid
import scalaz.syntax.monoid._

object FoldMap {
  def foldMapP[A, B : Monoid](iter: Iterable[A])(f: A => B = (a: A) => a)(implicit ec: ExecutionContext): B = {
    val nCores: Int = Runtime.getRuntime().availableProcessors()
    val groupSize: Int = (iter.size.toDouble / nCores.toDouble).ceil.round.toInt

    val groups = iter.grouped(groupSize)
    val futures: Iterator[Future[B]] = groups map { group =>
      Future { group.foldLeft(mzero[B])(_ |+| f(_)) }
    }
    val result: Future[B] = Future.sequence(futures) map { iterable =>
      iterable.foldLeft(mzero[B])(_ |+| _)
    }

    Await.result(result, Duration.Inf)
  }

  // Specialised implementation for arrays that doesn't copy
  def foldMapP[A, B : Monoid](arr: Array[A])(f: A => B)(implicit ec: ExecutionContext): B = {
    def iter(idx: Int, end: Int, result: B): B =
      if(idx == end)
        result
      else
        iter(idx + 1, end, result |+| f(arr(idx)))

    val nCores: Int = Runtime.getRuntime().availableProcessors()
    val groupSize: Int = (arr.size.toDouble / nCores.toDouble).ceil.round.toInt

    val futures =
      for(i <- 0 until nCores) yield {
        Future {
          iter(i * groupSize, i * groupSize + groupSize, mzero[B])
        }
      }
    val result: Future[B] = Future.sequence(futures) map { iterable =>
      iterable.foldLeft(mzero[B])(_ |+| _)
    }

    Await.result(result, Duration.Inf)
  }

  def foldMap[A, B : Monoid](iter: Iterable[A])(f: A => B = (a: A) => a): B =
    iter.foldLeft(mzero[B])(_ |+| f(_))

  implicit class IterableFoldMappable[A](iter: Iterable[A]) {
    def foldMapP[B : Monoid](f: A => B = (a: A) => a)(implicit ec: ExecutionContext): B =
      FoldMap.foldMap(iter)(f)

    def foldMap[B : Monoid](f: A => B = (a: A) => a): B =
      FoldMap.foldMap(iter)(f)
  }

  implicit class ArrayFoldMappable[A](arr: Array[A]) {
    def foldMapP[B : Monoid](f: A => B = (a: A) => a)(implicit ec: ExecutionContext): B =
      FoldMap.foldMap(arr)(f)

    def foldMap[B : Monoid](f: A => B = (a: A) => a): B =
      FoldMap.foldMap(arr)(f)
  }

  def main(args: Array[String]) {

  }
}