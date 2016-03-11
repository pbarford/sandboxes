package test

object Messing {

  final case class RulePass[A](value: A) extends Rule[A]
  final case class RuleFailure(messages: List[String]) extends Rule[Nothing]

  trait Rule[+A] {

    def map[A,B](ra:Rule[A])(f: A => B):Rule[B] = ra match {
      case RulePass(a) => RulePass(f(a))
      case RuleFailure(a) => RuleFailure(a)
    }

    def flatMap[A,B](ra: Rule[A])(f : A => Rule[B]) : Rule[B] = ra match {
      case RulePass(a) => f(a)
      case RuleFailure(a) => RuleFailure(a)
    }


    def and[B, C](that: Rule[B])(func: (A, B) => C): Rule[C] = {
      //that.map((b: B) => (a: A) => func(a, b))
      ???
    }



    //def or[A,B](ra:Rule[A])(f)

  }

  def rule[A]: ARule[A, A] = (input: A) => RulePass(input)

  type ARule[-A, +B] = A => Rule[B]


  implicit class ARuleOps[A, B](rule: ARule[A, B]) {
  }


  def check3:ARule[String, String] =
    (v: String) =>
    if(v.contains("hello")) RulePass(v) else RuleFailure(List("invalid on check 1"))

  def check4:ARule[String, String] =
    (v: String) =>
      if(v.contains("paul")) RulePass(v) else RuleFailure(List("invalid on check 1"))

  def check1(v:String):Rule[String] =
    if(v.contains("hello")) RulePass(v) else RuleFailure(List("invalid on check 1"))

  def check2(v:String):Rule[String] =
    if(v.contains("paul")) RulePass(v) else RuleFailure(List("invalid on check 2"))

  def main(args: Array[String]) {

    val s = check1 _
    val c = List(check1 _ , check2 _)

    println(c.map(_ apply "tesdt pdaul hdello").flatMap {
      x => x match
      {
        case RulePass(x) => List.empty
        case RuleFailure(x) => x
      }
    })
    println(c.map(_ apply "test paudl helldo" ))



  }

}
