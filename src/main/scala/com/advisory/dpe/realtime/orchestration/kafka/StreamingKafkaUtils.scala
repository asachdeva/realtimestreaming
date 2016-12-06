package com.advisory.dpe.realtime.orchestration.kafka

import java.util

import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import com.typesafe.scalalogging.slf4j._
import org.apache.camel.component.kafka.{KafkaComponent, KafkaEndpoint}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object StreamingKafkaUtils extends App with Logging  with ConfigurableTrait {

  /**
    * This method takes a topic name, a list of brokers and the message data to be published
    * and then sends the same message
    *
    * @param topic
    * @param brokers
    * @param data
    */
  def sinkTransformedDataToKafka(topic: String, brokers: String, data: String): Unit = {
    val props = new util.HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    val message = new ProducerRecord[String, String](topic, null, data)
    producer.send(message)
  }

  /**
    * This method allows for handy configuration of the Kafka endpoint from a Camel Context
    *
    * @return
    */
  def configureCamelKafkaEndpoint(): KafkaEndpoint = {
    val kakfaEndpointUri = configFactory.envOrElseConfigAsString("camel.kafka.endpoint.uri")
    val kafkaEndpoint: KafkaEndpoint = new KafkaEndpoint(kakfaEndpointUri, new KafkaComponent())

    kafkaEndpoint.setBrokers(configFactory.envOrElseConfigAsString("kafka.brokers.list"))
    kafkaEndpoint.setTopic(configFactory.envOrElseConfigAsString("camel.kafka.topic.set"))
    kafkaEndpoint.setClientId(configFactory.envOrElseConfigAsString("kafka.client.id"))

    kafkaEndpoint
  }

  def configureKafkaEndpoiontWithTopicName(kafkaEndpoint: KafkaEndpoint, topicName: String): Unit = {
    kafkaEndpoint.setTopic(topicName)
  }

  def kafkaConfForSpark_ : Map[String, String] = {
    Map("metadata.broker.list" -> configFactory.envOrElseConfigAsString("kafka.brokers.list"),
      "zookeeper.connect" -> configFactory.envOrElseConfigAsString("zookeeper.connect"),
      "client.id" -> configFactory.envOrElseConfigAsString("kafka.client.id"),
      "auto.offset.reset" -> configFactory.envOrElseConfigAsString("spark.kafka.auto.offset.reset"),
      "zookeeper.connection.timeout.ms" -> configFactory.envOrElseConfigAsString("zookeeper.connection.timeout.ms"))
  }
}
