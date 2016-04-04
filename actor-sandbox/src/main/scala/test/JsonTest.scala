package test

object JsonTest {

  case class HttpResponse(query:Query)
  case class Query(count:Int, created:String, results: Result)
  case class Result(quote:Quote)
  case class Quote(Ask:String, Bid:String, Name:String, StockExchange:String)

  def main(args: Array[String]) {
    import org.json4s._
    import org.json4s.native.JsonMethods._
    implicit val formats = DefaultFormats

    val json = """{"query":{"count":1,"created":"2016-04-04T14:42:32Z","lang":"en-US","results":{"quote":{"symbol":"AAPL","Ask":"110.80","AverageDailyVolume":"46038500","Bid":"110.79","AskRealtime":null,"BidRealtime":null,"BookValue":"23.13","Change_PercentChange":"+0.83 - +0.75%","Change":"+0.83","Commission":null,"Currency":"USD","ChangeRealtime":null,"AfterHoursChangeRealtime":null,"DividendShare":"2.08","LastTradeDate":"4/4/2016","TradeDate":null,"EarningsShare":"9.40","ErrorIndicationreturnedforsymbolchangedinvalid":null,"EPSEstimateCurrentYear":"9.08","EPSEstimateNextYear":"10.02","EPSEstimateNextQuarter":"1.78","DaysLow":"110.27","DaysHigh":"111.16","YearLow":"92.00","YearHigh":"134.54","HoldingsGainPercent":null,"AnnualizedGain":null,"HoldingsGain":null,"HoldingsGainPercentRealtime":null,"HoldingsGainRealtime":null,"MoreInfo":null,"OrderBookRealtime":null,"MarketCapitalization":"614.45B","MarketCapRealtime":null,"EBITDA":"82.79B","ChangeFromYearLow":"18.82","PercentChangeFromYearLow":"+20.46%","LastTradeRealtimeWithTime":null,"ChangePercentRealtime":null,"ChangeFromYearHigh":"-23.72","PercebtChangeFromYearHigh":"-17.63%","LastTradeWithTime":"9:53am - <b>110.82</b>","LastTradePriceOnly":"110.82","HighLimit":null,"LowLimit":null,"DaysRange":"110.27 - 111.16","DaysRangeRealtime":null,"FiftydayMovingAverage":"101.62","TwoHundreddayMovingAverage":"107.80","ChangeFromTwoHundreddayMovingAverage":"3.02","PercentChangeFromTwoHundreddayMovingAverage":"+2.80%","ChangeFromFiftydayMovingAverage":"9.20","PercentChangeFromFiftydayMovingAverage":"+9.05%","Name":"Apple Inc.","Notes":null,"Open":"110.41","PreviousClose":"109.99","PricePaid":null,"ChangeinPercent":"+0.75%","PriceSales":"2.60","PriceBook":"4.75","ExDividendDate":"2/4/2016","PERatio":"11.79","DividendPayDate":"2/11/2016","PERatioRealtime":null,"PEGRatio":"1.04","PriceEPSEstimateCurrentYear":"12.20","PriceEPSEstimateNextYear":"11.06","Symbol":"AAPL","SharesOwned":null,"ShortRatio":"1.47","LastTradeTime":"9:53am","TickerTrend":null,"OneyrTargetPrice":"134.35","Volume":"4847389","HoldingsValue":null,"HoldingsValueRealtime":null,"YearRange":"92.00 - 134.54","DaysValueChange":null,"DaysValueChangeRealtime":null,"StockExchange":"NMS","DividendYield":"1.91","PercentChange":"+0.75%"}}}}"""

    val res = parse(json).extract[HttpResponse]
    println(res)


    case class Model(message: Message, age: Int, test:List[Test])
    case class Message(value:String)
    case class Test(name:String)
    val rawJson = """{"message": { "value":"world" }, "age": 42, "test": [{"name":"tesT"}] }"""

    println(parse(rawJson).extract[Model])
  }
}
