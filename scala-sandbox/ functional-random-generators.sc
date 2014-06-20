trait Generator[+T] {
  self =>
  def generate: T

  def map[S](f: T => S): Generator[S] = new Generator[S] {
    def generate = f(self.generate)
  }

  def flatMap[S](f: T => Generator[S]): Generator[S] = new Generator[S] {
    def generate =f(self.generate).generate
  }
}

val integers = new Generator[Int] {
  val rand = new java.util.Random
  def generate = rand.nextInt()
}

val booleans = new Generator[Boolean] {
  def generate = integers.generate > 0
}

val b2 = for(x<-integers) yield x > 0
b2.generate
val pairs = new Generator[(Int, Int)] {
  def generate = (integers.generate, integers.generate)
}
val p2 = for {
            x <- integers
            y <- integers} yield(x,y)
p2.generate