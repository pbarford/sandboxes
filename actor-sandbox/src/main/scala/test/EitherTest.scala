package test


object EitherTest {

  case class ARight[+A, +B](b: B) extends AEither[A, B] {
    def isLeft = false
    def isRight = true
  }

  case class ALeft[+A, +B](a: A) extends AEither[A, B] {
    def isLeft = true
    def isRight = false
  }

  trait AEither[+E, +A] {
    def map[B](f: A => B): AEither[E, B] =
      this match {
        case ARight(a) => ARight(f(a))
        case ALeft(e) => ALeft(e)
      }
    def flatMap[EE >: E, B](f: A => AEither[EE, B]):AEither[EE, B] =
      this match {
        case ALeft(e) => ALeft(e)
        case ARight(a) => f(a)
      }
    def orElse[EE >: E, AA >: A](b: => AEither[EE, AA]):AEither[EE, AA] =
      this match {
        case ALeft(_) => b
        case ARight(a) => ARight(a)
      }
    def map2[EE >: E, B, C](b: AEither[EE, B])(f: (A,B) => C):AEither[EE, C] =
      for {
        a <- this;
        b1 <- b
      } yield f(a, b1)

  }

  def traverse[E, A, B](as: List[A])(f: A => AEither[E, B]): AEither[E, List[B]] = {
    as match {
      case Nil => ARight(Nil)
      case h::t => (f(h) map2 traverse(t)(f))(_ :: _)
    }
  }

  def sequence[E, A](es: List[AEither[E, A]]): AEither[E, List[A]] = traverse(es)(x => x)

  def mean(xs: IndexedSeq[Double]): AEither[String, Double] =
    if (xs.isEmpty)
      ALeft("mean of empty list!")
    else
      ARight(xs.sum / xs.length)

  def safeDiv(x: Int, y: Int): AEither[Exception, Int] =
    Try(x / y)

  def Try[A](a: => A): AEither[Exception, A] =
    try ARight(a)
    catch { case e: Exception => ALeft(e) }

  def main(args: Array[String]) {
    println(mean(IndexedSeq.empty))
    println(mean(IndexedSeq(3,5,7)))

    println(safeDiv(0, 0))
    println(safeDiv(5, 3))

    val s = List(safeDiv(10,2),safeDiv(6,2))

    println(sequence(s))
  }
}
