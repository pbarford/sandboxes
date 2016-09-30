package monad

import monad.Book.Refinement.Result

import scalaz.\/
import scalaz.syntax.apply._
import scalaz._
import Scalaz._

object Book extends App {

  sealed trait Value {
    val name: String = this match {
      case Number(_) => "Number"
      case Chars(_) => "Chars"
    }
  }
  final case class Number(get: Double) extends Value
  final case class Chars(get: String) extends Value

  sealed trait Expression {
    def eval: Double = this match {
        case Plus(l, r) => l.eval + r.eval
        case Minus(l, r) => l.eval - r.eval
        case Multiply(l, r) => l.eval * r.eval
        case Divide(l, r) => l.eval / r.eval
        case Literal(v) => v
      }

    def +(that: Expression): Expression = Plus(this, that)
    def -(that: Expression): Expression = Minus(this, that)
    def *(that: Expression): Expression = Multiply(this, that)
    def /(that: Expression): Expression = Divide(this, that)
  }

  object Expression {
    def lit(in: Double): Expression = Literal(in)
  }

  object Refinement {

    type Result[A] = \/[String,A]

    type Refinement[I,O] = I => Result[O]

    implicit class RefinementOps[I](in: I) {
      def refine[O](implicit r: Refinement[I,O]): Result[O] = r(in)
    }
  }

  object Injection {

    type Injection[I,O] = I => O

    implicit class InjectionOps[I](in: I) {
      def inject[O](implicit i: Injection[I,O]): O = i(in)
    }
  }

  object Errors {
    def wrongTag[A](received: Value, expected: String): Result[A] =
      s"""|Expected value with tag $expected
          |but received value $received with tag ${received.name}""".stripMargin.left
  }

  object Refinements {
    import Refinement._

    type ValueRefinement[A] = Refinement[Value,A]

    def make[A](name: String)(f: PartialFunction[Value,A]): ValueRefinement[A] = {
      val lifted = f.lift
      (v: Value) => lifted(v).fold(Errors.wrongTag[A](v, name))(a => a.right)
    }
    implicit val doubleRefine: ValueRefinement[Double] = make[Double]("Number"){ case Number(d) => d }
    implicit val stringRefine: ValueRefinement[String] = make[String]("Chars"){ case Chars(s) => s }
  }

  object Injections {
    import Injection._
    type ValueInjection[A] = Injection[A,Value]
    implicit val injectDouble: ValueInjection[Double] = Number.apply _
    implicit val injectString: ValueInjection[String] = Chars.apply _
  }

/*
  object Lift {
    import Expression._
    import Refinement._
    import Refinements._
    import Injection._
    import Injections._

    def apply[A: ValueRefinement, B: ValueInjection](f: A => B)(a: Value): Result[Value] =
      (a.refine[A] map (a => f(a).inject[B]))

    def apply[A: ValueRefinement, B: ValueRefinement, C: ValueInjection](f: (A, B) => C)(a: Value, b: Value): Result[Value] =
      (a.refine[A] |@|  b.refine[B]) map ((a,b) => f(a,b).inject[C])
  }
*/

  final case class Plus(left: Expression, right: Expression) extends Expression
  final case class Minus(left: Expression, right: Expression) extends Expression
  final case class Multiply(left: Expression, right: Expression) extends Expression
  final case class Divide(left: Expression, right: Expression) extends Expression
  //final case class Append(left: Expression, right: Expression) extends Expression
  //final case class UpperCase(string: Expression) extends Expression
  //final case class LowerCase(string: Expression) extends Expression
  final case class Literal(get: Double) extends Expression

  import Expression._
  println((lit(1) + lit(3)).eval)

}
