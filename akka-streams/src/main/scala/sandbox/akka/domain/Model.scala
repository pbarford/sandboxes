package sandbox.akka.domain

import Model._

import scala.collection.SortedSet
import scala.collection.immutable.Seq

object Model {

  case class Bet(betID:String, betTimestamp:Long, betType:String, betSource:BetSource, legs:Seq[Leg], totalStake:Double, stakePerLine:Option[Double], betCurrency:String, liabilityAmount:Option[String])
  case class BetSource (sourceBetPlatform:String, channel:String)
  case class Leg(legNumber:Int, selection:Selection)
  case class Selection(selectionID:Option[String], selectionName:String, price:Price, platformReference:Option[PlatformReference]) extends Ordered[Selection] {
    override def compare(that: Selection): Int = {
      if(this.getSelectionId.toInt < that.getSelectionId.toInt)
        -1
      else if(this.getSelectionId.toInt > that.getSelectionId.toInt)
        1
      else
        0
    }

    def getSelectionId:String = selectionID match {
      case Some(id) => id
      case None => platformReference match {
        case Some(pr) => pr.platformReferenceSelectionID.getOrElse("ERROR")
        case _ => "ERROR"
      }
    }

  }

  case class Price(priceType:String, decimalPrice:Option[Double], priceDescription:String)
  case class PlatformReference(platformReferenceSubclassID:Option[String],platformReferenceEventTypeID:Option[String], platformReferenceEventID:Option[String], platformReferenceMarketID:Option[String], platformReferenceSelectionID:Option[String])

  case class SelectionBet(betID:String, betTimeStamp:Long, betSource:String, totalStake:Double, stakePerLine:Option[Double], betCurrency:String, betType:String, subType:String, selections: SortedSet[Selection]) {
    val key:String = selections.foldLeft(subType)((a, s) => s"$a:${s.getSelectionId}")
    val multi:Boolean = selections.size > 1
    val stake:Double = stakePerLine match {
      case Some(value) => value
      case _ => totalStake
    }

    lazy val betPayout:Double  = { selections.foldLeft(stake) ((a, s) => a * s.price.decimalPrice.getOrElse(0.0)) }
    lazy val betLiability:Double =  betPayout - stake

    override def toString: String = s"betID [${betID}] betTimeStamp [${betTimeStamp}] key [${key}], stake [$stake] multi [${multi}] betPayout [${betPayout}] betLiability [${betLiability}] betType [$betType] subType [$subType]"
  }

}

object ModelUtil {

  def splitBet: Bet => Option[List[SelectionBet]] = { bet =>
    bet.betType match {
      case "SINGLE" => Some(bet.legs.map(l => generateSelectionBet(bet, "SINGLE", SortedSet(l.selection))).toList)
      case "DOUBLE" => double(bet)
      case "STRAIGHT TRICAST" => triple(bet)
      case "TREBLE" => triple(bet)
      case "TRIXIE" => trixie(bet)
      case "YANKEE" => yankee(bet)
      case "LUCKY 15" => lucky15(bet)
      case _ => None
    }
  }

  private def yankee : Bet => Option[List[SelectionBet]] = { bet =>
    if(bet.legs.size != 4) {
      None
    } else {
      val d:List[SelectionBet] = doubles(bet).map(ds => generateSelectionBet(bet, "DOUBLE", SortedSet(ds._1.selection, ds._2.selection)))
      val t:List[SelectionBet] = triples(bet).map(ts => generateSelectionBet(bet, "TRIPLE", SortedSet(ts._1.selection, ts._2.selection, ts._3.selection)))
      Some(d ++ t ++ List(generateSelectionBet(bet, "FOUR_FOLD", bet.legs.map(l => l.selection).to[SortedSet])))
    }
  }

  private def lucky15 : Bet => Option[List[SelectionBet]] = { bet =>
    if(bet.legs.size != 4) {
      None
    } else {
      val s: List[SelectionBet] = bet.legs.map(l => generateSelectionBet(bet, "SINGLE", SortedSet(l.selection))).toList
      val d: List[SelectionBet] = doubles(bet).map(ds => generateSelectionBet(bet, "DOUBLE", SortedSet(ds._1.selection, ds._2.selection)))
      val t: List[SelectionBet] = triples(bet).map(ts => generateSelectionBet(bet, "TRIPLE", SortedSet(ts._1.selection, ts._2.selection, ts._3.selection)))
      Some(s ++ d ++ t ++ List(generateSelectionBet(bet, "FOUR_FOLD", bet.legs.map(l => l.selection).to[SortedSet])))
    }
  }

  private def trixie : Bet => Option[List[SelectionBet]] = { bet =>
    if(bet.legs.size != 3) {
      None
    } else {
      val d: List[SelectionBet] = doubles(bet).map(ds => generateSelectionBet(bet, "DOUBLE", SortedSet(ds._1.selection, ds._2.selection)))
      val t: List[SelectionBet] = triples(bet).map(ts => generateSelectionBet(bet, "TRIPLE", SortedSet(ts._1.selection, ts._2.selection, ts._3.selection)))
      Some(d ++ t)
    }
  }

  private def triple : Bet => Option[List[SelectionBet]] = { bet =>
    if(bet.legs.size != 3) {
      None
    } else {
      val t: List[SelectionBet] = triples(bet).map(ts => generateSelectionBet(bet, "TRIPLE", SortedSet(ts._1.selection, ts._2.selection, ts._3.selection)))
      Some(t)
    }
  }

  private def double : Bet => Option[List[SelectionBet]] = { bet =>
    if(bet.legs.size != 2) {
      None
    } else {
      val d:List[SelectionBet] = doubles(bet).map(ds => generateSelectionBet(bet, "DOUBLE", SortedSet(ds._1.selection, ds._2.selection)))
      Some(d)
    }
  }

  private def generateSelectionBet(bet:Bet, subType:String, selections:SortedSet[Selection]):SelectionBet = {
    SelectionBet(bet.betID, bet.betTimestamp, bet.betSource.sourceBetPlatform, bet.totalStake, bet.stakePerLine, bet.betCurrency, bet.betType, subType, selections)
  }

  private def doubles: Bet => List[(Leg, Leg)] = { bet =>
    def inner(c: List[(Leg, Leg)], xs: Seq[Leg]):List[(Leg, Leg)] = xs match {
      case x :: xs =>
        inner(c ++ xs.map(y => (x, y)), xs)
      case Nil => c
    }
    inner(List.empty, bet.legs.toList)
  }

  private def triples:Bet => List[(Leg, Leg, Leg)] = { bet =>
    def step(acc: List[(Leg, Leg,Leg)], head:Leg, tail: Seq[Leg]):List[(Leg, Leg, Leg)] = {
      tail match {
        case x::xs => step(acc ++ xs.map(y => (head, x, y)), head, xs)
        case _ => acc
      }
    }
    def inner(acc: List[(Leg, Leg,Leg)], list: Seq[Leg]):List[(Leg, Leg, Leg)] = list match {
      case x :: xs => inner(acc ++ step(List.empty, x, xs), xs)
      case _ => acc
    }
    inner(List.empty, bet.legs.toList)
  }

  def formatD: Double => Double = d => BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
}
