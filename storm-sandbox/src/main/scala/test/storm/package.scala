package test

import org.apache.storm.tuple.Values

package object storm {

  case class Bet(betID:String, betType:String, betSource:BetSource, legs:Seq[Leg], totalStake:Double, betCurrency:String) {

    def asTuples:Seq[Values] = {
      legs.map(l => new Values(s"${l.selection.selectionID}-${l.selection.selectionName}" , Double.box(totalStake), Double.box(l.selection.price.decimalPrice)))
    }
  }
  case class BetSource (sourceBetPlatform:String, channel:String)
  case class Leg(legNumber:Int, selection:Selection)

  case class Selection(selectionID:String, selectionName:String, price:Price, platformReference:PlatformReference)

  case class Price(decimalPrice:Double, priceDescription:String)

  case class PlatformReference(platformReferenceSubclassID:String,platformReferenceEventTypeID:String, platformReferenceEventID:String, platformReferenceMarketID:String, platformReferenceSelectionID:String)
}
