package caketest.module

import caketest.module.MessageFactory.Message

object MessageFactory {
  type Message = (String => String)
}

trait MessageFactory {
  def message : Message
}

trait MessageFactory1 extends MessageFactory {
  override def message: Message = s => s"$s :: TEST1"
}

trait MessageFactory2 extends MessageFactory {
  override def message: Message = s => s"$s :: TEST2"
}
