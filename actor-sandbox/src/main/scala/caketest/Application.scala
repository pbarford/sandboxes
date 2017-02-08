package caketest

import caketest.module.{MessageFactory, Prefix}

trait Application {

  this: MessageFactory with Prefix =>

  def getActor() = new Actor(prefix, message)
}
