package test
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import com.mfglabs.precepte._
import corescalaz._
import default._

object Observable {

  val env = BaseEnv(Host("localhost"), Environment.Dev, Version("1.0-DEMO"))
  val noState = ST(Span.gen, env, Vector.empty, ())


  def await[A](f: Future[A]) = Await.result(f, 10 seconds)

  type Pre[A] = DPre[Future, Unit, A]
    object Demo extends Category("demo") {
      def apply[A](f: ST[Unit] => Future[A])(implicit c: Callee) =
        Pre(BaseTags(c, Demo))(f)
  }
  val logback = Logback(env, "demo-logger")
  object WithLogger extends Category("demo") {
    def apply[A](f: logback.Logger => Future[A])(implicit c: Callee) = {
      val withLogger = (st: ST[Unit]) => f(logback.Logger(st.managed.span, st.managed.path))
      Pre(BaseTags(c, Demo))(withLogger)
    }
  }


  import Macros.callee

  def f1a: Pre[Int] =
    WithLogger { logger =>
      logger.info("Computing a value")
      Future.successful(42)
    }

  def f2a(s: Int): Pre[String] =
    WithLogger { logger =>
      logger.info("Performing string concatenation!", Macros.param(s))
      Future.successful(s"The answer to life the universe and everything is $s")
    }


  def f1: Future[Int] = Future.successful(42)
  def f2(s: Int): Future[String] = Future.successful(s"The answer to life the universe and everything is $s")

  def p0:Pre[Int] = Demo(s => Future.successful(25))
  def p1(x:Int):Pre[Int] = Demo(s => Future.successful(x + 3))
  def p2(x:Int):Pre[Int] = Demo(s => Future.successful(x + 3))
  def p3(x:Int):Pre[Int] = Demo(s => Future.successful(x + 3))

  def p4(t:(Int, Int, Int)):Pre[Int] = Demo(s => Future.successful(t._1 + t._2 - t._3))

  def main(args: Array[String]) {
    import scalaz.syntax.applicative._
    val ptest =
      for {
        a <- p0
        b <- (p1(a) |@| p2(a) |@| p3(a)).tupled
        c <- p4(b)
      } yield c

    val ultimateAnswer: Future[String] =
      for {
        s <- f1
        i <- f2(s)
      } yield i

    val ultimateAnswerPre: Pre[String] =
      for {
        s <- f1a
        i <- f2a(s)
      } yield i

    //println(await(ultimateAnswer))

    implicit val unitSG = new scalaz.Semigroup[Unit] {
      def append(f1: Unit, f2: => Unit) = ()
    }

    import scalaz.std.scalaFuture._
    //println(await(ultimateAnswerPre.eval(noState)))

    val ultimateAnswerWithExecutionGraph = ultimateAnswerPre.graph(Graph.empty).eval(noState)
    val (graph, result) = await(ultimateAnswerWithExecutionGraph)

    println(graph.viz)
    println(result)

    val (demoGraph, res) = await(ptest.graph(Graph.empty).eval(noState))
    println(demoGraph.viz)
    println(res)
  }
}
