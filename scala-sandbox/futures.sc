import scala.concurrent.Future

import scala.util.{Failure, Success}
import com.pjb.scala.Generator

val integers = new Generator[Int] {
  val rand = new java.util.Random
  def generate = rand.nextInt()
}

val booleans = new Generator[Boolean] {
  def generate = integers.generate > 0
}

trait Socket {
 def readFromMemory() : Future[String] = Future {
   "testing"
 }
 def send(server: String, packet: String) : Future[String] = Future {
   if(booleans.generate)
    "ok" + ":" + server
   else
     throw new Error(server + ":failed")
 }
 def sendTo(packet: String) : Future[String] = {
   send("europe", packet) fallbackTo {
     send("usa", packet)
   } recover {
     case error => {
       println(error.getMessage)
       error.getMessage
     }
   }
 }
}
val stringSocket = new Socket {}
val packet = stringSocket.sendTo("hello these")
packet onComplete {
  case Success(p) =>  {
    println(p)
  }
  case Failure(t) => println("An error has occured: " + t.getMessage)
}


val s = packet.value
