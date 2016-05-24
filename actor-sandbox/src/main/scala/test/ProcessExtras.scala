package test

import scalaz.stream.Process._
import scalaz.{\/-, -\/, \/}
import scalaz.concurrent.Task
import scalaz.stream.Process


object ProcessExtras {
  implicit class ProcessExtrasSyntax[A,B](p:Process[Task, A \/ B]) {
    def or(onLeft: A => Process[Task, Nothing]): Process[Task, B] = p.flatMap {
      case -\/(a) => onLeft(a)
      case \/-(b) => emit(b).toSource
    }
  }
}
