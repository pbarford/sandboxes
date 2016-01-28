package test

trait Functor[F[_]] { self =>

  def map[A, B](fa: F[A])(f: A => B): F[B]

  def lift[A, B](f: A => B): F[A] => F[B] = fa => map(fa)(f)

  def as [A,B](fa:F[A], b: => B):F[B] = map(fa)(_ => b)

  def void[A](fa: F[A]):F[Unit] = as(fa, ())

  def compose[G[_]](implicit G: Functor[G]): Functor[Lambda[X => F[G[X]]]] =
    new Functor[Lambda[X => F[G[X]]]] {
      def map[A, B](fga: F[G[A]])(f: A => B) : F[G[B]] =
        self.map(fga)(ga => G.map(ga)(a => f(a)))
  }
}

trait FunctorLaws {
  def identity[F[_], A](fa: F[A])(implicit F: Functor[F]) =
    F.map(fa)(a => a) == fa

  def composition[F[_], A, B, C](fa:F[A], f: A => B, g: B => C)(implicit F: Functor[F]) =
    F.map(F.map(fa)(f))(g) == F.map(fa)(f andThen g)

}

object FunctorTest {

  implicit val listFunctor:Functor[List]  = new Functor[List] {
    def map[A, B](fa: List[A])(f: A => B): List[B] = {
      fa map f
    }
  }

  implicit val optionFunctor:Functor[Option]  = new Functor[Option] {
    def map[A, B](fa: Option[A])(f: A => B): Option[B] = {
      fa map f
    }
  }

  implicit def func1Functor[X]: Functor[X => ?] = new Functor[X => ?] {
    def map[A, B](fa: X => A )(f: A => B): X => B = fa andThen f

  }

  def main (args: Array[String]) {
    val f1 = implicitly[Functor[List]]
    val f2 = implicitly[Functor[Option]]

    println(f1.map(List(1, 2, 3))(_ + 1))
    println(f2.map(Some("1"))(java.lang.Double.parseDouble(_)))

    println(f1.void(List(1, 2, 3)))
    println(f1.as(List(1, 2, 3), 10))

    println(f1.lift {(_: Int) * 2}(List(3, 5, 9)))
    println(f2.lift{ x:String => s"test:$x "}(Some("hello")))

    val f3 = implicitly[Functor[Int => ?]]
    val r1 = f3.map(_ + 1)(_ + 2)
    println(r1(3))

    val f4 = f1 compose f2
    val xs:List[Option[Int]] = List(Some(1), Some(2))

    println(f4.map(xs)((_:Int) + 1))
  }
}
