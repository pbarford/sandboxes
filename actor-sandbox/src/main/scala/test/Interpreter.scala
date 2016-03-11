package test

object Interpreter {

  sealed trait Exp[V] {

    def fold[A](lit: Boolean => A,
                and: (A, A) => A,
                not: A => A,
                or: (A, A) => A,
                lookup: V => A): A = {

      def go(x: Exp[V]): A = x match {
        case Lit(b) => lit(b)
        case And(a, b) => and(go(a), go(b))
        case Not(e) => not(go(e))
        case Or(a, b) => or(go(a), go(b))
        case Var(v) => lookup(v)
      }

      go(this)
    }
  }

  //def evaluate[A](e: Exp[A], env: A => Boolean):Boolean =
  //  e.fold(identity, _&&_, !_, _||_, env)

  //def replace[A,B](e:Exp[A], env: A => Exp[B]):Exp[B] =
  //  e.fold(Lit(_), And(_, _), Not(_), Or(_, _), env)

  case class Lit[A](s:Boolean) extends Exp[A]
  case class And[A](a: Exp[A], b:Exp[A]) extends Exp[A]
  case class Not[A](e:Exp[A]) extends Exp[A]
  case class Or[A](a:Exp[A], b:Exp[A]) extends Exp[A]
  case class Var[A](v:A) extends Exp[A]

  def main(args: Array[String]) {

  }
}
