package com.advisory.dpe.realtime.processing.spark

import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import com.advisory.dpe.realtime.orchestration.kafka.StreamingKafkaUtils
import com.advisory.dpe.realtime.transform.HL7TransformUtilities
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingSparkConsumerForHL7Data extends App with ConfigurableTrait {

  override def main(args: Array[String]): Unit = {
    execute
  }

  def execute() {
    val PUBLISH_HL7_XML_TOPIC: String   = configFactory.envOrElseConfigAsString("spark.kafka.outputtopics.XMLXFormTopic")
    val PUBLISH_HL7_JSON_TOPIC: String  = configFactory.envOrElseConfigAsString("spark.kafka.outputtopics.JSONXFormTopic")
    val brokers                         = configFactory.envOrElseConfigAsString("kafka.brokers.list")
    val inputKafkaTopics                = configFactory.envOrElseConfigAsString("spark.kafka.topic.set")

    System.setProperty("hadoop.home.dir", configFactory.envOrElseConfigAsString("hadoop.home.dir"))

    val sparkConf: SparkConf = new SparkConf()

    if (configFactory.envOrElseConfigAsString("environment.name").equals("sandbox")) {
      sparkConf
        .setAppName("StreamingSparkConsumerForHL7Data")
        .set("spark.driver.allowMultipleContexts", "true")
    } else {
      sparkConf.setMaster("local[*]")
        .setAppName("StreamingSparkConsumerForHL7Data")
        .set("spark.driver.allowMultipleContexts", "true")
        .set("spark.deploy.defaultCores", "4")
        .set("spark.executor.memory", "2g")
        .setJars(Array("/Users/sachdeva/realtimestreaming-all-1.0.jar"))
    }
    val ssc = new StreamingContext(sparkConf, Seconds(configFactory.envOrElseConfigAsInt("spark.polling.interval")))
    val topicsSet = inputKafkaTopics.split(",").toSet

    val kafkaConf = StreamingKafkaUtils.kafkaConfForSpark_

    val directKafkaInputDStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaConf, topicsSet)

    directKafkaInputDStream.foreachRDD { rdd => {

      if (rdd.count() > 0) {
        rdd.foreach { x =>

          val xmlBookingsData: String = HL7TransformUtilities.transformHL7ToXMLv23(x._2)
          println(s"HL7 Transformed into XML as $xmlBookingsData")
          StreamingKafkaUtils.sinkTransformedDataToKafka(PUBLISH_HL7_XML_TOPIC, brokers, xmlBookingsData)


          val jsonBookingsData: String = HL7TransformUtilities.transformHL7ToJSON(xmlBookingsData)
          println(s"HL7 Transformed into JSON as $jsonBookingsData")
          StreamingKafkaUtils.sinkTransformedDataToKafka(PUBLISH_HL7_JSON_TOPIC, brokers, jsonBookingsData)
          }
        }
      }
    }

    directKafkaInputDStream.print()
    ssc.start()

    println("Spark Streaming started")
    ssc.awaitTermination()
    println("Streaming finished")
  }
}
