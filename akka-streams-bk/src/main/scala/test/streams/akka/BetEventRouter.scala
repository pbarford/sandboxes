package test.streams.akka

import akka.actor.Actor
import test.streams.akka.RabbitMqConsumer.SelectionBet


class BetEventRouter extends Actor {
  override def receive: Receive = {
    case SelectionBet => ???
  }
}
