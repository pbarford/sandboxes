package com.paddypower.akka.domain

import scala.collection.SortedSet
import scala.collection.immutable.Seq

object Model {

  case class Bet(betID:String, betType:String, betSource:BetSource, legs:Seq[Leg], totalStake:Double, betCurrency:String)
  case class BetSource (sourceBetPlatform:String, channel:String)
  case class Leg(legNumber:Int, selection:Selection)
  case class Selection(selectionID:String, selectionName:String, price:Price, platformReference:PlatformReference) extends Ordered[Selection] {
    override def compare(that: Selection): Int = {
      if(this.selectionID.toInt < that.selectionID.toInt)
        -1
      else if(this.selectionID.toInt > that.selectionID.toInt)
        1
      else
        0
    }

    override def toString: String = selectionID
  }

  case class Price(decimalPrice:Double, priceDescription:String)
  case class PlatformReference(platformReferenceSubclassID:String,platformReferenceEventTypeID:String, platformReferenceEventID:String, platformReferenceMarketID:String, platformReferenceSelectionID:String)

  case class SelectionBet(betID:String, totalStake:Double, betCurrency:String, betType:String, selections: SortedSet[Selection]) {
    val key:String = selections.foldLeft("multi")((a, s) => s"$a:${s.selectionID}")
    val multi:Boolean = selections.size > 1
    val betPayout =  selections.foldLeft(totalStake)((a,s) => a * ( s.price.decimalPrice))
    val betLiability =  betPayout - totalStake

    def splitBet: Option[List[SelectionBet]] = {
      betType match {
        case "SINGLE" => Some(List(this))
        case "DOUBLE" => double
        case "STRAIGHT TRICAST" => triple
        case "TRIXIE" => trixie
        case "YANKEE" => yankee
        case "LUCKY 15" => lucky15
        case _ => Some(List(this))
      }
    }

    def yankee : Option[List[SelectionBet]] = {
      if(selections.size != 4) {
        None
      } else {
        val d:List[SelectionBet] = doubles.map(ds => SelectionBet(betID, totalStake / 11, betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
        val t:List[SelectionBet] = triples.map(ts => SelectionBet(betID, totalStake / 11, betCurrency, "TRIPLE", SortedSet(ts._1, ts._2, ts._3)))
        Some(d ++ t ++ List(SelectionBet(betID, totalStake / 11, betCurrency, "QUAD", selections)))
      }
    }

    def lucky15 : Option[List[SelectionBet]] = {
      if(selections.size != 4) {
        None
      } else {
        val s: List[SelectionBet] = selections.map(s => SelectionBet(betID, totalStake / 15, betCurrency, "SINGLE", SortedSet(s))).toList
        val d: List[SelectionBet] = doubles.map(ds => SelectionBet(betID, totalStake / 15, betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
        val t: List[SelectionBet] = triples.map(ts => SelectionBet(betID, totalStake / 15, betCurrency, "TRIPLE", SortedSet(ts._1, ts._2, ts._3)))
        Some(s ++ d ++ t ++ List(SelectionBet(betID, totalStake / 15, betCurrency, "QUAD", selections)))
      }
    }

    def trixie : Option[List[SelectionBet]] = {
      if(selections.size != 3) {
        None
      } else {
        val d: List[SelectionBet] = doubles.map(ds => SelectionBet(betID, totalStake / 15, betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
        Some(d ++ List(SelectionBet(betID, totalStake / 15, betCurrency, "TRIPLE", selections)))
      }
    }

    def triple : Option[List[SelectionBet]] = {
      if(selections.size != 3) {
        None
      } else {
        val t: List[SelectionBet] = triples.map(ts => SelectionBet(betID, totalStake / 15, betCurrency, "TRIPLE", SortedSet(ts._1, ts._2, ts._3)))
        Some(t)
      }
    }

    def double : Option[List[SelectionBet]] = {
      if(selections.size != 2) {
        None
      } else {
        val d:List[SelectionBet] = doubles.map(ds => SelectionBet(betID, totalStake / 11, betCurrency, "DOUBLE", SortedSet(ds._1, ds._2)))
        Some(d)
      }
    }

    private def doubles:List[(Selection, Selection)] = {
      def inner(c: List[(Selection, Selection)], xs: Seq[Selection]):List[(Selection, Selection)] = xs match {
        case x :: xs =>
          inner(c ++ xs.map(y => (x, y)), xs)
        case Nil => c
      }
      inner(List.empty, selections.toList)
    }

    private def triples:List[(Selection, Selection, Selection)] = {
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
      inner(List.empty, selections.toList)
    }

    override def toString: String = s"betID [${betID}] key [${key}], stake [$totalStake] multi [${multi}] betPayout [${betPayout}] betLiability [${betLiability}] betType[$betType]"
  }
}