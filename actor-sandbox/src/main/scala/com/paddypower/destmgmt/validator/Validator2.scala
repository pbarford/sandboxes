package com.paddypower.destmgmt.validator2

import com.paddypower.destmgmt.model.{EventDescriptor, Event}

object Validator2 {

  case class *~[+A, +B](a:A, b:B)

  val t: Int *~ String = *~(23, "")

  sealed trait Result[+A]{

    def ~[B](that: Result[B]): Result[A *~ B] =
      (this, that) match {
        case (Pass(a), Pass(b)) => Pass(*~(a, b))
        case (Fail(a), Pass(b)) => Fail(a)
        case (Pass(a), Fail(b)) => Fail(b)
        case (Fail(a), Fail(b)) => Fail(a ++ b)
      }

    def map[B](func: A => B) = this match {
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

  type Rule[-A, +B] = A => Result[B]


  implicit class RuleOps[A, B](rule: Rule[A, B]) {
    def map[C](func: B => C): Rule[A, C] =
      (a: A) => rule(a) map func

    def flatMap[C](rule2: Rule[B, C]): Rule[A, C] =
      (a: A) => rule(a) flatMap rule2

  }

  def rule[A]: Rule[A, A] =
    (input: A) => Pass(input)


  private val nonEmptyString2: Rule[String, String] =
    (str: String) =>
      if(str.isEmpty) Fail(List("empty")) else Pass(str)

  private def nonEmptyString(failMsg:String): Rule[String, String] =
    (str: String) =>
      if(str.isEmpty) Fail(List(failMsg)) else Pass(str)

  private def isSomeString(failMsg:String): Rule[Option[String], String] =
    (str: Option[String]) =>
      str match {
        case None => Fail(List(failMsg))
        case Some(s) => Pass(s)
      }

  private def isString(expected:String, failMsg:String): Rule[String, String] =
    (str: String) =>
      if(str != expected) Fail(List(s"$failMsg failed with value $str")) else Pass(str)

  private def stringIsIn(expected: List[String], field:String) = (value:String) =>
    if(!expected.contains(value)) Fail(List(s"Action expected : $expected")) else Pass(value)

  def checkAction:Rule[Event, String] =
    rule[Event] map (_.action) flatMap checkIsRefreshAction

  def checkIsUpdateAction:Rule[String, String] =
    isString("UPDATE", "action")

  def checkIsRefreshAction:Rule[String, String] =
    isString("REFRESH", "action")

  def checkEventName:Rule[Event, String] =
    rule[Event] map (_.name) flatMap isSomeString("name is required")


  def checkRefreshEventDescriptor:Rule[EventDescriptor, String] =
    rule[EventDescriptor] map(_.typeName) flatMap isSomeString("Refresh requires typeName")


  def refreshCheck:Rule[Event, Event] = {
    (event) =>
      val b=checkRefreshEventDescriptor(event.eventDescriptor)
      val c=checkEventName(event)

      (checkAction(event) ~ b ~ c) map {
        case a *~ b *~ z => event
      }
  }

  def main(args: Array[String]) {
    println(refreshCheck(Event(22323, "REFRESH", EventDescriptor(2,23, Some("testType")), Some("name"))))
  }
}
