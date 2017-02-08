package caketest.module

trait Prefix{

  val prefix : String
}

trait Prefix1 extends Prefix {
  override val prefix: String = "HELLO"
}

trait Prefix2 extends Prefix {
  override val prefix: String = "HI"
}