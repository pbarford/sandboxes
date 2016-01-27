package crap

import scala.annotation.tailrec

object Test {
  class Decorator(left: String, right: String) {
    def layout[A](x: A) = left + x.toString() + right
  }
  def apply(f: Int => String, v: Int) = f(v)

  def main(args: Array[String]) {

    val decorator = new Decorator("[", "]")
    println(apply(decorator.layout, 7))

    //assert(product(noOp)(2, 4) == linearProduct(2, 4))
    //assert(product(noOp)(3, 6) == linearProduct(3, 6))

    testSum
    //testProduct
  }

  def testSum = {
    def plus(a: Int, b: Int) = a + b

    val result0 = operate(noOp)(plus)(2, 4, 0)
    val result1 = sum(noOp)(2,4)
    assert(result0 == result1)
  }

  def testProduct = {
    def multiply(a: Int, b: Int) = a * b

    val result0 = operate(noOp)(multiply)(2, 4, 1)
    val result1 = product(noOp)(2, 4)

    assert(result0 == result1)
  }

  def noOp(x: Int) = {
    x
  }

  def linearProduct(a: Int, b: Int): Int =
    if (a > b) 1
    else a * linearProduct(a + 1, b)

  def sum(f: Int => Int)(a: Int, b: Int): Int = {
    @tailrec def iter(a: Int, result: Int): Int = {
      if (a > b) result
      else iter(a + 1, result + f(a))
    }
    iter(a, 0)
  }

  def product(f: Int => Int)(a: Int, b: Int): Int = {
    @tailrec def iter(a: Int, result: Int): Int = {
      if (a > b) result
      else iter(a + 1, result * f(a))
    }
    iter(a, 1)
  }



  def operate(f: Int => Int)
             (oper: (Int, Int) => Int)
             (a: Int, b: Int, init: Int): Int = {

    @tailrec def iter(a: Int, result: Int): Int = {
      if (a > b) result
      else iter(a + 1, oper(result, f(a)))
    }

    iter(a, init)
  }

}
