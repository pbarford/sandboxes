package com.pjb.scala

import scala.concurrent.ExecutionContext.Implicits.global

object Futures {

  def main(args : Array[String]) {
    val stringSocket = new Socket {}
    //val packet = stringSocket.sendTo("hello these")
    //val packet = stringSocket.retries(2) {stringSocket.sendTo("hello these")}
    //val packet = stringSocket.retryLeft(2) {stringSocket.sendTo("hello these")}
    val packet = stringSocket.retryRight(2) {stringSocket.sendTo("hello these")}


    packet onSuccess {
      case res =>  println(res)
    }
    packet onFailure {
      case t => println("An error has occured: " + t.getMessage)
    }

    System.in.read()
  }






}
