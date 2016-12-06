package com.advisory.dpe.realtime.orchestration.camel.routes.stream

import kafka.server.KafkaServerStartable
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.test.TestingServer
import org.apache.log4j.Logger
import org.junit._
import org.junit.rules.TemporaryFolder
import org.scalatest.junit.JUnitSuiteLike

class UberCamelRouteTest extends CamelTestSupport with RouteBuilderSupport with JUnitSuiteLike  {

  val logger = Logger.getLogger(this.getClass.getName)
  var cli: CuratorFramework = null
  var zkTestServer: TestingServer = null
  var kafkaServer : KafkaServerStartable = null
  val _temporaryFolder = new TemporaryFolder()

  override def createRouteBuilder = new UberCamelRoute(context, 50).builder

  @Ignore
  def testGenerateHL7Message {
    context.start()
    while(true) {
      Thread.sleep(500)
    }
    context.stop()
  }

}
