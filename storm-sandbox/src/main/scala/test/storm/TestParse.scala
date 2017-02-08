package test.storm

object TestParse extends App {

  import org.json4s._
  import org.json4s.native.JsonMethods._
  implicit val formats = DefaultFormats

  val s = "{\"betSource\":{\"sourceBetPlatform\":\"PP_OPENBET\",\"channel\":\"ONLINE\",\"locationID\":\"77.120.161.44\",\"betPromotion\":null},\"legs\":[{\"legNumber\":1,\"selection\":{\"subclassID\":23,\"subclassName\":\"Horse Racing\",\"eventTypeID\":16214,\"eventTypeName\":\"LEICESTER\",\"eventID\":4416290,\"eventName\":\"16:50 HANDICAP (0-85) 1m 3f 183yds\",\"marketID\":74861984,\"marketName\":\"Win or Each Way\",\"selectionID\":440413812,\"selectionName\":\"Walsingham Grange\",\"price\":{\"priceType\":\"LIVE_PRICE\",\"decimalPrice\":17.0,\"priceDescription\":\"16/1\",\"handicap\":null,\"variants\":null},\"index\":null,\"platformReference\":{\"platformReferenceSubclassID\":\"23\",\"platformReferenceEventTypeID\":\"176\",\"platformReferenceEventID\":\"11266705\",\"platformReferenceMarketID\":\"97380549\",\"platformReferenceSelectionID\":\"500819580\"},\"eventDate\":1475596200000,\"eventTrader\":\"n/a\",\"marketTag\":null,\"selectionCategory\":\"RACING\"},\"legType\":\"WIN_ONLY\",\"eachWayDetails\":null}],\"customer\":{\"customerID\":\"30663370\",\"customerName\":\"silversky10\",\"stakeFactor\":0.01,\"percentageOfMaxBet\":92.31,\"commentary\":null,\"linkedCustomerID\":null,\"customerFirstName\":\"Oleksandr\",\"customerSurname\":\"Usan\",\"customerRegCode\":\"N\",\"customerRegEmail\":\"usan@list.ru\",\"customerRegPostcode\":\"N\",\"customerIsElite\":false,\"customerCategories\":\"PEP Checked\",\"customerCountryCode\":\"UA\"},\"betID\":\"1228212730\",\"betTimestamp\":1475564704000,\"betCurrency\":\"EUR\",\"totalStake\":0.6,\"platformReferenceCurrency\":\"EUR\",\"platformConvertedStake\":null,\"betType\":\"SINGLE\",\"sourceBetType\":\"SGL\",\"betAction\":null,\"stakePerLine\":null,\"totalLegs\":1,\"betBreakdown\":\"1\",\"totalSubbets\":1,\"liabilityAmount\":\"10.20\",\"equallyDivided\":null}"

  println(parse(s).extract[Bet])
}
