package test

trait Monad[F[_]] extends Applicative[F] { self =>

  def pure[A](a:A):F[A]

  def flatMap[A,B](fa: F[A])(f : A => F[B]) : F[B]

  override def apply[A, B](fa: F[A])(ff: F[A => B]):F[B] = flatMap(ff)((f: A => B) => map(fa)(f))

  override def map[A,B](fa: F[A])(f: A => B): F[B] = flatMap(fa)((a:A) => pure(f(a)))

  def flatten[A,B](ffa: F[F[A]]) : F[A] = flatMap(ffa)(fa => fa)

}

trait MonadLaws[F[_]] {

}

object Monad {
  implicit val listMonad: Monad[List] = new Monad[List] {

    override def pure[A](a: A): List[A] = List(a)

    override def flatMap[A, B](fa: List[A])(f: (A) => List[B]): List[B] = fa.flatMap(f)
  }

  implicit val optionMonad: Monad[Option] = new Monad[Option] {

    override def pure[A](a: A): Option[A] = Option(a)

    override def flatMap[A, B](fa: Option[A])(f: (A) => Option[B]): Option[B] = fa.flatMap(f)
  }
}

object MonadTest {
  def main(args: Array[String]) {
    val m1 = Monad.optionMonad
    val m2 = Monad.listMonad

    println(m2.flatMap(List(1,2,3,4))(x => List.fill(x)(x)))

    println(m1.flatMap(Option(1))(x => if(x > 1 )Some(x) else None))
  }
}
