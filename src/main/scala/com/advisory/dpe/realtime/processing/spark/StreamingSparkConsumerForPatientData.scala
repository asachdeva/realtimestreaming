package com.advisory.dpe.realtime.processing.spark

import java.util.Properties

import com.advisory.dpe.realtime.orchestration.camel.traits.ConfigurableTrait
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingSparkConsumerForPatientData extends App with ConfigurableTrait {

  override def main(args: Array[String]): Unit = {
    execute
  }

  def execute() {
    val sparkConf: SparkConf = new SparkConf()

    if (configFactory.envOrElseConfigAsString("environment.name").equals("sandbox")) {
      sparkConf
        .setAppName("StreamingSparkConsumerForPatientData")
        .set("spark.driver.allowMultipleContexts", "true")
    } else {
      sparkConf.setMaster("spark://SachdevA-01-MBR:7077")
        .setAppName("StreamingSparkConsumerForPatientData")
        .set("spark.driver.allowMultipleContexts", "true")
        .set("spark.deploy.defaultCores", "4")
        .set("spark.executor.memory", "8g")
          .set("spark.driver.extraClassPath", "/Users/sachdeva/.m2/repository/org/postgresql/postgresql/9.3-1102-jdbc41/postgresql-9.3-1102-jdbc41.jar")
            .set("spark.executor.extraClassPath", "/Users/sachdeva/.m2/repository/org/postgresql/postgresql/9.3-1102-jdbc41/postgresql-9.3-1102-jdbc41.jar")
        .setJars(Array("/Users/sachdeva/realtimestreaming-all-1.0.jar"))
    }

    val ssc = new StreamingContext(sparkConf, Seconds(configFactory.envOrElseConfigAsInt("spark.polling.interval")))
    val sqlContext = new SQLContext(ssc.sparkContext)

    // Loading customers data
    val df = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "true") // Use first line of all files as header
      .option("inferSchema", "false") // Automatically infer data types
      .option("quote", "\u0000") // To avoid error: java.io.IOException: (line 1) invalid char between encapsulated token and delimiter
      .option("delimiter", "|")
      .load("/Users/sachdeva/Downloads/final.csv")

    df.registerTempTable("PatientData")
    val jdbc_url = "jdbc:postgresql://localhost/Demo"

    val connectionProperties:Properties = new Properties()
    connectionProperties.put("user", "testdev")
    connectionProperties.put("password", "testdev")

//    org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils.saveTable(df, jdbc_url, "temp_csvMap1", connectionProperties)
//    df.write.mode(SaveMode.Append).jdbc(jdbc_url,"csvmap1",connectionProperties)

    selectSimple(sqlContext, df)
  }

  def selectSimple(sqlContext: SQLContext, df: DataFrame): Unit = {
    var DF:DataFrame = sqlContext.sql("SELECT * FROM PatientData")
    println("Number of Rows inserted into spark SQL" + DF.count())
    println(DF.show(20))
  }
}
