package test.streams.scalaz

import test.model._

import scalaz.\/
import scalaz.concurrent.Task
import scalaz.stream._
import scalaz.stream.Process

object TestStreamz extends App {

  val e = Event(1, Active)
  val ms = List(Market(1, Active, 1),
    Market(9, Active, 1),
    Market(2, Active, 1),
    Market(3, Active, 1),
    Market(4, Active, 1),
    Market(5, Active, 1),
    Market(6, Active, 1),
    Market(7, Active, 1),
    Market(8, Active, 1))
  val ss = List(Selection(1, Active, 2.5, 1, 1),
    Selection(20, Active, 2.5, 1, 1),
    Selection(2, Active, 2.5, 1, 1),
    Selection(3, Active, 2.5, 1, 2),
    Selection(4, Active, 2.5, 1, 3),
    Selection(5, Active, 2.5, 1, 4),
    Selection(6, Active, 2.5, 1, 4),
    Selection(7, Active, 2.5, 1, 4),
    Selection(8, Active, 2.5, 1, 5),
    Selection(9, Active, 2.5, 1, 5),
    Selection(10, Active, 2.5, 1, 6),
    Selection(11, Active, 2.5, 1, 7),
    Selection(12, Active, 2.5, 1, 7),
    Selection(13, Active, 2.5, 1, 9),
    Selection(14, Active, 2.5, 1, 7),
    Selection(15, Active, 2.5, 1, 8),
    Selection(16, Active, 2.5, 1, 8),
    Selection(17, Active, 2.5, 1, 3),
    Selection(18, Active, 2.5, 1, 3),
    Selection(19, Active, 2.5, 1, 1))

  def translate1: (Event,List[Market],List[Selection]) => Task [PricingEvent2] = {
    (e,m,s) => Task.delay(toPricingEvent2(e,m,s))
  }

  def translate2: (Event,List[Market],List[Selection]) => Process[Task,PricingEvent2] = {
    (e,m,s) => Process.emit(toPricingEvent2(e,m,s))
  }

  def pricing1: PricingEvent2 => Task[String \/ PricingEvent2] = { e =>
    PricingService.execute4(e)
  }

  def pricing2: PricingEvent2 => Task[String \/ Option[PricingEvent2]] = { e =>
    PricingService.execute5(e)
  }

  def pricing3: PricingEvent2 => Writer[Task, String, PricingEvent2] = { e =>
    Process.eval(PricingService.execute4(e))
  }

  def delta: (PricingEvent2, PricingEvent2) => Option[PricingEvent2] = { (o,u) =>
    o.diff(u)
  }

  def deltaP(o:PricingEvent2): Process1[PricingEvent2, Option[PricingEvent2]] = {
    process1.lift(u => o.diff(u))
  }

  def process1a: (Event,List[Market],List[Selection]) => Process[Task,  String \/ PricingEvent2] = { (e,m,s) =>
    Process.eval {
      for {
        e <- translate1(e,m,s)
        res <- pricing1(e)
        update <- Task.now(res)
      } yield update
    }
  }

  def process2: (Event,List[Market],List[Selection]) => Writer[Task, String,Option[PricingEvent2]] = { (e,m,s) =>
    Process.eval {
      for {
        e <- translate1(e,m,s)
        res <- pricing2(e)
      } yield res
    }
  }

  def process3: (Event,List[Market],List[Selection]) => Process[Task, Option[PricingEvent2]] = { (e,m,s) =>
      for {
        e <- translate2(e,m,s)
        res <- pricing3(e).observeW(errorOut("PROCESS-ERROR")).stripW
        updates <- Process.emit(delta(e, res))
      } yield updates
  }

  def process4: (Event,List[Market],List[Selection]) => Process[Task, Option[PricingEvent2]] = { (e,m,s) =>
    for {
      e <- translate2(e,m,s)
      res <- pricing3(e).observeW(errorOut("PROCESS-ERROR")).stripW.pipe(deltaP(e))
    } yield res
  }

  def consoleOut1: String => Sink[Task, PricingEvent2] = { p =>
    Process.eval(Task.delay { e:PricingEvent2 => Task.delay { println(s"${Thread.currentThread().getName} [$p] : $e") }  })
  }

  def consoleOut2: String => Sink[Task, Option[PricingEvent2]] = { p =>
    Process.eval(Task.delay { e:Option[PricingEvent2] => Task.delay { println(s"${Thread.currentThread().getName} [$p] : $e") }  })
  }

  def consoleOut3: String => Sink[Task, Option[PricingEvent2]] = { p =>
    sink.lift(a => Task.delay { println(s"${Thread.currentThread().getName} [$p] : $a") })
  }

  def errorOut: String => Sink[Task, String] = { p =>
    Process.eval(Task.delay { e:String => Task.delay { println(s"${Thread.currentThread().getName} [$p] : $e") }  })
  }

  def app1:Process[Task, Unit] = {
    merge.mergeN(3) {
      Process.range(1, 10).map {
        i => process1a(e.copy(eventId = i), ms, ss).observeW(errorOut("APP1")).stripW.to(consoleOut1("APP1"))
      }
    }
  }

  def app2:Process[Task, Unit] = {
    merge.mergeN(3) {
      Process.range(1, 10).map {
        i => for {
          r <- process1a(e.copy(eventId = i), ms, ss).observeW(errorOut("APP2")).observeO(consoleOut1("APP2"))
        } yield ()
      }
    }
  }

  def app3:Process[Task, Unit] = {
    merge.mergeN(3) {
      Process.range(1, 10).map {
        i => process2(e.copy(eventId = i), ms, ss).observeW(errorOut("APP3")).stripW.to(consoleOut2("APP3"))
      }
    }
  }

  def app4:Process[Task, Unit] = {
    merge.mergeN(3) {
      Process.range(1, 10).map {
        i => process3(e.copy(eventId = i), ms, ss).to(consoleOut3("APP4"))
      }
    }
  }

  def app5:Process[Task, Unit] = {
    merge.mergeN(3) {
      Process.range(1, 10).map {
        i => process4(e.copy(eventId = i), ms, ss).to(consoleOut3("APP5"))
      }
    }
  }

  app1.run.attemptRun
  app2.run.attemptRun
  app3.run.attemptRun
  app4.run.attemptRun
  app5.run.attemptRun
}
