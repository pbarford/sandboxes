package com.paddypower.destmgmt.validator


import com.paddypower.destmgmt.model.{EventDescriptor, Event}

import scalaz._
import Scalaz._

object ScalazValidation {

  type StringValidation[T] = ValidationNel[String, T]

  def testAction(event:Event):ValidationNel[String, Event] =
    if(List("UPDATE", "REFRESH").contains(event.action)) event.successNel else "invalid action".failureNel

  def testNonEmpty(e:Event):ValidationNel[String, Event] =
    e.eventDescriptor.typeName match {
      case None => "empty field".failureNel
      case Some(v) => e.successNel
    }

  def checker(e:Event):ValidationNel[String,Event] = {
    val checks = List(testAction _, testNonEmpty _)
    checks.traverseU(_ apply e).map {
      case a => println(s"a=$a")
                e
    }
  }

  def main(args: Array[String]): Unit = {
    val e = Event(22323, "REFRESH", EventDescriptor(2,23, Some("testType")), Some("name"))

    println(checker(e))

    //
    //println(testAction(e.action) |@| testNonEmpty(e.eventDescriptor.typeName){ _ + _ } )
  }
}
