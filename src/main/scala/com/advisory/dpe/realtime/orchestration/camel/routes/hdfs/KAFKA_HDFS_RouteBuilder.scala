package com.advisory.dpe.realtime.orchestration.camel.routes.hdfs

import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder
import org.apache.camel.{CamelContext, LoggingLevel};

class KAFKA_HDFS_RouteBuilder(context: CamelContext) extends ScalaRouteBuilder(context) with ConfigurableTrait {
  val hdfsURL: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_HDFS_Route.hdfs_url")
  val kafkaBrokers: String = configFactory.envOrElseConfigAsString("kafka.brokers.list")
  val kafkaInputTopic: String = configFactory.envOrElseConfigAsString("camel.kafka.topic.set")
  val routeId: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_HDFS_Route.routeId")

  from(s"kafka:$kafkaBrokers?topic=$kafkaInputTopic&serializerClass=kafka.serializer.StringEncoder&groupId=HealthPost")
    .routeId(s"$routeId")
    .log(LoggingLevel.INFO, routeId, s"New Kafka Message Received in Kafka Topic Named $kafkaInputTopic")
    .setBody(simple("${body}"))
    .to("direct:sendMessageToHDFS")
    .onCompletion()
    .log(LoggingLevel.INFO, routeId, "KAFKA HL7 ADT HDFS Route Completed")

  from("direct:sendMessageToHDFS")
    .setBody(simple("${body}"))
    .to(s"$hdfsURL")
}
