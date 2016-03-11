package test

object HoF {

  type Pred[A] = A => Boolean

  def lift[A]( f: (Boolean, Boolean) => Boolean):(Pred[A], Pred[A]) => Pred[A] =
    (p1, p2) => a => f(p1(a), p2(a))

  def not[A](p: Pred[A]):Pred[A] =
    a => !p(a)

  def or[A]:(Pred[A],Pred[A]) => Pred[A] =
    lift(_ || _)

  def and[A]:(Pred[A],Pred[A]) => Pred[A] =
    lift(_ && _)

  def isDivisibleBy(i:Int):Pred[Int] =
    a => a % i == 0

  val isEven:Pred[Int] = isDivisibleBy(2)

  def main(args: Array[String]) {

    val res = and(isEven, not(isDivisibleBy(5)))

    println(isEven(3))
    println(res(8))
    println(res(10))
  }
}


