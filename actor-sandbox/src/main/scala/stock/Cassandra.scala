package stock

import com.datastax.driver.core.{Statement, Session}

object Cassandra {
  case class Cassandra[A](s: Session => A) {
    def apply(x:Session) = s(x)
    def map[B](f: A => B): Cassandra[B] = {
      new Cassandra(c => f(s(c)))
    }

    def flatMap[B](f: A => Cassandra[B]):Cassandra[B] = {
      new Cassandra(c => f(s(c))(c))
    }
  }

  def pure[A](a: A):Cassandra[A] = Cassandra(c => a)

  implicit def cassandra[A](f: Session => A):Cassandra[A] = Cassandra(f)

  def execute(stmt:Statement):Session => Unit = {
    s => {
       s.execute(stmt)
    }
  }

  abstract class CassandraProvider {
    def apply[A](f: Cassandra[A]): A
  }
}
