package storm.fsm

import scalaz.{State, Scalaz}, Scalaz._

object FSM {
  def apply[I, S](f: PartialFunction[(I, S), S]): FSM[I, S] =
    new FSM((i, s) => f.applyOrElse((i, s), (_: (I, S)) => s))

  private def states[S, O](xs: List[State[S, O]]): State[S, List[O]] =
    xs.sequence[({type λ[α]=State[S, α]})#λ, O]

  private def modify[I, S](f: (I, S) => S): I => State[S, Unit] =
    i => State.modify[S](s => f(i, s))
}

final class FSM[I, S] private (f: (I, S) => S) {
  def apply(is: List[I]): State[S, S] =
    FSM.states(is.map(FSM.modify(f))).flatMap(_ => State.get[S])

  def run(is: List[I]): State[S, S] =
    apply(is)
}