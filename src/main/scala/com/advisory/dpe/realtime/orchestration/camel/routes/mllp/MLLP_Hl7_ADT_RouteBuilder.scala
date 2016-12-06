package com.advisory.dpe.realtime.orchestration.camel.routes.mllp

import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import com.advisory.dpe.realtime.orchestration.kafka.StreamingKafkaUtils
import org.apache.camel.component.kafka.{KafkaEndpoint, KafkaProducer}
import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder
import org.apache.camel.{CamelContext, Exchange, LoggingLevel}


class MLLP_Hl7_ADT_RouteBuilder(context: CamelContext) extends ScalaRouteBuilder(context) with ConfigurableTrait {
  val mllpPort          :Int        = configFactory.envOrElseConfigAsInt   ("camel.routes.MLLP_HL7_ADT_Route.port")
  val mllpHost          :String     = configFactory.envOrElseConfigAsString("camel.routes.MLLP_HL7_ADT_Route.host")
  val routeId           :String     = configFactory.envOrElseConfigAsString("camel.routes.MLLP_HL7_ADT_Route.routeId")
  val kafkaOutputTopic  :String     = configFactory.envOrElseConfigAsString("camel.routes.MLLP_HL7_ADT_Route.kafkaOutputTopic")
  val hdfsURL           :String     = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_HDFS_Route.hdfs_url")

  val kafkaEndpoint: KafkaEndpoint  = StreamingKafkaUtils.configureCamelKafkaEndpoint()
  val bookingsTopicProducer: KafkaProducer = new KafkaProducer(kafkaEndpoint)

  from(s"mllp://$mllpHost:$mllpPort?autoAck=false&reuseAddress=true&connectTimeout=500&receiveTimeout=10000").streamCaching()
    .routeId(s"$routeId")
    .log(LoggingLevel.INFO, routeId, s"HL7 ADT Message Received over MLLP at host: $mllpHost and at port: $mllpPort")
    .convertBodyTo(classOf[String])
    .to("direct:processHL7ADTMessageChannel1", "direct:processHL7ADTMessageChannel2")

  from("direct:processHL7ADTMessageChannel1")
    .to("metrics:timer:simple.timer?action=start")
    .process(bodyToKafkaTopicProcessor(false))
    .to("metrics:timer:simple.timer?action=stop")

  from("direct:processHL7ADTMessageChannel2")
    .to("metrics:timer:simple.timer?action=start")
    .log(LoggingLevel.INFO, routeId, s"Archiving the HL7 Message as a file for archival on HDFS")
    .convertBodyTo(classOf[String], "UTF-8")
    .to("file:///tmp/ADT.hl7")
    .to("metrics:timer:simple.timer?action=stop")

  from("file:///tmp/ADT.hl7?delete=true")
    .to("metrics:timer:simple.timer?action=start")
    .to(s"$hdfsURL")
    .to("metrics:timer:simple.timer?action=stop")

    .onCompletion()
    .log(LoggingLevel.INFO, routeId, "MLLP HL7 ADT Message Route Completed with current payload")


  def bodyToKafkaTopicProcessor(isInvalid: Boolean) = (exchange: Exchange) => {
    println("Getting Ready to stream HL7 ADT messages to kafka topic")
    if (isInvalid) {
      StreamingKafkaUtils.configureKafkaEndpoiontWithTopicName(kafkaEndpoint, kafkaOutputTopic + "_INVALID")
    }
    else {
      StreamingKafkaUtils.configureKafkaEndpoiontWithTopicName(kafkaEndpoint, kafkaOutputTopic)
    }
    bookingsTopicProducer.start()
    bookingsTopicProducer.process(exchange)
  }
}