package com.advisory.dpe.realtime.orchestration.camel.routes.stream

import com.advisory.dpe.realtime.orchestration.kafka.StreamingKafkaUtils
import org.apache.camel.component.kafka.{KafkaEndpoint, KafkaProducer}
import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder
import org.apache.camel.{CamelContext, Exchange}

class UberCamelRoute(val camelContext: CamelContext, val generationInterval: Long) extends ScalaRouteBuilder(camelContext) {
  val kafkaEndpoint: KafkaEndpoint = StreamingKafkaUtils.configureCamelKafkaEndpoint()
  val bookingsTopicProducer: KafkaProducer = new KafkaProducer(kafkaEndpoint)

  val hl7BodyToKafkaTopicProcessor = (exchange: Exchange) => {
    println("Getting Ready to stream HL7 message to kafka topic")
    bookingsTopicProducer.start()
    bookingsTopicProducer.process(exchange)
  }

  from(s"timer:foo?fixedRate=true&period=${generationInterval}")
    .process(function = exchange => {
      exchange.in = HL7MessageGenerator.generateMLLPMessage()
    })
    .convertBodyTo(classOf[String])
      .to("direct:processHL7Message")

  from("direct:processHL7Message")
    .process(hl7BodyToKafkaTopicProcessor)

//  from("direct:processHL7Message")
//    .to("hfs2://xxx.xxx.xxx.xxx:8020/archive/camel")
}
