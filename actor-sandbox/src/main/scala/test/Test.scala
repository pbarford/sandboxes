package test

import scala.util.Try


object Test {

  val stringOne = new StringOne()
  val stringTwo = new StringTwo()

  val chain = stringOne -> stringTwo

  def convertItoS(i:Int):String = i.toString

  def process[A,B](i:A)(f: A => B):B = f(i)

  case class Person(name:String, age:Int)

  def processAble(i:Int):Boolean = process(i) { x => x > 10 }

  def main(args: Array[String]) {
    chain.on("dfdfd") match {
      case Right(res) => println(res)
      case Left(err) => println(s"error : $err")
    }

    println(process(2)(convertItoS))
    println(processAble(2))

    val getAge = (p:Person) => process(p) { x => x.age }
    val isOver30 = (age:Int) => process(age) { a =>  a > 30}
    val isUnder30 = (age:Int) => process(age) { a =>  a < 30}
    val personIsUnder30 = getAge andThen isUnder30
    val personIsOver30 = getAge andThen isOver30


    val r1 = personIsUnder30(Person("damian", 28))
    val r2 = personIsOver30(Person("paul", 42))

    List(2,3,4,5).map(x => Some(x + 2)).flatten

  }



}

