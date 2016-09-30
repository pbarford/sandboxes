package state

import java.util.Random

import scalaz.State

object StateTest {

  def main(args: Array[String]) {

    val s = State[Int, String](i => (i +1, "str"))
    println(s.eval(1))
    println(s.exec(1))
    println(s(1))

    println(List.fill(10)(TwoDice()))
  }

  def dice() = State[Random, Int](r => (r, r.nextInt(6) + 1))

  def TwoDice() = for {
    r1 <- dice()
    r2 <- dice()
  } yield (r1, r2)

}
