package streams

import shapeless._

sealed trait Diff2[A]

final case class Identical2[A](value: A) extends Diff2[A]
final case class Changed[A](v:A) extends Diff2[A]

object Diff2 {
  def apply[A](left : A, right : A) : Diff2[A] = {
    if (left == right) Identical2(left)
    else Changed(right)
  }
}

trait SimpleDelta2[R <: HList] extends DepFn2[R, R] {
  type Out <: HList
}

object SimpleDelta2 {

  type Aux[I <: HList, O <: HList] = SimpleDelta2[I]{type Out = O}
  implicit def hnilDelta: Aux[HNil, HNil] = new SimpleDelta2[HNil] {
    type Out = HNil
    def apply(l : HNil, r: HNil) : Out = HNil
  }
  implicit def hconsDelta[H, T <: HList, DT <: HList](implicit tailDelta: Aux[T, DT])
  : Aux[H::T, Diff2[H] :: DT] = new SimpleDelta2[H :: T] {
    type Out = Diff2[H] :: DT
    def apply(l : H :: T, r: H :: T) : Out =
      Diff2(l.head, r.head) :: tailDelta(l.tail, r.tail)
  }
  def apply[A, R <: HList](l : A, r: A)
                          (implicit genA: Generic.Aux[A, R], delta: SimpleDelta2[R]) : delta.Out =
    delta(genA.to(l), genA.to(r))
}

object SimpleDeltaSyntax2 {
  implicit class SimpleDeltaOps2[A, R<:HList](val before: A) extends AnyVal {
    def merge(after: A)(implicit genA: Generic.Aux[A, R], delta: SimpleDelta2[R]): delta.Out = delta(genA.to(before), genA.to(after))
  }
}


object Shapeless3 extends App {

  val homer = Character("Homer Simpson", 42, Address(742, "Evergreen Terrace", "SpringField"))
  val ned = Character("Ned Flanders", 42, Address(744, "Evergreen Terrace", "SpringField"))

  import SimpleDeltaSyntax2._
  println(homer.merge(ned))


}
