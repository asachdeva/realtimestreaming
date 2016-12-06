package com.advisory.dpe.realtime.orchestration.camel.routes.rabbitmq

import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import com.advisory.dpe.realtime.orchestration.kafka.StreamingKafkaUtils
import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder
import org.apache.camel.{CamelContext, Exchange, LoggingLevel};

class KAFKA_RMQ_RouteBuilder(context: CamelContext) extends ScalaRouteBuilder(context) with ConfigurableTrait {
  val rabbitURL: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_RMQ_Route.rabbit_url")
  val kafkaBrokers: String = configFactory.envOrElseConfigAsString("kafka.brokers.list")
  val kafkaInputTopic: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_RMQ_Route.kafkaInputTopic")
  val routeId: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_RMQ_Route.routeId")

  from(s"kafka:$kafkaBrokers?topic=$kafkaInputTopic&serializerClass=kafka.serializer.StringEncoder&groupId=HealthPost")
    .routeId(s"$routeId")
    .to("metrics:timer:simple.timer?action=start")
    .log(LoggingLevel.INFO, routeId, s"New Kafka Message Received in Kafka Topic Named $kafkaInputTopic which is being routed to $rabbitURL" )
      .setBody(simple("${body}"))
      .to("direct:sendMessageToRMQ")
    .to("metrics:timer:simple.timer?action=stop")
      .onCompletion()
        .log(LoggingLevel.INFO, routeId, "KAFKA HL7 ADT RabbitMQ Route Completed")

  from("direct:sendMessageToRMQ")
    .to("metrics:timer:simple.timer?action=start")
    .setBody(simple("${body}"))
    .to(s"$rabbitURL")
    .to("metrics:timer:simple.timer?action=stop")

}
