package caketest

import caketest.module.{MessageFactory2, Prefix2}

class TestBoot extends Application with MessageFactory2 with Prefix2 {
  val a = getActor()
  a.saySomething()
}
