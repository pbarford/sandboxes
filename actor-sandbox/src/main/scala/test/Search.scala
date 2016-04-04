package test

object Search {

  sealed trait Exp {
    def fold[A](lit: String => A,
                and: (A,A) => A,
                many: A => A,
                or: (A,A) => A): A = {
      def go(x:Exp):A = x match {
        case Literal(s) => lit(s)
        case And(a,b) => and(go(a), go(b))
        case Many(e) => many(go(e))
        case Or(a, b) => or(go(a), go(b))
      }
      go(this)
    }

    def interpret(s:String):(Boolean, String) =
      this match  {
        case Literal(l) if(s contains  l) =>
          //println(s" [LITERAL-MATCH] on [$l]")
          (true, s drop l.length)

        case And(exp1, exp2) =>
          //println(s" [AND] $exp1 $exp2 on [$s]")
          val (p, ns) = exp1 interpret s
          if(p) exp2 interpret ns else (false, s)

        case Or(exp1,exp2) =>
          //println(s" [OR] $exp1 $exp2 on [$s]")
          val (p, ns) = exp1 interpret s
          if(p) (true, ns) else exp2 interpret s

        case Many(exp) =>
          //println(s" [MANY] $exp on [$s]")
          val (p, ns) = exp interpret s
          if (!p) (false, s) else (true,s)

        case _ =>
          //println(s"FALSE on [$s]")
          (false, s)
      }
  }
  case class Literal(s:String) extends Exp
  case class And(a: Exp, b:Exp) extends Exp
  case class Many(e:Exp) extends Exp
  case class Or(a:Exp, b:Exp) extends Exp


  def main(args: Array[String]) {
    val e = And(Literal("raining"),
              Or(Literal("dogs"),
                      Literal("cats")))

    println(e.interpret("raining dorgs ddfds ceats"))
  }
}
