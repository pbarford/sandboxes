package test

object Monoid {

  trait Monoid[A] {
    def op(a1: A, a2: A):A
    def zero: A

  }

  def endoMonoid[A]: Monoid[A => A] = new Monoid[A => A] {
    def op(f: A => A, g: A => A) = f compose g
    val zero = (a: A) => a
  }

  def dual[A](m: Monoid[A]): Monoid[A] = new Monoid[A] {
    def op(x: A, y: A): A = m.op(y, x)
    val zero = m.zero
  }

  def foldMap[A,B](as:List[A], m : Monoid[B])(f: A => B): B =
    as.foldLeft(m.zero)((b,a) => m.op(b, f(a)))

  def foldLeft[A, B](as: List[A])(z: B)(f: (B, A) => B): B =
    foldMap(as, dual(endoMonoid[B]))(a => b => f(b, a))(z)

  def foldRight[A, B](as: List[A])(z: B)(f: (A, B) => B): B =
    foldMap(as, endoMonoid[B])(f.curried)(z)

  val stringMonoid = new Monoid[String] {
    override def op(a1: String, a2: String): String = s"$a1$a2"
    override def zero: String = ""

  }

  def listMonoid[A] = new Monoid[List[A]] {
    override def op(a1: List[A], a2: List[A]): List[A] = a1 ++ a2
    override def zero: List[A] = List.empty
  }

  def foldMapV[A, B](as: IndexedSeq[A], m: Monoid[B])(f: A => B): B =
    if (as.length == 0)
      m.zero
    else if (as.length == 1)
      f(as(0))
    else {
      val (l, r) = as.splitAt(as.length / 2)
      m.op(foldMapV(l, m)(f), foldMapV(r, m)(f))
    }

  def main(args: Array[String]) {

    println(stringMonoid.op("test", "done"))
    println(List("test", "done").foldLeft(stringMonoid.zero)(stringMonoid.op))
    println(List("test", "done").foldRight(stringMonoid.zero)(stringMonoid.op))
    println(foldMap(List(20.0, 3.0), stringMonoid)(a => a.toString))
    println(listMonoid.op(List(2,3,6), List(4,6,7)))

    println(foldRight(List("one", "two", "three"))("")((x,y) => x + y))
    println(foldLeft(List("one", "two", "three"))("")((x,y) => x + y))

    println(foldLeft(List(2.4, 4.5, 5.5))(0.0)((x,y) => x + y))



    println(foldMapV(IndexedSeq("one", "two", "three"), stringMonoid)(x => x.reverse))

  }
}
