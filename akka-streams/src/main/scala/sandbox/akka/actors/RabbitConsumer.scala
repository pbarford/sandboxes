package sandbox.akka.actors

import com.rabbitmq.client._
import sandbox.akka.config.RabbitConfig

trait RabbitConsumer extends RabbitConfig with ShutdownListener {

  private[actors] var internalConnection: Option[Connection] = None

  lazy val connectionFactory = {
    val cf = new ConnectionFactory()
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf.setRequestedHeartbeat(30)
    cf.setConnectionTimeout(60)
    cf.setVirtualHost("/")
    cf.setHost("192.168.99.100")
    cf.setPort(5672)
    cf
  }

  private def initialiseConnection = {
    val conn:Connection = connectionFactory.newConnection()
    conn.addShutdownListener(this)
    internalConnection = Some(conn)
    conn
  }

  lazy val connection:Connection = {
    internalConnection match {
      case Some(c) => c
      case _ => initialiseConnection
    }
  }

  override def shutdownCompleted(cause: ShutdownSignalException): Unit = {
    initialiseConnection
    consume
  }

  lazy val channel:Channel = connection.createChannel()

  def consume:Unit
}
