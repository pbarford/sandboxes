package sandbox.akka.config

trait RabbitConfig {
  def queueName:String = "akka-streams"
}
