package test

import scala.collection.SortedSet

package object model {

  sealed trait StatusEnum
  case object Active extends StatusEnum
  case object Suspended extends StatusEnum

  trait Id[A <: Id[A]] extends Ordered[A] {
    def id:Int
    override def compare(that: A): Int =
      if(this.id < that.id) -1
      else if(this.id > that.id) 1
      else 0
  }

  case class Event(eventId:Int, status:StatusEnum)
  case class Market(marketId:Int, status:StatusEnum, eventId:Int)
  case class Selection(selectionId:Int, status:StatusEnum, price:Double, eventId:Int, marketId:Int) {
    override def equals(obj: scala.Any): Boolean = selectionId == obj.asInstanceOf[Selection].selectionId
  }

  def selectionsByMarket(id:Int): List[Selection] => List[Selection] = {
    ss => ss.filter(s => s.marketId == id)
  }

  def toDmEvent : (Event, List[Market], List[Selection]) => DmEvent = { (e, ms, ss) =>

    DmEvent(e.eventId,
            e.status,
            ms.map(m => DmMarket(m.marketId, m.status, selectionsByMarket(m.marketId)(ss).map(s => DmSelection(s.selectionId, s.status, s.price)))))

  }

  def toPricingEvent1 : (Event, List[Market], List[Selection]) => PricingEvent = { (e, ms, ss) =>

    PricingEvent(e.eventId,
      e.status,
      ms.map(m => PricingMarket(m.marketId, m.status, selectionsByMarket(m.marketId)(ss).map(s => PricingSelection(s.selectionId, s.status, s.price)).to[SortedSet])).to[SortedSet])

  }

  def toPricingEvent2 : (Event, List[Market], List[Selection]) => PricingEvent2 = { (e, ms, ss) =>

    PricingEvent2(e.eventId,
      Some(e.status),
      Some(ms.map(m => PricingMarket2(m.marketId, Some(m.status), Some(selectionsByMarket(m.marketId)(ss).map(s => PricingSelection2(s.selectionId, Some(s.status), Some(s.price))).to[SortedSet]))).to[SortedSet]))

  }

  case class PricingEvent(id:Int, status:StatusEnum, markets:SortedSet[PricingMarket]) extends Id[PricingEvent] {
    def diff(update:PricingEvent):Option[PricingEvent] = {
      val updatedMarkets = for {
        om <- markets
        um <- update.markets.find(s => s.id == om.id)
        updates <- om.diff(um)
      } yield updates
      if(updatedMarkets.nonEmpty)
        Some(this.copy(markets = updatedMarkets))
      else None
    }
  }
  case class PricingMarket(id:Int, status:StatusEnum, selections: SortedSet[PricingSelection]) extends Id[PricingMarket] {
    def diff(update:PricingMarket):Option[PricingMarket] = {
      val statusDiff : Option[PricingMarket] => Option[PricingMarket] = { delta =>
        if (status != update.status) Some(this.copy(status = update.status))
        else delta
      }

      val selectionsDiff : Option[PricingMarket] => Option[PricingMarket] = { delta =>
        val updatedSelections = for {
          os <- selections
          us <- update.selections.find(s => s.id == os.id)
          updates <- os.diff(us)
        } yield updates

        if(updatedSelections.nonEmpty)
          Some(delta.getOrElse(this.copy()).copy(selections = updatedSelections))
        else delta
      }

      val delta = statusDiff andThen selectionsDiff
      delta(None)
    }
  }
  case class PricingSelection(id:Int, status:StatusEnum, price:Double) extends Id[PricingSelection] {
    def diff(update:PricingSelection):Option[PricingSelection] = {
      val statusDiff : Option[PricingSelection] => Option[PricingSelection] = { delta =>
        if(status != update.status) Some(this.copy(status = update.status))
        else delta
      }
      val priceDiff : Option[PricingSelection] => Option[PricingSelection] = { delta =>
        if(price != update.price) Some(delta.getOrElse(this.copy()).copy(price = update.price))
        else delta
      }
      val delta = statusDiff andThen priceDiff
      delta(None)
    }
  }

  case class PricingEvent2(id:Int, status:Option[StatusEnum], markets:Option[SortedSet[PricingMarket2]] = None) extends Id[PricingEvent2] {
    def diff(update:PricingEvent2):Option[PricingEvent2] = {
      val updatedMarkets = for {
        om <- markets.getOrElse(List.empty)
        um <- update.markets.map(ms => ms.find(s => s.id == om.id)).getOrElse(None)
        updates <- om.diff(um)
      } yield updates
      if(updatedMarkets.nonEmpty)
        Some(this.copy(status = None, markets = Some(updatedMarkets.to[SortedSet])))
      else None
    }
  }
  case class PricingMarket2(id:Int, status:Option[StatusEnum] = None, selections: Option[SortedSet[PricingSelection2]] = None) extends Id[PricingMarket2] {
    def diff(update:PricingMarket2):Option[PricingMarket2] = {

      val statusDiff : Option[PricingMarket2] => Option[PricingMarket2] = { delta =>
        if (status != update.status)
          Some(this.copy(status = update.status))
        else delta
      }

      val selectionsDiff : Option[PricingMarket2] => Option[PricingMarket2] = { delta =>
        val updatedSelections = for {
          os <- selections.getOrElse(List.empty)
          us <- update.selections.map(ss => ss.find(s => s.id == os.id)).getOrElse(None)
          updates <- os.diff(us)
        } yield updates

        if(updatedSelections.nonEmpty)
          Some(delta.getOrElse(emptyCopy).copy(selections = Some(updatedSelections.to[SortedSet])))
        else delta
      }

      val delta = statusDiff andThen selectionsDiff
      delta(None)
    }

    lazy val emptyCopy = this.copy(status = None, selections = None)
  }
  case class PricingSelection2(id:Int, status:Option[StatusEnum] = None, price:Option[Double] = None) extends Id[PricingSelection2] {
    def diff(update:PricingSelection2):Option[PricingSelection2] = {
      val statusDiff : Option[PricingSelection2] => Option[PricingSelection2] = { delta =>
        if(status != update.status) Some(this.copy(status = update.status))
        else delta
      }
      val priceDiff : Option[PricingSelection2] => Option[PricingSelection2] = { delta =>
        if(price != update.price) Some(delta.getOrElse(emptyCopy).copy(price = update.price))
        else delta
      }
      val delta = statusDiff andThen priceDiff
      delta(None)
    }

    lazy val emptyCopy = this.copy(status = None, price = None)
  }

  case class DmEvent(id:Int, status:StatusEnum, markets:List[DmMarket])
  case class DmMarket(id:Int, status:StatusEnum, selections: List[DmSelection])
  case class DmSelection(id:Int, status:StatusEnum, price:Double)
}
