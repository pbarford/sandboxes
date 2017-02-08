package test.streams.akka

import test.streams.akka.RabbitMqConsumer.{PlatformReference, Price, Selection, SelectionBet}

import scala.collection.SortedSet

object TestStuff extends App {

  val bet1 = SelectionBet("1228212730", 2.6, "EUR", "", SortedSet(Selection("2", "t2", Price(4.0, "dsd"), PlatformReference("", "", "", "", "")),Selection("1", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", "")), Selection("3", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", "")),Selection("4", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", "")),Selection("5", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", ""))))
  val bet2 = SelectionBet("1228212730", 7.5, "EUR", "", SortedSet(Selection("2", "t2", Price(4.0, "dsd"), PlatformReference("", "", "", "", "")),Selection("1", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", "")), Selection("3", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", "")),Selection("4", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", ""))))
  val bet3 = SelectionBet("1228212730", 1.1, "EUR", "", SortedSet(Selection("2", "t2", Price(4.0, "dsd"), PlatformReference("", "", "", "", "")),Selection("1", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", "")), Selection("3", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", "")),Selection("4", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", ""))))
  val bet4 = SelectionBet("1228212750", 1.1, "EUR", "", SortedSet(Selection("2", "t2", Price(4.0, "dsd"), PlatformReference("", "", "", "", "")),Selection("1", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", "")), Selection("3", "t1", Price(2.5, "dsd"), PlatformReference("", "", "", "", ""))))

  println(s"$bet1.key} --> ${bet1.betPayout} : ${bet1.betLiability}")
  println(bet1.double)
  println(bet1.triple)

  val bets1: Option[List[SelectionBet]] = bet2.lucky15
  println(bets1.get.size)
  bets1.get.map(b => println(b))

  val bets2 = bet3.yankee
  println(bets2.get.size)
  bets2.get.map(b => println(b))

  val bets3 = bet4.trixie
  println(bets3.get.size)
  bets3.get.map(b => println(b))

  val selections: List[String] = bets3.get.flatMap(_.selections.map(_.selectionID)).distinct
  val combinations: List[SortedSet[Selection]] = bets3.get.map(_.selections.keySet)
  val keyCombinations: List[String] = bets3.get.map(_.key)

  val x = for {
    sel <- selections
    c: SortedSet[Selection] <- combinations.filter(_.contains(Selection(sel, "", Price(0, ""), PlatformReference("", "", "", "", ""))))
  } yield sel -> c
  println(x.groupBy(t => t._1).map(t => t._1 -> t._2.map(_._2)))


  val y = for {
    sel <- selections
    keys <- keyCombinations.filter(s => s.matches(s".*$sel.*"))
  } yield sel -> keys

  val inv = y.groupBy(t => t._1).map(t => t._1 -> t._2.map(_._2))
  //println(inv)

  val inverted = selections.map(s => Map(s -> keyCombinations.filter(kc => kc.matches(s".*$s.*")))).reduce(_ ++ _)
  //println(inverted)

  println(invertedIndexOfMultiples(bets1.get))
  println(invertedIndexOfMultiples(bets2.get))
  println(invertedIndexOfMultiples(bets3.get))

  println(invertedIndexOfSelectionToBet(bet4))

  def invertedIndexOfMultiples(bets: List[SelectionBet]):Map[String, List[String]] = {
    val kcs: List[String] = bets.filter(b => b.selections.size > 1).map(_.key)
    val r = for {
      sel <- bets.flatMap(_.selections.map(_.selectionID)).distinct
    } yield Map(sel -> kcs.filter(kc => kc.matches(s".*$sel.*")))
    r.reduce(_ ++ _)
  }

  def invertedIndexOfSelectionToBet(bet:SelectionBet):Map[String, List[String]] = {
    val r = for {
      sel <- bet.selections.map(_.selectionID)
    } yield Map(sel -> List(bet.betID))
    r.reduce(_ ++ _)
  }

}
