import week4.numbers._

val n1 = Zero.successor
val n2 = n1.successor
println(n2.isZero)

val n3 = n1 - n1

println(n3.isZero)


val n4 = n2.successor - n1.successor

println(n4.isZero)

