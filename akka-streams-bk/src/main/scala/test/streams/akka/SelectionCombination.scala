package test.streams.akka

import test.streams.akka.RabbitMqConsumer.{Selection, SelectionBet}

import scala.collection.SortedSet


object SelectionCombination extends App {

  type SelectionId = Int
  type BetCombination = String

  var selectionCombinations = Vector[BetCombination]()


  def loadSelectionCombinations : SelectionId => Vector[BetCombination] = { s =>
    Vector[BetCombination]("1-2-3", "2-1-4")
  }

  def addSelectionCombination : (SelectionId, BetCombination) => Unit = { (s,bc) =>

  }

  def trixie : SelectionBet => Option[List[SelectionBet]] = { bet =>
    if(bet.selections.size != 3) {
      None
    } else {
      val d: List[SelectionBet] = doubles(bet.selections.toList).map(ds => SelectionBet(bet.betID, bet.totalStake / 15, bet.betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
      val t: List[SelectionBet] = triples(bet.selections.toList).map(ts => SelectionBet(bet.betID, bet.totalStake / 15, bet.betCurrency, "TRIPLE", SortedSet(ts._1, ts._2, ts._3)))
      Some(d ++ t ++ List(SelectionBet(bet.betID, bet.totalStake / 15, bet.betCurrency, "QUAD", bet.selections)))
    }
  }

  def yankee : SelectionBet => Option[List[SelectionBet]] = { bet =>
    if(bet.selections.size != 4) {
      None
    } else {
      val d:List[SelectionBet] = doubles(bet.selections.toList).map(ds => SelectionBet(bet.betID, bet.totalStake / 11, bet.betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
      val t:List[SelectionBet] = triples(bet.selections.toList).map(ts => SelectionBet(bet.betID, bet.totalStake / 11, bet.betCurrency, "TRIPLE", SortedSet(ts._1, ts._2, ts._3)))
      Some(d ++ t ++ List(SelectionBet(bet.betID, bet.totalStake / 11, bet.betCurrency, "QUAD", bet.selections)))
    }
  }

  def lucky15 : SelectionBet => Option[List[SelectionBet]] = { bet =>
    if(bet.selections.size != 4) {
      None
    } else {
      val s: List[SelectionBet] = bet.selections.map(s => SelectionBet(bet.betID, bet.totalStake / 15, bet.betCurrency, "SINGLE", SortedSet(s))).toList
      val d: List[SelectionBet] = doubles(bet.selections.toList).map(ds => SelectionBet(bet.betID, bet.totalStake / 15, "DOUBLE", bet.betCurrency, SortedSet(ds._1, ds._2)))
      val t: List[SelectionBet] = triples(bet.selections.toList).map(ts => SelectionBet(bet.betID, bet.totalStake / 15, "TRIPLE", bet.betCurrency, SortedSet(ts._1, ts._2, ts._3)))
      Some(s ++ d ++ t ++ List(SelectionBet(bet.betID, bet.totalStake / 15, bet.betCurrency, "QUAD", bet.selections)))
    }
  }

  def doubles(x:Seq[Selection]):List[(Selection, Selection)] = {
    def inner(c: List[(Selection, Selection)], xs: Seq[Selection]):List[(Selection, Selection)] = xs match {
      case x :: xs =>
         inner(c ++ xs.map(y => (x, y)), xs)
      case Nil => c
    }
    inner(List.empty, x)
  }

  def triples(x:Seq[Selection]):List[(Selection, Selection, Selection)] = {
    def step(acc: List[(Selection, Selection,Selection)], head:Selection, tail: Seq[Selection]):List[(Selection, Selection, Selection)] = {
      tail match {
        case x::xs => step(acc ++ xs.map(y => (head, x, y)), head, xs)
        case _ => acc
      }
    }

    def inner(acc: List[(Selection, Selection,Selection)], list: Seq[Selection]):List[(Selection, Selection, Selection)] = list match {
      case x :: xs => inner(acc ++ step(List.empty, x, xs), xs)
      case _ => acc
    }

    inner(List.empty, x)
  }

}
