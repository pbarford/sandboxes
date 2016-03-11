package test

sealed trait Validation[E, A] {
  def map[B](f: A => B):Validation[E, B]
  def flatMap[B](f: A => Validation[E, B]):Validation[E, B]
  def liftFail[F](f: E => F):Validation[F, A]
}

case class Success[E, A](a: A) extends Validation[E, A] {
  def map[B](f: A => B):Validation[E, B] = new Success(f(a))
  def flatMap[B](f: A => Validation[E, B]):Validation[E, B] = f(a)
  def liftFail[F](f: E => F):Validation[F, A] = new Success(a)
}

case class Failure[E,A] (e: E) extends  Validation[E, A] {
  def map[B](f: A => B):Validation[E, B] = new Failure(e)
  def flatMap[B](f: A => Validation[E, B]):Validation[E, B] = new Failure(e)
  def liftFail[F](f: E => F):Validation[F, A] = new Failure(f(e))
}

sealed trait AOption[A] {
  def map[B](f: A => B):AOption[B]
  def flatMap[B](f: A => AOption[B]):AOption[B]
}

case class BarException(msg:String)
case class BazException(msg:String)
case class ComputeException(msg:String)

case class ASome[A](a:A) extends AOption[A] {
  override def map[B](f: A => B):AOption[B] = new ASome(f(a))
  override def flatMap[B](f: A => AOption[B]):AOption[B] = f(a)
}

case class ANone[A]() extends AOption[A] {
  override def map[B](f: A => B):AOption[B] = new ANone
  override def flatMap[B](f: A => AOption[B]):AOption[B] = new ANone
}

class Foo(abar : AOption[Bar]) { def bar: AOption[Bar] = abar }
class Bar(abaz: AOption[Baz]) { def baz: AOption[Baz]  = abaz }
class Baz { def compute:Int = 45 }

class Foo2(maybeBar : AOption[Bar2]) {
  def bar: Validation[BarException, Bar2] = maybeBar match {
      case ASome(b) => new Success(b)
      case _ => new Failure(BarException("no bar dude"))
    }
}

class Bar2(maybeBaz: AOption[Baz2]) {
  def baz: Validation[BazException, Baz2] = maybeBaz match {
    case ASome(b) => new Success(b)
    case _ => new Failure(BazException("no baz dude"))
  }
}

class Baz2 {
  def compute:Validation[ComputeException, Int] = new Success(45)
}

class Foo3(bars : List[Bar3]) { def bar: List[Bar3] = bars }
class Bar3(bazs: List[Baz3]) { def baz: List[Baz3]  = bazs }
class Baz3 { def computeAll:List[Int] = List(3) }



object MonadTest2 {

  def computeBaz(baz:Baz):Int = baz.compute
  def computeBar(bar:Bar):AOption[Int] = bar.baz.map { b:Baz => computeBaz(b) }
  def computeFoo(foo:Foo):AOption[Int] = foo.bar.flatMap { b:Bar => computeBar(b)}

  def compute(maybeFoo:AOption[Foo]):AOption[Int] = maybeFoo.flatMap { f:Foo => computeFoo(f) }

  def compute2(maybeFoo:AOption[Foo]):AOption[Int] = maybeFoo.flatMap ((f:Foo) => computeFoo(f))

  def compute3(maybeFoo:AOption[Foo]):AOption[Int] =
    maybeFoo.flatMap {
      foo => foo.bar.flatMap {
        bar => bar.baz.map {
          baz => baz.compute
        }
      }
    }

  def compute3a(maybeFoo:AOption[Foo]):AOption[Int] =
    for {
      foo <- maybeFoo
      bar <- foo.bar
      baz <- bar.baz
    } yield baz.compute

  def compute4(foo: Foo2):Validation[ComputeException, Int] = {
    foo.bar.liftFail { e => new ComputeException(e.msg)}.flatMap {
      bar => bar.baz.liftFail { e => new ComputeException(e.msg)}.flatMap {
        baz => baz.compute
      }
    }
  }

  def compute4a(foo: Foo2):Validation[ComputeException, Int] = {
    for {
      bar <- foo.bar.liftFail { e => new ComputeException(e.msg) }
      baz <- bar.baz.liftFail { e => new ComputeException(e.msg) }
      res <- baz.compute
    } yield res
  }


  def goodFoo:AOption[Foo] =  ASome(new Foo(ASome(new Bar(ASome(new Baz())))))

  def goodFoo2:AOption[Foo] =  for {
    baz <- ASome(new Baz)
    bar <- ASome(new Bar(ASome(baz)))
    foo <- ASome(new Foo(ASome(bar)))
  } yield foo

  def goodFoo3:Foo2 = new Foo2(ASome(new Bar2(ASome(new Baz2()))))
  def badFoo1:Foo2 = new Foo2(ASome(new Bar2(ANone[Baz2])))
  def badFoo2:Foo2 = new Foo2(ANone[Bar2])

  def goodFoo4:List[Foo3] = List(new Foo3(List(new Bar3(List(new Baz3, new Baz3)))))

  def computeAll(foos:List[Foo3]):List[Int] =
    for {
      foo <- foos
      bar <- foo.bar
      baz <- bar.baz
      res <- baz.computeAll
    } yield res


  def main(args: Array[String]) {

    val res = for {
      bar <- goodFoo
      baz <- bar.bar
    } yield baz.baz.map { b => b.compute }

    println(res)
    println(compute(goodFoo2))
    println(compute2(goodFoo2))
    println(compute3a(goodFoo2))
    println(compute4a(goodFoo3))
    println(compute4(badFoo1))
    println(compute4(badFoo2))
    println(computeAll(goodFoo4))

  }
}
