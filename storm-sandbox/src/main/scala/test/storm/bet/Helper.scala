package test.storm.bet

import test.storm.Bet

object Helper {
  import org.json4s._
  import org.json4s.native.JsonMethods._
  implicit val formats = DefaultFormats

  def convert(s:String):Bet = parse(s).extract[Bet]
}
