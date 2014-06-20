import scala.util.Try

trait Generator[+T] {
  self =>
  def generate: T
}

val integers = new Generator[Int] {
  val rand = new java.util.Random
  def generate = rand.nextInt()
}

val booleans = new Generator[Boolean] {
  def generate = integers.generate > 0
}

type Treasure = String
type Coin = Int
class Adventure {
  def collectCoins(): Try[List[Coin]] = {
    if(booleans.generate)
      throw new Error("Game Over - your dead")
    Try(List(1,2,4))
  }
  def buyTreasure(coins: List[Coin]): Try[Treasure] = {
    if(coins.sum < 4) {
      throw new Error("Game Over - not enough moolha")
    }
    Try("Gold")
  }
}

val adventure = new Adventure()
val treasure = for {
  c <- adventure.collectCoins()
  t <- adventure.buyTreasure(c)
} yield t