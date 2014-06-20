def isPrime(n:Int):Boolean = {
  //(2 to n-1).map(x=> (n,x)).forall { case(x,y) => x % y != 0 }
  (2 until n).forall { d  => n % d != 0 }
}

(1 until 10) flatMap(i =>
  (1 until i) filter(j => isPrime(i + j)) map( j => (i,j))
)

for {
  i <- 1 until 10
  j <- 1 until i
  if isPrime(i+j)
} yield (i,j)