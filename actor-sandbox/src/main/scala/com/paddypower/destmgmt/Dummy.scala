package com.paddypower.destmgmt

/**
  * Created by paulo on 16/03/16.
  */

sealed trait Expr

case class Number(value: Int) extends Expr
case class Plus(leftExpr: Expr, rightExpr: Expr) extends Expr
case class Minus(leftExpr: Expr, rightExpr: Expr) extends Expr

case class UserF(val name:String)

trait Printable {
  def printSomewhere(value:String):Unit
}

trait PrintableWelcome {
  self: Printable =>
  def sayHi(value:String):Unit = printSomewhere(s"hi ${value}")
}

trait PrintToConsole1 extends Printable {
  override def printSomewhere(value:String):Unit = println(s"PrintToConsole1 --> $value")
}

trait PrintToConsole2 extends Printable {
  override def printSomewhere(value:String):Unit = println(s"PrintToConsole2 --> $value")
}

object Ex1 extends PrintableWelcome with PrintToConsole1 {
}

object Ex2 extends PrintableWelcome with PrintToConsole2 {
}


object Dummy {
  def evaluate(expr: Expr):Int = expr match {
    case Number(v) => v
    case Plus(l, r) => evaluate(l) + evaluate(r)
    case Minus(l, r) => evaluate(l) - evaluate(r)
  }

  def main(args: Array[String]) {
    println(evaluate(Plus(Number(2), Number(3))))
    println(evaluate(Plus(Minus(Number(5), Number(2)), Number(4))))

    Ex1.sayHi("barry")
    Ex2.sayHi("dec")

  }

}
