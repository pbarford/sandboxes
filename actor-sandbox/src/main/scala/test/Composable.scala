package test

object Composable {

  def main(args: Array[String]) {
    val f = (i: Int) => i*3
    val g = (i: Int) => i.toString
    val tripleToString = f andThen g
    println(tripleToString(3))
  }

}


