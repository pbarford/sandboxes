package week4

trait List[T] {
  def isEmpty : week4.Boolean
  def head : T
  def tail: List[T]
}

class Cons[T](val head: T, val tail: List[T]) extends List[T] {
  def isEmpty: week4.Boolean = False
}

class Nil[T] extends List[T] {
  def isEmpty: week4.Boolean = True
  def head: Nothing = throw new NoSuchElementException("Nil.head")
  def tail: Nothing = throw new NoSuchElementException("Nil.tail")
}

object List {
  def apply[T](x1: T, x2: T): List[T] = new Cons(x1, new Cons(x2, new Nil))
  def apply[T]() = new Nil
}