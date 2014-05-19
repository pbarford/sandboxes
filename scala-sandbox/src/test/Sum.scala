package test

import scala.io._

object Sum {

  def toInt(in: String): Option[Int] = {
    try {
      Some(Integer.parseInt(in.trim))
    } catch {
      case e: NumberFormatException => None
    }
  }

  def sum2(xs: List[Int]): Int = xs.foldLeft(0)((a, b) => a + b)
  def max(xs: List[Int]): Int = xs.reduceLeft((a,b) => a max b)

  def sum(in: Iterator[String]) = {
    val ints = in.flatMap(s => toInt(s))
    ints.foldLeft(0)((a, b) => a + b)
  }

  def largest(i : Int*) : Int = i.reduceLeft((a,b) => a max b)

  def mkString[T](as: T*): String = as.foldLeft("")(_ + _.toString + " - ")

  def main(args: Array[String]) {

    println("Enter some numbers and press ctrl-D (Unix/Mac) ctrl-C (Windows)")
    val input = Source.fromInputStream(System.in)
    //val lines = input.getLines.collect { case s:String => s }
    val lines = input.getLines
    println("Sum "+sum(lines))
  }
}