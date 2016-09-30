package test

import scala.collection.immutable.SortedSet

object Sets {

  trait TradingCommand {
    val timeuuid: Int
  }

  case class NudgeCommand(timeuuid: Int) extends TradingCommand
  case class TickCommand(timeuuid: Int) extends TradingCommand

  implicit val tradingOrdering = order[TradingCommand]()
  //implicit val tickOrdering = order[TickCommand]()
  //implicit val nudgeOrdering = order[NudgeCommand]()

  def order[A<: TradingCommand](): Ordering[A] =  new Ordering[A] {
    override def compare(x: A, y: A): Int = {
      x.timeuuid - y.timeuuid
    }
  }


  def main(args: Array[String]) {


    val s = SortedSet[TradingCommand](NudgeCommand(12), NudgeCommand(11), TickCommand(6))

    println(s)
  }

}
