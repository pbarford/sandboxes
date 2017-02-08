package test

import scala.concurrent.{ExecutionContext, Future}

object MonadT extends App {

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

  def getCountryCode2(personId : String)(implicit ec:ExecutionContext): Future[Option[String]] = {
    findPerson(personId) flatMap { case Some(Person(_, Some(address))) =>
      findCountry(address.addressId) map { case Some(Country(code)) =>
        code
      }
    }
  }

}
