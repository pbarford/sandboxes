package test

import cats.{Id, ~>}
import cats.data.Coproduct
import cats.free._
import test.Domain._
import Interacts._

import scala.collection.mutable.ListBuffer

object Domain {
  sealed trait Interact[A]
  case class Ask(prompt: String) extends Interact[String]
  case class Tell(msg: String) extends Interact[Unit]

  sealed trait DataOp[A]
  case class AddCat(a: String) extends DataOp[String]
  case class GetAllCats() extends DataOp[List[String]]
}

class Interacts[F[_]](implicit I: Inject[Interact, F]) {
  def tell(msg: String): Free[F, Unit] = Free.inject[Interact, F](Tell(msg))
  def ask(prompt: String): Free[F, String] = Free.inject[Interact, F](Ask(prompt))
}

object Interacts {
  implicit def interacts[F[_]](implicit I: Inject[Interact, F]): Interacts[F] = new Interacts[F]
}

class DataOps[F[_]](implicit I: Inject[DataOp, F]) {
  def addCat(a: String): Free[F, String] = Free.inject[DataOp, F](AddCat(a))
  def getAllCats: Free[F, List[String]] = Free.inject[DataOp, F](GetAllCats())
}

object DataOps {
  implicit def dataOps[F[_]](implicit I: Inject[DataOp, F]): DataOps[F] = new DataOps[F]
}

object InteractInterpreter extends (Interact ~> Id) {
  def apply[A](i: Interact[A]) = i match {
    case Ask(prompt) => println(prompt); readLine()
    case Tell(msg) => println(msg)
  }
}

object InMemoryDataOpInterpreter extends (DataOp ~> Id) {
  private[this] val memDataSet = new ListBuffer[String]

  def apply[A](fa: DataOp[A]) = fa match {
    case AddCat(a) => memDataSet.append(a)
                      a
    case GetAllCats() => memDataSet.toList
  }
}

object FreeM extends App {

  type Application[A] = Coproduct[Interact, DataOp, A]

  def program(implicit I: Interacts[Application], D: DataOps[Application]): Free[Application, Unit] = {

    import I._, D._

    for {
      cat <- ask("What's the kitty's name?")
      _ <- addCat(cat)
      cats <- getAllCats
      _ <- tell(cats.toString)
      cat <- ask("What's the kitty's name?")
      _ <- addCat(cat)
      cats <- getAllCats
      _ <- tell(cats.toString)
    } yield ()

  }
  val interpreter: Application ~> Id = InteractInterpreter or InMemoryDataOpInterpreter

  val evaluated = program foldMap interpreter

}

