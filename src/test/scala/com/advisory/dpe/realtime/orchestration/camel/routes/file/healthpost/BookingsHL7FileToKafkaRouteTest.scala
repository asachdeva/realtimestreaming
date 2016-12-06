package com.advisory.dpe.realtime.orchestration.camel.routes.file.healthpost

import java.util.Properties

import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.apache.camel.Exchange
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.test.TestingServer
import org.apache.log4j.Logger
import org.junit.rules.TemporaryFolder
import org.junit._
import org.scalatest.junit.JUnitSuiteLike

class BookingsHL7FileToKafkaRouteTest extends CamelTestSupport with RouteBuilderSupport with JUnitSuiteLike  {

  val logger = Logger.getLogger(this.getClass.getName)
  var cli: CuratorFramework = null
  var zkTestServer: TestingServer = null
  var kafkaServer : KafkaServerStartable = null
  val _temporaryFolder = new TemporaryFolder()

  // This line sets up a camel context and adds the Bookings Data HL7 File to Kafka routeclear
  override def createRouteBuilder = new BookingsHL7FileToKafkaRoute(context).builder

  @Rule
  def temporaryFolder = _temporaryFolder

  @Before
  def startZookeeperAndKafka() {
    logger.info("Starting embedded zookeeper server...")

    zkTestServer = new TestingServer(2181)
    cli = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString(), new RetryOneTime(2000));
    logger.info(s"Zookeeper state: ${cli.getState}")
    zkTestServer.start()
    cli.start()

    // Kafka Start
    logger.info("Starting embedded kafka broker...")
    val kafkaConfig: KafkaConfig = buildKafkaConfig(zkTestServer.getConnectString)
    kafkaServer = new KafkaServerStartable(kafkaConfig)
    kafkaServer.startup()
  }

  private def buildKafkaConfig(zookeeperConnectionString: String): KafkaConfig = {
    logger.info("Kafka Messages are being logged to directory " + temporaryFolder.getRoot.getAbsolutePath)

    val p: Properties = new Properties
    p.setProperty("zookeeper.connect", zookeeperConnectionString)
    p.setProperty("host.name", "localhost")
    p.setProperty("broker.id", "1")
    p.setProperty("port", "" + 9092)
    p.setProperty("log.dirs", temporaryFolder.getRoot.getAbsolutePath)
    p.setProperty("log.retention.hours", "1")
    new KafkaConfig(p)
  }

  @Ignore
  def testFileToKafka {
    assert(temporaryFolder.newFile() !== null)
    Thread.sleep(1000)
    template.sendBodyAndHeader("file://files/inbox", "content of template", Exchange.FILE_NAME, "template.txt")
  }

  @After
  def stopCamelAndZookeeperAndKafka() {
    logger.info("Stopping Camel Context...")
    createRouteBuilder.getContext.getShutdownStrategy.setSuppressLoggingOnTimeout(true)
    createRouteBuilder.getContext.getShutdownStrategy.stop()

    logger.info("Stopping embedded kafka server...")
    kafkaServer.shutdown()

    logger.info("Stopping embedded zookeeper...")
    cli.close();
    zkTestServer.stop();
  }
}
