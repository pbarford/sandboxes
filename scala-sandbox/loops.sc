class test {
  def whileAwesome(conditional: => Boolean)(f: => Unit) {
    if (conditional) {
      f
      whileAwesome(conditional)(f)
    }
  }

  def doIt() {
    var count = 0
    whileAwesome(count < 5) {
      println("still awesome")
      count += 1
    }
  }
}

val t = new test
t.doIt()