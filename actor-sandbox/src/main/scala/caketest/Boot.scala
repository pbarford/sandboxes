package caketest

import caketest.module.{MessageFactory1, Prefix1}

class Boot extends Application with MessageFactory1 with Prefix1 {
  val a = getActor()
  a.saySomething()
}
