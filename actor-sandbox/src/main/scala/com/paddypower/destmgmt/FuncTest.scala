package com.paddypower.destmgmt

object FuncTest {
  case class Person(name:String, age:Int)

  def process[A,B](i:A)(f: A => B):B = f(i)

  val getAge = (p:Person) => process(p) { x => x.age }

  val isOver30 = (age:Int) => process(age) { a =>  a > 30}

  val isUnder30 = (age:Int) => process(age) { a =>  a < 30}

  val personIsUnder30 = getAge andThen isUnder30

  val personIsOver30 = getAge andThen isOver30





  def main(args: Array[String]) {
      val r1 = personIsUnder30(Person("damian", 28))
      val r2  = personIsOver30(Person("paul", 42))

  }
}
