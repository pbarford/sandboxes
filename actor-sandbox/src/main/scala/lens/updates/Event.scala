package lens.updates

import monocle._
import monocle.macros.GenLens

sealed trait Id[A] {
  val id:Int

  override def equals(o: Any) = o match {
    case that: Id[A] => that.id == this.id
    case _ => false
  }

  override def hashCode(): Int = id.hashCode()
}

sealed trait Merge[A] extends Id[A] {
  def merge(update:A):A

}

sealed trait CombineUpdates[A <: Merge[A]] {
  def combinator:List[A]
  def combine(updates: List[A]):List[A] = {
    val x = combinator.map { e =>
      updates.find(u => e.id == u.id) match {
        case Some(u) =>
          e.merge(u)
        case _ => e
      }
    }

    x ++ updates.diff(combinator)
  }
}

case class Event(id:Int, name:String, markets:List[Market]) extends CombineUpdates[Market] {
  override def combinator: List[Market] = markets
}

case class Market(id:Int, name:String, selections:List[Selection]) extends Merge[Market] with CombineUpdates[Selection] {

  import MarketLens._
  def lens(i:Int, n:String, s:List[Selection])(m:Market):Market =
    (nameLens.set(n) andThen idLens.set(i) andThen selLens.set(s))(m)


  override def combinator: List[Selection] = selections

  override def merge(update:Market) : Market = {
    lens(update.id, update.name, combine(update.selections))(this)
  }
}
case class Selection(id:Int, name:String, price:Double) extends Merge[Selection] {

  import SelectionLens._

  def lens(n:String, p:Double)(s:Selection):Selection =
    (nameLens.set(n) andThen priceLens.set(p))(s)

  override def merge(update: Selection): Selection = {
    lens(update.name, update.price)(this)
  }
}

case class Test(name:Model.Version[String])

object Model {
  type Version[A] = (A, Int)
}

object MarketLens {
  val nameLens = Lens[Market, String](_.name)(n => o => o.copy(name = n))
  val idLens = Lens[Market, Int](_.id)(n => o => o.copy(id = n))
  val selLens = Lens[Market, List[Selection]](_.selections)(n => o => o.copy(selections = n))
}

object SelectionLens {
  val nameLens = GenLens[Selection](_.name)
  val idLens = GenLens[Selection](_.id)
  val priceLens = GenLens[Selection](_.price)
}

object Event extends App {

  val marketLens = Lens[Event, List[Market]](_.markets)(ms => e => e.copy(markets = ms))

  val e1 = Event(1, "test", List(Market(1,"m1", List(Selection(1, "s1", 2.0),Selection(2, "s2", 3.0))),Market(2,"m2", List.empty)))
  println(e1)

  val updates = List(Market(1, "m1-update", List(Selection(1, "s1-updated", 4.0))), Market(3, "m3", List(Selection(5, "s4", 7.0))))
  println(updates)
  val e2 = marketLens.set(e1.combine(updates))(e1)
  println(e2)


}