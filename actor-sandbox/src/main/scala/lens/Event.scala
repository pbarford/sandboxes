
package lens
import monocle._
import monocle.macros.GenLens
import monocle.function.Each._
import monocle.std.list._

import scala.language.higherKinds

sealed trait Id[A] {
  val id:Int
  override def equals(o: Any) = o match {
    case that: Market => that.id == this.id
    case _ => false
  }

  override def hashCode(): Int = id.hashCode()
}

sealed trait Merge[A] {
  def merge(update:A):A

}

sealed trait Merge2[A] {
  def merge(update: Id[A]): Id[A]

  def mergeUpdates(existing: List[Id[A]], updates: List[Id[A]]) = {
    val x = existing.map { e =>
      updates.find(u => e.id == u.id) match {
        case Some(u) =>
          this.merge(u)
        case _ => e
      }
    }
    x ++ updates.diff(existing)
  }
}

case class Event(id:Int, name:String, markets:List[Market])  {

  def mergeMarketUpdates(updates: List[Market]): List[Market] = {

    val x = markets.map { m:Market =>
      updates.find(u => m.id==u.id) match {
        case Some(u) =>
          m.merge(u)
        case _ => m
      }
    }
    x ++ updates.diff(markets)
  }
}

case class Market(id:Int, name:String, selections:List[Selection]) extends Id[Market] with Merge[Market] {

  import MarketLens._
  def lens(i:Int, n:String, s:List[Selection])(m:Market):Market =
    (nameLens.set(n) andThen idLens.set(i) andThen selLens.set(s))(m)

  override def merge(update:Market) : Market = {
    lens(update.id, update.name, update.selections)(this)
  }

  def mergeSelectionUpdates(updates: List[Selection]) = {
    val x = selections.map { s =>
      updates.find(u => s.id==u.id) match {
        case Some(u) =>
          s.merge(u)
        case _ => s
      }
    }
    x ++ updates.diff(selections)
  }
}
case class Selection(id:Int, name:String, price:Double) extends Id[Selection] with Merge[Selection] {
  import SelectionLens._

  def lens(i:Int, n:String, p:Double)(s:Selection):Selection =
    (nameLens.set(n) andThen idLens.set(i) andThen priceLens.set(p))(s)

  override def merge(update: Selection): Selection = {
    lens(update.id, update.name, update.price)(this)
  }
}

case class State2(event:Option[Event2])
case class Event2(id:Int, name:Option[String], markets:Option[List[Market2]])
case class Market2(id:Int, name:Option[String])

object MarketLens {
  val nameLens = Lens[Market, String](_.name)(n => o => o.copy(name = n))
  val idLens = Lens[Market, Int](_.id)(n => o => o.copy(id = n))
  val selLens = Lens[Market, List[Selection]](_.selections)(n => o => o.copy(selections = n))
}

object SelectionLens {
  val nameLens = Lens[Selection, String](_.name)(n => o => o.copy(name = n))
  val idLens = Lens[Selection, Int](_.id)(n => o => o.copy(id = n))
  val priceLens = Lens[Selection, Double](_.price)(n => o => o.copy(price = n))
}

object Event extends App {

  val eventLens = GenLens[Event](_.name)
  val marketLens = Lens[Event, List[Market]](_.markets)(ms => e => e.copy(markets = ms))
  val marketName = Lens[Market, String](_.name)(n => m => m.copy(name = n))
  val selectionLens = Lens[Market, List[Selection]](_.selections)(ss => m => m.copy(selections = ss))
  val selectionPrice = GenLens[Selection](_.price)


  val evLens = Optional[State2, Event2](_.event)(e => s => s.copy(event =  Some(e)))
  val mkLens = Optional[Event2, List[Market2]](_.markets)(m => e => e.copy(markets = Some(m)))
  val mkNameLens = Optional[Market2, String](_.name)(n => m => m.copy(name = Some(n)))

  val l = evLens composeOptional mkLens
  val m = l composeTraversal each composeOptional mkNameLens

  val ee = State2(Some(Event2(1, Some("tst"), Some(List(Market2(1, Some("tyr")), Market2(2, Some("xxx")))))))

  val x = marketLens composeTraversal each composeLens marketName


  println(ee)
  val ee2 = m.set("hello")(ee)
  println(ee2)

  val e1 = Event(1, "test", List(Market(1,"m1", List(Selection(1, "s1", 2.0))),Market(2,"m2", List.empty)))
  println(e1)

  println(x.set("new name")(e1))

  val updates = List(Market(1, "m1-update", List(Selection(1, "s1-updated", 4.0))), Market(3, "m3", List.empty))
  val e2 = marketLens.set(e1.mergeMarketUpdates(updates))(e1)
  println(e2)


}