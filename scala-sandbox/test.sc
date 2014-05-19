def isOdd(x: Int) = x % 2 == 1
List(1,2,3,4).filter(isOdd)
1 :: 2 :: 3 :: Nil
"Elwood eats mice".takeWhile(c => c != ' ')

def sumSq(in :List[Double]) : (Int, Double, Double) =
  in.foldLeft((0, 0d, 0d))((t,v) => (t._1 + 1, t._2 + v, t._3 + v * v))

sumSq(List(2.0, 3.0, 5.0))

def sumSq2(in: List[Double]) : (Int, Double, Double) =
  in.foldLeft((0, 0d, 0d)){
    case ((cnt, sum, sq), v) => (cnt + 1, sum + v, sq + v * v)}

sumSq2(List(2.0, 3.0, 5.0))

