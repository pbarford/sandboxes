package test.storm

import java.util

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichSpout
import org.apache.storm.tuple.{Fields, Values}

case class Sentence(value:String) {
  val asTuple:Values = new Values(value)
}

class TestSpout extends BaseRichSpout {

  def connect:Connection = {
    val cf = new ConnectionFactory()
    cf.setHost("192.168.99.102")
    cf.setPort(5672)
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf.newConnection()
  }

  var channel:Channel = _

  var spoutOutputCollector:Option[SpoutOutputCollector] = None

  override def close(): Unit = {
    channel.close()
    channel.getConnection.close()
    super.close()
  }

  override def nextTuple(): Unit = {
    val msg = channel.basicGet("test-storm", false)
    if(msg != null) {
      println(s"----> TestSpout ----> msg $msg")

      val x = new String(msg.getBody, "UTF-8")
      println(s"----> TestSpout ----> nextTuple $x")
      val s = Sentence(x)
      spoutOutputCollector.map(c => {
        c.emit(s.asTuple, msg.getEnvelope.getDeliveryTag)
      })
    }
  }

  override def open(conf: util.Map[_, _], context: TopologyContext, collector: SpoutOutputCollector): Unit = {
    this.channel = connect.createChannel()
    this.spoutOutputCollector = Some(collector)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer.declare(new Fields("word"));
  }

  override def ack(msgId: scala.Any): Unit = msgId.isInstanceOf[Long] match {
    case true =>
      channel.basicAck(msgId.asInstanceOf[Long], false)
    case false =>
      super.ack(msgId)

  }

  override def fail(msgId: scala.Any): Unit = msgId.isInstanceOf[Long] match {
    case true => channel.basicNack(msgId.asInstanceOf[Long], false, false)
    case false => super.fail(msgId)

  }
}