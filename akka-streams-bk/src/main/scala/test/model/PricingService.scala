package test.model

import scala.collection.SortedSet
import scalaz.concurrent.Task
import scalaz.{-\/, \/, \/-}

object PricingService {

  private val prices:List[Double] = List(2.5, 3.5, 5.5, 6.7)

  val r = scala.util.Random
  def execute1 : List[Selection] => List[Selection] = { selections =>

    val x = for {
      i  <- 1 to gen(10)
      lookupId = gen(20)
      price = r.nextInt(4)
    } yield selections.find(s => s.selectionId == lookupId).map(s => s.copy(price = prices(price))).get
    x.groupBy(_.selectionId).map(_._2.head).toList
  }

  def execute2 : PricingEvent => PricingEvent = { ev =>

    val marketUpdates = for {
      iteration  <- 1 to gen(10)
      marketId <- 1 to gen(10)
      market <- ev.markets.find(m => m.id == marketId)
    } yield market.copy(status = nextStatus, selections = market.selections.map(s => s.copy(price = prices(r.nextInt(4)))))
    ev.copy(markets = marketUpdates.groupBy(_.id).map(_._2.head).to[SortedSet])
  }

  def execute3 : PricingEvent2 => PricingEvent2 = { ev =>

    val marketUpdates = for {
      iteration  <- 1 to gen(10)
      marketId <- 1 to gen(10)
      market: PricingMarket2 <- ev.markets.map(ms => ms.find(m => m.id == marketId)).get
    } yield market.copy(status = Some(nextStatus), selections = Some(market.selections.get.map(s => s.copy(price = Some(prices(r.nextInt(4)))))))
    ev.copy(markets = Some(marketUpdates.groupBy(_.id).map(_._2.head).to[SortedSet]))
  }

  def execute4 : PricingEvent2 => Task[String \/ PricingEvent2] = { ev =>
    Task.delay {
      r.nextInt(2) match {
        case 1 =>
          val marketUpdates = for {
            iteration <- 1 to gen(10)
            marketId <- 1 to gen(10)
            market: PricingMarket2 <- ev.markets.map(ms => ms.find(m => m.id == marketId)).get
          } yield market.copy(status = Some(nextStatus), selections = Some(market.selections.get.map(s => s.copy(status = Some(nextStatus), price = Some(prices(r.nextInt(4)))))))
          \/-(ev.copy(markets = Some(marketUpdates.groupBy(_.id).map(_._2.head).to[SortedSet])))
        case 0 => -\/(s"Error in pricing for event ${ev.id}")
      }
    }
  }

  def execute5 : PricingEvent2 => Task[String \/ Option[PricingEvent2]] = { ev =>
    Task.delay {
      r.nextInt(2) match {
        case 1 =>
          val marketUpdates = for {
            iteration <- 1 to gen(10)
            marketId <- 1 to gen(10)
            market: PricingMarket2 <- ev.markets.map(ms => ms.find(m => m.id == marketId)).get
          } yield market.copy(status = Some(nextStatus), selections = Some(market.selections.get.map(s => s.copy(status = Some(nextStatus), price = Some(prices(r.nextInt(4)))))))
          val u = ev.copy(markets = Some(marketUpdates.groupBy(_.id).map(_._2.head).to[SortedSet]))
          \/-(ev.diff(u))
        case 0 => -\/(s"Error in pricing for event ${ev.id}")
      }
    }
  }

  def nextStatus : StatusEnum = if(r.nextInt(2) == 0) Active else Suspended

  def gen(i:Int) : Int = {
    r.nextInt(i) + 1
  }
}
