package com.advisory.dpe.realtime.orchestration.kafka.producer

import java.io.{File, IOException}
import java.util.{Properties, UUID}

import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import kafka.message.NoCompressionCodec
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}

object CSVUploaderToKafka extends App with ConfigurableTrait {

  override def main(args: Array[String]): Unit = {
    execute
  }

  def execute() {

    try {
      val brokers : String = configFactory.envOrElseConfigAsString("kafka.brokers.list")
      val props = new Properties()
      val codec = NoCompressionCodec.codec;

      val clientId: String = UUID.randomUUID().toString
      props.put("compression.codec", codec.toString)
      props.put("producer.type", "sync")
      props.put("metadata.broker.list", brokers)
      props.put("message.send.max.retries", "3")
      props.put("request.required.acks", "0")
      props.put("serializer.class", "kafka.serializer.StringEncoder");
      props.put("client.id", clientId.toString)

      val producer = new Producer[String, String](new ProducerConfig(props))

      val source = scala.io.Source.fromFile(validateFilePath("/Users/sachdeva/work/realtimestreaming/src/main/resources/testdata/final.csv"))
      val lines = source.getLines()
      var count = 0

      for (row <- lines) {
        val cols = row.split(",").map(_.trim)

        // SKIP Header Row
        if (count == 0) {
          count = count + 1
        }

        else {
          val data = new KeyedMessage[String, String]("PATIENT_DATA", row)
          producer.send(data)
          count = count + 1
        }
      }

      println(s"Sent $count messages to Kafka successfully")
      producer.close()
    } catch {
      case e: IOException => System.err.println("Caught IOException: " + e.getMessage)
    }
  }

  private def validateFilePath(filePath: String): String = {
    val file = new File(filePath)
    if (!file.exists()) {
      throw new IOException("'" + filePath + "' is not a vaild file path.")
    }
    filePath
  }
}
