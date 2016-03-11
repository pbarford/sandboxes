package com.paddypower.destmgmt.validator

import com.paddypower.destmgmt.model._

import scalaz._
import Scalaz._


object Validator {

  sealed trait Result[+A]{
    def ap[B](fn: Result[A => B]): Result[B] =
      (this, fn) match {
        case (Pass(a), Pass(b)) => Pass(b(a))
        case (Fail(a), Pass(b)) => Fail(a)
        case (Pass(a), Fail(b)) => Fail(b)
        case (Fail(a), Fail(b)) => Fail(a ++ b)
      }

    def and[B, C](that: Result[B])(func: (A, B) => C): Result[C] =
      ap(that.map((b: B) => (a: A) => func(a, b)))

    def or[B, C](that: Result[B])(func: (A, B) => C): Result[C] =
      ???

    def map[B](func: A => B):Result[B] = this match {
      case Pass(a) => Pass(func(a))
      case Fail(a) => Fail(a)
    }

    def flatMap[B](func: A => Result[B]) = this match {
      case Pass(a) => func(a)
      case Fail(a) => Fail(a)
    }
  }

  final case class Pass[A](value: A) extends Result[A]
  final case class Fail(messages: List[String]) extends Result[Nothing]

  type ARule[-A, +B] = A => Result[B]


  implicit class RuleOps[A, B](rule: ARule[A, B]) {
    def map[C](func: B => C): ARule[A, C] =
      (a: A) => rule(a) map func

    def flatMap[C](rule2: ARule[B, C]): ARule[A, C] =
      (a: A) => rule(a) flatMap rule2

    def and[C, D](rule2: ARule[A, C])(func: (B, C) => D): ARule[A, D] =
      (a: A) => (rule(a) and rule2(a))(func)

    def or[C, D](rule2: ARule[A, C])(func: (B, C) => D): ARule[A, D] =
      (a: A) => (rule(a) or rule2(a))(func)
  }


  def rule[A]: ARule[A, A] =
    (input: A) => Pass(input)


  private val nonEmptyString2: ARule[String, String] =
    (str: String) =>
      if(str.isEmpty) Fail(List("empty")) else Pass(str)

  private def nonEmptyString(failMsg:String): ARule[String, String] =
    (str: String) =>
      if(str.isEmpty) Fail(List(failMsg)) else Pass(str)

  private def isSomeString(failMsg:String): ARule[Option[String], String] =
  (str: Option[String]) =>
    str match {
      case None => Fail(List(failMsg))
      case Some(s) => Pass(s)
    }

  private def isString(expected:String, failMsg:String): ARule[String, String] =
    (str: String) =>
      if(str != expected) Fail(List(s"$failMsg failed with value $str")) else Pass(str)

  private def stringIsIn(expected: List[String], field:String) = (value:String) =>
    if(!expected.contains(value)) Fail(List(s"Action expected : $expected")) else Pass(value)

  def checkAction:ARule[Event, String] =
    rule[Event] map (_.action) flatMap checkIsRefreshAction

  def checkIsUpdateAction:ARule[String, String] =
    isString("UPDATE", "action")

  def checkIsRefreshAction:ARule[String, String] =
    isString("REFRESH", "action")

  def checkEventName:ARule[Event, String] =
    rule[Event] map (_.name) flatMap isSomeString("name is required")

  def checkRefreshEventDescriptor:ARule[EventDescriptor, String] =
   rule[EventDescriptor] map(_.typeName) flatMap isSomeString("Refresh requires typeName")


  def refreshCheck:ARule[Event, Event] = {
    (event) =>
      checkAction(event) ap {
        checkRefreshEventDescriptor(event.eventDescriptor) ap {
          checkEventName(event) ap {
            Pass {
              (action) =>
                (eventDescriptor) =>
                  (name) =>
                  event
            }
          }
        }
      }
  }

  val validate: ARule[Event, Event] =
    (event) =>
      checkAction(event) ap {
        Pass {
          (action: String) =>
          event
        }
      }


  def main(args: Array[String]) {
    println(refreshCheck(Event(22323, "REFRESH", EventDescriptor(2,23, Some("testType")), Some("name"))))


    val f = ((x: Int) => x + 1) map {_ * 7}
    val x = List(1, 2, 3, 4) map {(_: Int) * (_:Int)}.curried
    println(f(3))
    println(x map {_(9)})
    println(1.point[Option])
    println((((_: Int) * 3) map {_ + 100}) (1))
    println(1.some <* 2.some)
    println(9.some <*> {(_: Int) + 3}.some)

    val d = ({(_: Int) * 2} |@| {(_: Int) + 5}) {_ + _}

    println(d(10))

    println(("event 1 ok".successNel[String] |@| "event 2 failed!".failureNel[String] |@| "event 3 failed!".failureNel[String]) {_ + _ + _})
  }
}
