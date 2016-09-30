package puzzle

import scalaz.{-\/, \/, \/-}

object FinalScore {

  val validList         = List("Kickoff", "Goalhome", "Goalaway", "Goalhome", "Goalhome", "Final Whistle")
  val noKickoffElement  = List("Goalhome", "Goalaway", "Final Whistle")
  val typos             = List("KICKOFF", "GOALAWAY", "GOALHOME", "FINAL WHISTLE")
  val noFinalWhistle    = List("Kickoff", "Goalhome", "Goalhome")

  sealed trait MatchIncident
  case object KickOff extends MatchIncident
  case object GoalHome extends MatchIncident
  case object GoalAway extends MatchIncident
  case object FinalWhistle extends MatchIncident

  case class Score(home: Int, away: Int)

  def main(args: Array[String]) {

    println(processIncidents(mapL(validList)))
    println(processIncidents(mapL(noKickoffElement)))
    println(processIncidents(mapL(typos)))
    println(processIncidents(mapL(noFinalWhistle)))
  }

  def mapL(e:List[String]):List[MatchIncident] =
    e.flatMap(mapE(_))

  def mapE(s:String):Option[MatchIncident] = s match {
    case "Kickoff" => Some(KickOff)
    case "Goalhome" => Some(GoalHome)
    case "Goalaway" => Some(GoalAway)
    case "Final Whistle" => Some(FinalWhistle)
    case _ => None
  }

  def validate(l:List[MatchIncident]):String \/ List[MatchIncident] = l match {
    case Nil => -\/ ("empty list!!!!")
    case _ =>
      (l.head, l.last) match {
        case (KickOff, FinalWhistle) => \/-(l)
        case _ => -\/("invalid list !!!!")
      }
  }

  def processIncidents(l:List[MatchIncident]):String \/ Score = {
    for {
      x <- validate(l)
    } yield x.foldLeft(Score(0,0))((acc, i) => calcScore(i, acc))
  }

  def calcScore(i:MatchIncident, cs:Score):Score = i match {
    case GoalAway => Score(cs.home, cs.away + 1)
    case GoalHome => Score(cs.home + 1, cs.away)
    case _ => cs
  }
}
