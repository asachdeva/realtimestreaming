package com.advisory.dpe.realtime.orchestration.camel.routes.jdbc

import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder
import org.apache.camel.{CamelContext, LoggingLevel};

class KAFKA_JDBC_RouteBuilder(context: CamelContext) extends ScalaRouteBuilder(context) with ConfigurableTrait {
  val jdbcURL: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_JDBC_Route.jdbc_url")
  val kafkaBrokers: String = configFactory.envOrElseConfigAsString("kafka.brokers.list")
  val kafkaInputTopic: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_JDBC_Route.kafkaInputTopic")
  val routeId: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_JDBC_Route.routeId")

  from(s"kafka:$kafkaBrokers?topic=$kafkaInputTopic&serializerClass=kafka.serializer.StringEncoder&groupId=HealthPost")
    .routeId(s"$routeId")
    .log(LoggingLevel.INFO, routeId, s"New Kafka Message Received in Kafka Topic Named $kafkaInputTopic which is being routed to $jdbcURL")
    .setBody(simple("${body}"))
    .to("direct:sendMessageToJDBC")

  from("direct:sendMessageToJDBC")
    .convertBodyTo(classOf[String], "UTF-8")
    .setBody(simple("INSERT INTO ADT_Payloads(ID, DATA) VALUES('1', '${body}')"))
    .to("jdbc:dataSource?resetAutoCommit=false")

    .onCompletion()
    .log(LoggingLevel.INFO, routeId, "KAFKA HL7 ADT JDBC Route Completed")
}
