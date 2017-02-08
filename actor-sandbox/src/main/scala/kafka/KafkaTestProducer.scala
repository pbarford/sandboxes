package kafka

import java.util.HashMap

/*
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object KafkaTestProducer extends App {

  val props = new HashMap[String, Object]()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.99.103:9092")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)
  println("1")
  val message = new ProducerRecord[String, String]("test2", "1", "hello paul hello")
  println("2")

  producer.send(message)
  println("3")

  producer.close()

}
*/
