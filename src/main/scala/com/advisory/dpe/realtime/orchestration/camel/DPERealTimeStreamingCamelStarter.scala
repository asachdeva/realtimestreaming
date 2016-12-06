package com.advisory.dpe.realtime.orchestration.camel

import com.advisory.dpe.realtime.orchestration.camel.routes.jdbc.KAFKA_JDBC_RouteBuilder
import com.advisory.dpe.realtime.orchestration.camel.routes.mllp.MLLP_Hl7_ADT_RouteBuilder
import com.advisory.dpe.realtime.orchestration.camel.routes.rabbitmq.KAFKA_RMQ_RouteBuilder
import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import org.apache.camel.CamelContext
import org.apache.camel.impl.{DefaultCamelContext, SimpleRegistry}
import org.apache.commons.dbcp.BasicDataSource
import org.apache.log4j.Logger

object DPERealTimeStreamingCamelStarter extends App with ConfigurableTrait {

  override def main(args: Array[String]): Unit = {
    execute
  }

  /**
    * Currently for the POC this Camel Application Adds 3 main routes
    *
    * 1. MllpHl7ListenerRoute which listens on a mllp host and port and then streams the bytes to Kafka
    *     and also to a Data Lake (HDFS)
    *
    * 2. HL7TopicAsXMLToRabbitMQRoute which polls a Kafka Topic (transformed hl7 data as xml) and sends a message
    *      to RMQ
    *
    * 3. HL7TopicAsXMLToJdbcRoute which polls a Kafka Topic (transformed hl7 data as json) and inserts a row into
    *     a DB Table hosted on H2
    */
  def execute {

    def configureDataSource: SimpleRegistry = {
      val jdbcURL: String = configFactory.envOrElseConfigAsString("camel.routes.KAFKA_HL7_ADT_JDBC_Route.jdbc_url")

      val ds = new BasicDataSource
      ds.setDriverClassName("org.h2.Driver")
      ds.setUrl(jdbcURL)
      ds.setUsername("sa")

      val reg = new SimpleRegistry
      reg.put("dataSource", ds)
      reg
    }

    val reg: SimpleRegistry = configureDataSource

    val logger = Logger.getLogger(this.getClass.getName)
    val context: CamelContext = new DefaultCamelContext(reg)

    val MllpHl7Route = new MLLP_Hl7_ADT_RouteBuilder(context)
    val HL7TopicAsXMLToRabbitMQRoute = new KAFKA_RMQ_RouteBuilder(context)
    val HL7TopicAsXMLToJdbcRoute = new KAFKA_JDBC_RouteBuilder(context)

    context.addRoutes(MllpHl7Route)
//    context.addRoutes(HL7TopicAsXMLToRabbitMQRoute)
//    context.addRoutes(HL7TopicAsXMLToJdbcRoute)

    logger.info("starting ...")
    context.start
    while (true) {}
    logger.info("stopping...")
    context.stop()
  }
}