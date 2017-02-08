package test

import test.Model.{Bet, Leg, WinOnlyBet, WinOnlyLeg}
import test.Util.{CopyBet, CopyLeg}

object Model {

  sealed trait Bet {
    def id:String
    def leg:Leg
  }

  sealed trait Leg {
    def id: String
    def legNo: Int
  }

  case class WinOnlyBet(id:String, leg:WinOnlyLeg) extends Bet
  case class EachWayBet(id:String, leg:EachWayLeg) extends Bet

  case class WinOnlyLeg(id:String, legNo:Int) extends Leg
  case class EachWayLeg(id:String, legNo:Int, terms:Int) extends Leg
}

object Util {
  trait CopyLeg[A <: Leg] {
    def updateLeg(v: A, leg : Int) : Any
  }

  trait CopyBet[A <: Bet] {
    def updateBet(v: A, leg : Leg) : Any
  }
}

object CopyDefaults {
  /*
  implicit val winOnlyLeg2Update = new  Copy[WinOnlyLeg] {
    override def updateLeg(v:WinOnlyLeg, leg: Int): Any = {
      v.copy(legNo = leg)
    }
  }
  */



  implicit val legUpdate = new CopyLeg[Leg] {
    override def updateLeg(v: Leg, no: Int): Any = {
      import copySyntax._
      v.copy(legNo = no)
    }
  }

  implicit val betUpdate = new CopyBet[Bet] {
    override def updateBet(v: Bet, update: Leg): Any = {
      import copySyntax._
      v.copy(id = "324324")
      //v.copy(leg = update)
    }
  }
}

object CopySyntax2 {
  implicit class CopyLegS[A <: Leg](value : A) {
    def update(no:Int)(implicit c : CopyLeg[A]): Any = {
      c.updateLeg(value, no)
    }
  }

  implicit class CopyBetS[A <: Bet](value : A) {
    def update(leg:Leg)(implicit c : CopyBet[A]): Any = {
      c.updateBet(value, leg)
    }
  }
}

object CopyTest extends App {
  val x = WinOnlyLeg("xxxx", 1)
  import CopyDefaults._
  import CopySyntax2._
  println(x.asInstanceOf[Leg].update(3))

  val y = WinOnlyLeg("yyyy", 1)
  println(y.asInstanceOf[Leg].update(2))

  val b1 = WinOnlyBet("yyyy", x)
  println(b1.asInstanceOf[Bet].update(WinOnlyLeg("232323", 34)))

}