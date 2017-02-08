package caketest

import caketest.module.MessageFactory._

class Actor (prefix:String, message: Message) {

  def saySomething():Unit = println(message(prefix))
}
