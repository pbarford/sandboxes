package com.pjb.sandbox

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object WriteToKafka extends App {

    val props = new Properties()
    props.put("bootstrap.servers", "192.168.99.100:9092")
    props.put("batch.size", "16384")
    props.put("buffer.memory", "33554432")
    props.put("retries", "0")
    props.put("client-id", "testing")
    props.put("message.send.max.retries", "3")
    props.put("request.required.acks", "-1")
    props.put("ack", "all")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer2 = new KafkaProducer[String, String](props)
    val producer = new KafkaProducer[String,String](props)
    println("sending")
    producer.send(new ProducerRecord[String,String]("input", s"hello::${System.currentTimeMillis()}", s"hello::${System.currentTimeMillis()}"))
    println("sent")
    producer.close()
}
