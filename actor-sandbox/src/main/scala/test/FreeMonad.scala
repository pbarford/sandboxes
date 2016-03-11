package test

import test.KVS._

object KVS {
  sealed trait KVS[A]

  case class Put[A](key: String, value:String, a:A) extends KVS[A]
  case class Get[A](key: String, h:String => A) extends KVS[A]
  case class Delete[A](key: String, a:A) extends KVS[A]
}

object FreeMonad {

  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  case class Done[F[_]:Functor, A](a: A) extends Free[F, A]
  case class More[F[_]:Functor, A](k: F[Free[F,A]]) extends Free[F, A]

  class Free[F[_], A](implicit fn: Functor[F]) {

    def flatMap[B](f: A => Free[F,B]):Free[F, B] =
      this match {
        case Done(a) => f(a)
        case More(k) => More(fn.map(k)(_ flatMap f))
      }

    def map[B](f: A => B): Free[F, B] =
      flatMap(x => Done(f(x)))
  }

  implicit val kvsFunctor: Functor[KVS] =
    new Functor[KVS] {
      def map[A,B](a: KVS[A])(f: A => B):KVS[B] = a match {
        case Put(k, v, a)=> Put(k, v, f(a))
        case Get(k, h) => Get(k, x => f(h(x)))
        case Delete(k, a) => Delete(k, f(a))
      }
    }

  def put(k: String, v: String): Free[KVS,Unit] =
    More(Put(k, v, Done(())))

  def get(k:String): Free[KVS, String] =
    More(Get(k, v => Done(v)))

  def delete(k:String): Free[KVS,Unit] =
    More(Delete(k, Done(())))

  def modify(k: String, f: String => String): Free[KVS, Unit] =
    for {
      v <- get(k)
      _ <- put(k, f(v))
    } yield ()


  def runKVS[A](kvs: Free[KVS, A],
                table: Map[String, String]):Map[String,String] =
    kvs match {
      case More(Put(k, v, a)) =>
        runKVS(a, table + (k -> v))
      case More(Get(k, f)) =>
        runKVS(f(table(k)), table)
      case More(Delete(k, a)) =>
        runKVS(a, table - k)
      case Done(a) => table
    }

  def main(args: Array[String]) {
    println("ok")
    val kvs = for {
      a <- put("test", "test")
      b <- put("test1", "test")
      c <- modify("test", s => s"mod $s")
    } yield List(a, b ,c)

    val t = runKVS(kvs, Map.empty)
    println(t)
  }

}
