import scala.io.Source
import test.Sum._

class Foo
class Bar(name: String)
class Baz(name: String) {
  // constructor code is inline
  if (name == null) throw new Exception("Name is null")
}
trait Dog

class Fizz2(name: String) extends Bar(name) with Dog

trait Cat {
  def meow(): String = "meow"
}

class Person (val age: Int, val first: String, val valid: Boolean)

trait FuzzyCat extends Cat {
  override def meow(): String = "Meeeeeeow"
}

trait OtherThing {
  def hello():Int = 4
}

class Pussy extends Cat with OtherThing
class Yep extends FuzzyCat with OtherThing

object Dude extends Yep {
}
object Dude2 extends Yep {

  override def meow(): String = "Dude looks like a cat"
}

object OtherDude extends Yep {
  def twoMeows(param: Yep) = meow + ", " + param.meow
}

class HasYep {
  object myYep extends Yep {
    override def meow = "Moof"
  }
}

case class Stuff(name: String, age : Int)

class Up {
  def update(k : Int, v : String) = println("hey: key ["+ k + "], name --> " + v)
}

class Update {
  def update(what: String) = println("Singler: "+what)
  def update(a: Int, b: Int, what:String) = println("2d update")
}


object Dummy {
  def main(args: Array[String]) {
    println((new Pussy).meow())
    println((new Yep).meow())
    println((new Yep).hello())
    println(OtherDude.twoMeows(Dude))
    println(OtherDude.twoMeows(Dude2))
    println((new HasYep).myYep.meow())
    println(largest(45,5,67,234,3))
    println(mkString("hello", "world"))



    val a = new Up
    a(133) = "Paul"

    val u = new Update
    u() = "Foo"
    u(3,4) = "Howdy"
    fizzBuzz
    towerOfHanoi(5, "A", "B", "C")

    val s = Stuff("Paul", 40)
    val b = s match {
      case Stuff("Paul", 40) => true
      case _ => false
    }
    println(b)
    val howOld = s match {
      case Stuff("Paul", howOld) => howOld
      case _ => false
    }
    println(howOld)

    val x = s match {
      case Stuff("Paul", age) if age < 30 => "young Paul"
      case Stuff("Paul", _) => "old Paul"
      case _ => "Other"
    }
    println(x)


    println(tailRecursion(5))

    for {i <- 1 to 5 if isOdd(i)} println(i)

    for {i <- 1 to 5
         j <- 1 to 5 if isOdd(i * j)} println(i * j)

    val lst = (1 to 18 by 3).toList
    val lst2 = for {i <- lst if isOdd(i)} yield i
    println(lst2)

    println(with42(84 %))

    val w = List(1,2,3,4)
    val z = 1 :: 2 :: 3 :: 4 :: Nil
    println(w.filter(a => a % 2 == 0))
    println(z.filter(a => a % 2 == 0))
    println(z.filter(isOdd))
    println(w ::: z)
    println(x.filter(c => c match { case 'P' => false  case _ => true} ))
    println(x.filter(c => !c.isUpper))
    println(List("A", "Cat").map(s => s.toLowerCase))
    println(List("A", "Cat").map(_.toLowerCase))
    println(List("A", "Cat").map(_.length))
    println(List(99, 2, 1, 45).sortWith(_ < _))
    println(List("b", "a", "elwood", "archer").sortWith(_ < _))
    println(List("b", "a", "elwood", "archer").sortWith(_.length > _.length))
    println("99 Red Balloons".toList.filter(Character.isDigit))
    println("Elwood eats mice".takeWhile(c => c != ' '))

    println(sum2(List(1,4,5,6)))
    println(max(List(1,6,11,4,5,6)))

    val people = List(new Person(12, "Paul", true), new Person(8, "Alexandra", true), new Person(15, "Anita", true))
    println(validByAge(people))

    println(factorial(20))

    println("Enter some numbers and press ctrl-D (Unix/Mac) ctrl-C (Windows)")
    val input = Source.fromInputStream(System.in)

    //val lines = input.getLines.collect { case s:String => s }
    val lines = input.getLines
    println("Sum "+sum(lines))

  }

  def factorial(x : Int) :Long = if(x == 1) 1 else x * factorial(x - 1)

  def with42(in: Int => Int) = in(42)

  def towerOfHanoi(disk : Int, source : String, dest :String, spare : String) {

    if(disk == 0) {
      println("move disk [" + disk+ "] from : " + source + ", to :" + dest)
    }
    else {
      towerOfHanoi(disk - 1, source, spare, dest)
      println("move disk [" + disk + "] from : " + source + ", to :" + dest)
      towerOfHanoi(disk - 1, spare, dest, source)
    }
  }

  def isOdd(in: Int) :Boolean = in % 2 == 1

  def validByAge(in: List[Person]) = in.filter(_.valid).sortWith(_.age < _.age).map(_.first)

  def tailRecursion(x : Int) : Int = {
    def innerTail(x1: Int, total: Int) : Int = {
      if(x1 == 0) total
      else innerTail(x1 - 1, total + x1)
    }
    innerTail(x, 0)
  }

  def fizzBuzz()  {
    def innerFB(x : Int) {
      if(x % 3 == 0 && x % 5 == 0) println("FizzBuzz")
      else if(x % 3 == 0) println("Fizz")
      else if(x % 5 == 0) println("Buzz")
      else println(x)
      if (x != 100) innerFB(x + 1)
    }
    innerFB(1)
  }
}
