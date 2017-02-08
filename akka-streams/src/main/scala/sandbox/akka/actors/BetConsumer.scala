package sandbox.akka.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{Consumer, Envelope, ShutdownSignalException}
import sandbox.akka.actors.BetConsumer.{Message, PersistedBets}
import sandbox.akka.actors.fsm.FsmBetAggregator
import sandbox.akka.domain.Model.{Bet, SelectionBet}
import sandbox.akka.domain.ModelUtil
import sandbox.akka.persistence.Journal

import scalaz.{-\/, \/, \/-}

object BetConsumer {
  case class Message(data:String, deliveryTag:Long)
  case class PersistedBets(deliveryTag:Long, bets:List[SelectionBet])
  def props(journal:Journal) = Props(new BetConsumer(journal))
}

class BetConsumer(journal:Journal) extends Actor with RabbitConsumer {

  val betAggregatorRegion: ActorRef = ClusterSharding(context.system).start(
    typeName = FsmBetAggregator.shardName,
    entityProps = FsmBetAggregator.props(journal),
    settings = ClusterShardingSettings(context.system),
    extractEntityId = FsmBetAggregator.extractEntityId,
    extractShardId = FsmBetAggregator.extractShardId)

  override def preStart(): Unit = {
    consume
  }

  override def consume:Unit = {
    import scala.collection.JavaConversions._
    channel.queueDeclare(queueName, false, false, false, Map.empty[String,AnyRef])
    channel.basicConsume(queueName, false, new Consumer() {
      override def handleCancel(consumerTag: String): Unit = ()
      override def handleRecoverOk(consumerTag: String): Unit = ()
      override def handleCancelOk(consumerTag: String): Unit = ()
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]): Unit = {
        self ! Message(new String(body), envelope.getDeliveryTag)
      }
      override def handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException): Unit = ()
      override def handleConsumeOk(consumerTag: String): Unit = ()
    })
  }

  override def receive: Receive = {

    case m:Message => process(Util.convert(m), m.deliveryTag)

    case PersistedBets(deliveryTag, bets) => channel.basicAck(deliveryTag, false)
                                              bets.foreach(betAggregatorRegion ! _)
  }

  def process : (Option[List[SelectionBet]], Long) => Unit = {
    case (Some(bets), deliveryTag) => journal.persistBets(bets, persistHandler(self ! PersistedBets(deliveryTag, bets), persistFailurePrint(deliveryTag)))
  }

  def persistHandler(persistSuccessAction: => Unit, persistFailureAction: Throwable => Unit): (Throwable \/ Unit) => Unit = {
    case -\/(t) => persistFailureAction(t)
    case \/-(()) => persistSuccessAction
  }

  def persistFailurePrint(deliveryTag:Long) : Throwable => Unit = { t =>
    println(s"persistFailure $t for deliveryTag [$deliveryTag]")
    channel.basicAck(deliveryTag, false)
  }
}

object Util {
  def convert : Message => Option[List[SelectionBet]] = parseMessage andThen toSelectionBets

  private def parseMessage : Message => Bet = { m =>
    import org.json4s._
    import org.json4s.native.JsonMethods._
    implicit val formats = DefaultFormats
    parse(new String(m.data.toArray)).extract[Bet]
  }

  private def toSelectionBets : Bet => Option[List[SelectionBet]] = { b =>
    ModelUtil.splitBet(b)
  }
}