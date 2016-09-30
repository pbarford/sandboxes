package lens

import monocle.Lens


object Test extends App {
  case class Address(streetNumber: Int, streetName: String)
  case class Person(name: String, age: Int, address: Address)

  val _name = Lens[Person, String](_.name)(n => p => p.copy(name = n))
  val _age = Lens[Person, Int](_.age)(a => p => p.copy(age = a))
  val _address = Lens[Person, Address](_.address)(a => p => p.copy(address = a))
  val _streetNumber = Lens[Address, Int](_.streetNumber)(n => a => a.copy(streetNumber = n))



  val john = Person("John", 30, Address(126, "High Street"))
  println(_address composeLens _streetNumber get(john) )
  val j2 = (_address composeLens _streetNumber set(20))(john)
  println(_address composeLens _streetNumber get(j2) )
}
