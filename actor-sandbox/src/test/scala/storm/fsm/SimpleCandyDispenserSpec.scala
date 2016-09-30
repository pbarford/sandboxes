package storm.fsm

import org.scalatest.FunSpec
import storm.fsm.SimpleCandyDispenser.{Coin, Input, Machine, Turn}

class SimpleCandyDispenserSpec extends FunSpec {

  case class Result(candiesDispensed: Int, coinsAccepted: Int)

  def simulate(inputs: List[Input])(machine: Machine): (Machine, Result) = {
    val newMachine = SimpleCandyDispenser.fsm.run(inputs).exec(machine)
    (newMachine, Result(machine.candies - newMachine.candies, newMachine.coins - machine.coins))
  }

  describe("The candy machine") {
    val machine = Machine(candies = 5, coins = 10)

    it("should accept coins and dispense candies") {
      val inputs = List(
        Coin, // + 1 coin
        Turn // - 1 candy
      )

      val (newMachine, result) = simulate(inputs)(machine)

      assert(machine.candies == 5, "The original machine remains untouched")
      assert(machine.coins == 10, "The original machine remains untouched")

      assert(newMachine.candies == 4, "The machine dispensed one candy")
      assert(newMachine.coins == 11, "The machine accepted one coin")

      assert(result.candiesDispensed == 1, "The machine dispensed one candy")
      assert(result.coinsAccepted == 1, "The machine accepted one coin")
    }

    it("should ignore inputs for the wrong state") {
      val inputs = List(
        Coin, // + 1 coin
        Turn, // - 1 candy
        Coin, // + 2 coins
        Coin, // ignored
        Turn, // - 2 candies
        Turn, // ignored
        Turn, // ignored
        Turn, // ignored
        Coin, // + 3 coins
        Coin, // ignored
        Turn // - 3 candies
      )

      val (newMachine, result) = simulate(inputs)(machine)

      assert(machine.candies == 5, "The original machine remains untouched")
      assert(machine.coins == 10, "The original machine remains untouched")

      assert(newMachine.candies == 2, "The machine dispensed four candies")
      assert(newMachine.coins == 13, "The machine accepted four coins")

      assert(result.candiesDispensed == 3, "The machine dispensed four candies")
      assert(result.coinsAccepted == 3, "The machine accepted four coins")
    }

    it("should ignore inputs when empty") {
      // get 6 candies
      val inputs = List.fill(6)(List(Coin, Turn)).flatten

      val (newMachine, result) = simulate(inputs)(machine)

      assert(machine.candies == 5, "The original machine remains untouched")
      assert(machine.coins == 10, "The original machine remains untouched")

      assert(newMachine.candies == 0, "The machine dispensed five candies")
      assert(newMachine.coins == 15, "The machine accepted five coins")

      assert(result.candiesDispensed == 5, "The machine dispensed only five candies")
      assert(result.coinsAccepted == 5, "The machine accepted only five coins")
    }
  }
}