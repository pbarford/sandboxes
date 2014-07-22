package test

object Test {

  val stringOne = new StringOne()
  val stringTwo = new StringTwo()

  val chain = stringOne -> stringTwo

  def main(args: Array[String]) {
    chain.on("dfdfd") match {
      case Right(res) => println(res)
      case Left(err) => println(s"error : $err")
    }

  }

}

