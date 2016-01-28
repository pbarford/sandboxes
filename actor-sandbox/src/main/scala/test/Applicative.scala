package test

trait Applicative[F[_]] extends Functor[F] {

  def pure[A](a:A):F[A]

  def apply[A,B](fa: F[A])(ff: F[A => B]):F[B]

  def apply2[A,B,Z](fa: F[A], fb: F[B])(ff: F[(A,B) => Z]):F[Z] =
    apply(fa)(apply(fb)(map(ff)(f => b => a => f(a,b))))

  override def map[A,B](fa:F[A])(f: A => B):F[B] =
    apply(fa)(pure(f))

  def map2[A,B,Z](fa:F[A], fb:F[B])(f: (A,B) => Z):F[Z] =
    apply(fa)(map(fb)(b => f(_, b)))

  def map3[A,B,C,Z](fa:F[A], fb:F[B], fc:F[C])(f: (A,B,C) => Z):F[Z] =
    apply(fa)(map2(fb, fc)((b, c) => a => f(a, b, c)))

  def map4[A,B,C,D, Z](fa:F[A], fb:F[B], fc:F[C], fd:F[D])(f: (A,B,C,D) => Z):F[Z] =
    //apply(fa)(map3(fb, fc, fd)((b, c, d)  => a => f(a, b, c, d)))
    map2(tuple2(fa, fb), tuple2(fc, fd)) {
      case((a,b), (c,d)) => f(a,b,c,d)
    }

  def map5[A,B,C,D,E,Z](fa:F[A], fb:F[B], fc:F[C], fd:F[D], fe:F[E])(f: (A,B,C,D,E) => Z):F[Z] =
    map2(tuple3(fa, fb, fc), tuple2(fd, fe)) {
      case((a,b,c), (d,e)) => f(a,b,c,d,e)
    }

  def map6[A,B,C,D,E,G,Z](fa:F[A], fb:F[B], fc:F[C], fd:F[D], fe:F[E], fg:F[G])(f: (A,B,C,D,E,G) => Z):F[Z] =
    map2(tuple3(fa, fb, fc), tuple3(fd, fe, fg)) {
      case((a,b,c), (d,e,g)) => f(a,b,c,d,e,g)
    }

  def tuple2[A,B](fa:F[A], fb:F[B]):F[(A, B)] =
    map2(fa, fb)((a,b) => (a,b))

  def tuple3[A,B,C](fa:F[A], fb:F[B], fc: F[C]):F[(A, B, C)] =
    map3(fa, fb, fc)((a,b, c) => (a,b,c))
}

object Applicative {

  implicit val optionApplicative:Applicative[Option] = new Applicative[Option] {
    override def pure[A](a: A): Option[A] = Some(a)

    override def apply[A, B](fa: Option[A])(ff: Option[(A) => B]): Option[B] = (fa, ff) match {
      case (None, _) => None
      case (Some(a), None) => None
      case (Some(a), Some(f)) => Some(f(a))
    }
  }

  implicit val listApplicative:Applicative[List] = new Applicative[List] {
    override def pure[A](a: A): List[A] = List(a)

    override def apply[A, B](fa: List[A])(ff: List[(A) => B]): List[B] = {
      for {
        a <- fa
        f <- ff
      } yield f(a)
    }
  }
}

object ApplicativeTest {

  def main (args: Array[String]) {
    val a1 = Applicative.optionApplicative
    val a2 = Applicative.listApplicative

    println(a1.map(Some(1))(_ + 1))
    println(a2.map(List(1,2,3))(_ + 1))
    println(a1.map2(Option(2), Option(4))(_ + _))
    println(a2.map2(List(3,4,5), List(1,2,3))(_ + _))
    println(a1.map3(Option(2), Option(4), Option(1))(_ + _ + _))
    println(a1.map4(Option(2), Option(4), Option(1), Option(2))(_ + _ + _ + _))
    println(a1.map5(Option(2), Option(4), Option(1), Option(2), Option(2))(_ + _ + _ + _ + _))
    println(a1.map6(Option(2), Option(4), Option(1), Option(2), Option(2), Option(2))(_ + _ + _ + _ + _ + _))
    println(a1.tuple2(Option(2), Option(4)))
    println(a1.tuple3(Option(2), Option(4), Option(2)))
  }
}
