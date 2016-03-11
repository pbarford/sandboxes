package test

object FList {

  sealed trait FList[+A] {
  }
  case object Nil extends FList[Nothing]
  case class Cons[+A](head: A, tail: FList[A]) extends FList[A]

  def sum(ints: FList[Int]):Int = foldRight(ints, 0)((x,y) => x + y)

  def product(ds: FList[Double]):Double = foldLeft(ds, 1.0)(_ * _)

  def foldRight[A,B](as: FList[A], z: B)(f: (A,B) => B): B = as match {
    case Nil => z
    case Cons(h, tail) => f(h, foldRight(tail, z)(f))
  }

  def foldLeft[A,B](as:FList[A], z:B)(f: (B, A)=> B) :B = as match {
    case Nil => z
    case Cons(h, tail) => foldLeft(tail, f(z, h))(f)
  }

  def length[A](as:FList[A]):Int = foldRight(as, 0)((x,y) => y +1 )

  def tail[A](xs:FList[A]):FList[A] = xs match {
    case Nil => Nil
    case Cons(_, xs) => xs
  }

  def setHead[A](x:A, xs:FList[A]):FList[A] = xs match {
    case Nil => Cons(x, Nil)
    case Cons(_, xs) => Cons(x,xs)
  }

  def drop[A](l: FList[A], n: Int): FList[A] =
    n match {
      case 0 => l
      case 1 => tail(l)
      case _ => drop(tail(l), n -1)
    }

  def dropWhile[A](xs: FList[A])(f: A => Boolean): FList[A] = xs match {
    case Cons(h, tail) => if(f(h)) dropWhile(tail)(f) else xs
    case _ => xs
  }

  def append[A](a1: FList[A], a2: FList[A]):FList[A] = a1 match {
    case Nil => a2
    case Cons(h,t) => Cons(h, append(t, a2))
  }

  def init[A](l: FList[A]):FList[A] = l match {
    case Cons(h, Nil) => Nil
    case Cons(h, t) => Cons(h, init(t))
  }



  def apply[A](as: A*):FList[A] =
    if(as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

}

object FListTest {
  def main(args: Array[String]) {
    println(FList.sum(FList(1,3,5,6)))
    println(FList.length(FList(1,3,5,6)))
    println(FList.product(FList(1,3,5,6)))
    println(FList.tail(FList(1,3,5,6)))
    println(FList.drop(FList(1,3,5,6), 2))
    println(FList.dropWhile(FList(1,3,5,6))(x => x < 4))
    println(FList.setHead(5, FList(1,3,5,6)))
    println(FList.init(FList(1,3,5,6)))
  }
}
