package storm.fsm

import storm.fsm.SimpleCandyDispenser.{Coin, Machine, Turn, UnlockedMachine}


object SimpleCandyDispenser {

  sealed trait Input
  case object Coin extends Input
  case object Turn extends Input

  sealed trait Machine {
    def candies: Int
    def coins: Int
  }
  object Machine {
    def apply(candies: Int, coins: Int): Machine = LockedMachine(candies, coins)
  }
  case class LockedMachine(candies: Int, coins: Int) extends Machine
  case class UnlockedMachine(candies: Int, coins: Int) extends Machine

  val fsm =
    FSM[Input, Machine] {
      case (Coin, LockedMachine(candies, coins)) if candies > 0 =>
        UnlockedMachine(candies, coins + 1)

      case (Turn, UnlockedMachine(candies, coins)) if candies > 0 =>
        LockedMachine(candies - 1, coins)
    }


}

object Tester extends App {
  val machine = Machine(candies = 5, coins = 10)
  val inputs = List(
    Coin, // + 1 coin
    Turn,
    Coin
  )
  println(machine)
  println(SimpleCandyDispenser.fsm.run(inputs)(machine))
}