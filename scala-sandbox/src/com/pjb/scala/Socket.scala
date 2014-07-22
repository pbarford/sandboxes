package com.pjb.scala

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Socket {

  val integers = new Generator[Int] {
    val rand = new java.util.Random
    def generate = rand.nextInt()
  }

  val booleans = new Generator[Boolean] {
    def generate = integers.generate > 0
  }

  def readFromMemory() : Future[String] = Future {
    "testing"
  }
  def send(server: String, packet: String) : Future[String] = Future {
    if(booleans.generate) {
      "ok" + ":" + server
    } else {
      throw new Exception(server + ":failed")
    }
  }
  def sendTo(packet: String) : Future[String] = {
    send("europe", packet) fallbackTo {
      send("usa", packet)
    } recover {
      case error => error.getMessage
    }
  }

  def retries(no: Int)(block: => Future[String]): Future[String] = {
    if(no == 0) {
      Future.failed(new Exception("sorry"))
    } else {
      block fallbackTo {
        println("retries remaining : " + (no - 1))
        retries(no-1) { block }
      }
    }
  }

  def retryLeft[T](n: Int)(block: =>Future[T]): Future[T] = {
    val ns: Iterator[Int] = (1 to n).iterator
    val attempts: Iterator[()=>Future[T]] = ns.map(_ => ()=>block)
    val failed: Future[T] = Future.failed(new Exception)

    attempts.foldLeft(failed)((a, block) => a fallbackTo { block() })
  }

  def retryRight[T](n: Int)(block: =>Future[T]): Future[T] = {
    val ns: Iterator[Int] = (1 to n).iterator
    val attempts: Iterator[()=>Future[T]] = ns.map(_ => ()=>block)
    val failed: Future[T] = Future.failed(new Exception)
    attempts.foldRight(()=>failed)((block, a) => ()=> { block() fallbackTo{ a() }}) ()
  }

}