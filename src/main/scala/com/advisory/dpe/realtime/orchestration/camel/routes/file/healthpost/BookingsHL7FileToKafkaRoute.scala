package com.advisory.dpe.realtime.orchestration.camel.routes.file.healthpost

import com.advisory.dpe.realtime.orchestration.kafka.StreamingKafkaUtils
import org.apache.camel.component.kafka.{KafkaEndpoint, KafkaProducer}
import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder
import org.apache.camel.{CamelContext, Exchange}

class BookingsHL7FileToKafkaRoute(context: CamelContext) extends ScalaRouteBuilder(context) {
  val kafkaEndpoint: KafkaEndpoint = StreamingKafkaUtils.configureCamelKafkaEndpoint()
  val bookingsTopicProducer: KafkaProducer = new KafkaProducer(kafkaEndpoint)

  val hl7BodyToKafkaTopicProcessor = (exchange: Exchange) => {
    println("Getting Ready to stream hl7 message to kafka topic")
    bookingsTopicProducer.start()
    bookingsTopicProducer.process(exchange)
  }

  from("direct:processHL7Message").process(hl7BodyToKafkaTopicProcessor)

}
