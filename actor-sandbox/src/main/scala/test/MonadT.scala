package test

import test.MonadT.Result

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT
import scalaz.Scalaz._

object MonadT extends App {

  type Result[A] = OptionT[Future, A]

  case class Country(code:Option[String])
  case class Address(addressId:String, country: Option[Country])
  case class Person(name:String, address: Option[Address])

  def findPerson(id : String) : Future[Option[Person]] = ???
  def findCountry(addressId : String) : Future[Option[Country]] = ???

  def getCountryCode(person:Option[Person]) = for {
    p <- person
    a <- p.address
    c <- a.country
    code <- c.code
  } yield code

  def getCountryCode2(personId : String)(implicit ec : ExecutionContext): Future[Option[String]] = {
    findPerson(personId) flatMap { case Some(Person(_, Some(address))) =>
      findCountry(address.addressId) map { case Some(Country(code)) =>
        code
      }
    }
  }

  def getCountryCode3(personId:String)(implicit ec : ExecutionContext): Future[Option[String]] = {
    val result : OptionT[Future, String] = for {
      person <- OptionT(findPerson(personId))
      address <- OptionT(Future.successful(person.address))
      country <- OptionT(findCountry(address.addressId))
      code <- OptionT(Future.successful(country.code))
    } yield code

    result.run
  }

  /*
  def getCountryCode4(personId : String)(implicit ec : ExecutionContext): Future[Option[String]] = {

    object ? {
      def <~[A] (v : Future[Option[A]]) : Result[A] = OptionT(v)
      def <~[A] (v : Option[A]) : Result[A] = OptionT(Future.successful(v))
      def <~[A] (v : A) : Result[A] = v.point[Result]
    }

    val result : Result[String] = for {
      person <- ? <~ findPerson(personId)
      address <- ? <~ person.address
      country <- ? <~ findCountry(address.addressId)
      code <- ? <~ country.code
    } yield code

    result.run
  }
  */
}
