package test

import scalaz.stream.Process

object StreamTest {

  def main(args: Array[String]) {
    val p = Process(5, 4, 3, 2, 1)

    val results = p collect {
      case 1 => "one"
      case 2 => "two"
      case 3 => "three"
    } filter { _.length > 3 } map { _.toUpperCase }

    println(results.toSource.runLog.unsafePerformSync)

    val names = Process("one", "two", "three")
    def nums(n: Int):Process[Nothing, Int] = Process(n) ++ nums(n + 1)

    val s = names zip nums(1)

    println(s.toSource.runLog.unsafePerformSync)
  }
}
