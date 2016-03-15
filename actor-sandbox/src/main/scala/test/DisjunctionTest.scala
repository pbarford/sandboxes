package test

import scalaz._
import Scalaz._

object DisjunctionTest {


  def main(args: Array[String]) {

    println(1.right[String].flatMap(x => (x+2).right))
  }
}
