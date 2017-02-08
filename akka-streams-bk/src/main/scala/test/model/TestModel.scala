package test.model

object TestModel extends App {


  val e = Event(1, Active)
  val ms = List(Market(1, Active, 1),
                Market(9, Active, 1),
                Market(2, Active, 1),
                Market(3, Active, 1),
                Market(4, Active, 1),
                Market(5, Active, 1),
                Market(6, Active, 1),
                Market(7, Active, 1),
                Market(8, Active, 1))
  val ss = List(Selection(1, Active, 2.5, 1, 1),
                Selection(20, Active, 2.5, 1, 1),
                Selection(2, Active, 2.5, 1, 1),
                Selection(3, Active, 2.5, 1, 2),
                Selection(4, Active, 2.5, 1, 3),
                Selection(5, Active, 2.5, 1, 4),
                Selection(6, Active, 2.5, 1, 4),
                Selection(7, Active, 2.5, 1, 4),
                Selection(8, Active, 2.5, 1, 5),
                Selection(9, Active, 2.5, 1, 5),
                Selection(10, Active, 2.5, 1, 6),
                Selection(11, Active, 2.5, 1, 7),
                Selection(12, Active, 2.5, 1, 7),
                Selection(13, Active, 2.5, 1, 9),
                Selection(14, Active, 2.5, 1, 7),
                Selection(15, Active, 2.5, 1, 8),
                Selection(16, Active, 2.5, 1, 8),
                Selection(17, Active, 2.5, 1, 3),
                Selection(18, Active, 2.5, 1, 3),
                Selection(19, Active, 2.5, 1, 1))


  println(toDmEvent(e, ms, ss))
  println(PricingService.execute1(ss))

  val original = toPricingEvent1(e,ms,ss)
  val original2 = toPricingEvent2(e,ms,ss)
  val updated = PricingService.execute2(toPricingEvent1(e, ms, ss))
  val updated2 = PricingService.execute3(toPricingEvent2(e, ms, ss))
  println(original)
  println(updated)
  println(original.diff(updated))
  println(original2.diff(updated2))

}
