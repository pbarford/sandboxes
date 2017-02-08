package streams

import shapeless._

sealed trait Diff[A]

final case class Identical[A](value: A) extends Diff[A]
final case class Different[A](left : A, right : A) extends Diff[A]

object Diff {
  def apply[A](left : A, right : A) : Diff[A] = {
    if (left == right) Identical(left)
    else Different(left, right)
  }
}

trait SimpleDelta[R <: HList] extends DepFn2[R, R] {
  type Out <: HList
}

object SimpleDelta {

  type Aux[I <: HList, O <: HList] = SimpleDelta[I]{type Out = O}
  implicit def hnilDelta: Aux[HNil, HNil] = new SimpleDelta[HNil] {
    type Out = HNil
    def apply(l : HNil, r: HNil) : Out = HNil
  }
  implicit def hconsDelta[H, T <: HList, DT <: HList](implicit tailDelta: Aux[T, DT])
  : Aux[H::T, Diff[H] :: DT] = new SimpleDelta[H :: T] {
    type Out = Diff[H] :: DT
    def apply(l : H :: T, r: H :: T) : Out =
      Diff(l.head, r.head) :: tailDelta(l.tail, r.tail)
  }
  def apply[A, R <: HList](l : A, r: A)
                          (implicit genA: Generic.Aux[A, R], delta: SimpleDelta[R]) : delta.Out =
    delta(genA.to(l), genA.to(r))
}

object SimpleDeltaSyntax {
  implicit class SimpleDeltaOps[A, R<:HList](val before: A) extends AnyVal {
    def merge(after: A)(implicit genA: Generic.Aux[A, R], delta: SimpleDelta[R]): delta.Out = delta(genA.to(before), genA.to(after))
  }
}

case class Address(number: Int, street: String, city: String)
case class Character(name: String, age: Int, address: Address)


object Shapeless2 extends App {

  val homer = Character("Homer Simpson", 42, Address(742, "Evergreen Terrace", "SpringField"))
  val ned = Character("Ned Flanders", 42, Address(744, "Evergreen Terrace", "SpringField"))

  println(SimpleDelta(homer, ned))

  import SimpleDeltaSyntax._
  println(homer.merge(ned))


  println(convert(homer))

  def convert[A, R<:HList](v:A)(implicit genA: Generic.Aux[A, R]) = genA.to(v)
}
