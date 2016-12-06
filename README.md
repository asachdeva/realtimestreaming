#  Real Time Streaming Data Pipeline

## What is Real Time Streaming Data Pipeline?
Real Time Streaming Data Pipeline is a distributed stream processing framework.  It uses [Apache Kafka](http://kafka.apache.org) for messaging, 
and [Spark Streaming](http://spark.apache.org/streaming/) to provide distributed processing, transformations and then write results back to a 
Kafka Topic.  It uses [Apache Camel](https://camel.apache.org/) for teh mediation and orchetsration and routing for kakfa connect patterns.

## Real Time Streaming Data key features include:
* DPERealTimeStreamingCamelContext launches a camel context and app and right now only adds three main routes INGESTION PHASE
    * MLLPListenerRoute (opens an MLLP Listener on a specific machine and port) routes the bytes to a Kafka Topic
    * This runs in an endless loop till interrupted
* DemoStreamRoute based on Apache Camel (2.17.1) INGESTION PHASE
    * This runs on a timer (till interrupted) and streams an HL7 Bookings data (SIU segment) into a KafkaTopic named HL7BookingsData
* StreamingSparkConsumerForHl7BookingsData based on Apache Spark (1.6.1)  TRANSFORMATION  AND DELIVERY PHASE
    * This polls the HL7BookingsData topic every 2 seconds and transforms the HL7 testdata into XML and JSON representations
    * This also streams out the transformed XML and JSON data ito HL7AuthBookingsXML and HL7AuthBookingsJson Kafka topics respectively
 
## Quickstart Guide for setup binaries on local machine

1. Download latest Apache Kafka [distribution](http://kafka.apache.org/downloads.html) and un-tar it. 
2. Download latest Apache Zookeeper [distribution](http://zookeeper.apache.org/releases.html#download) and un-tar it
3. Download latest Apache Spark [distribution](http://spark.apache.org/downloads.html) and un-tar it

OR if you are on Mac OSX and homebrew

1. sudo brew install kafka (this will install zookeeper and kafka)
2. sudo brew install apahe-spark (this will install Apache Spark)

**NOTE**:  After you Install Kafka on the Server you need to enable auto.create.topics.enable=true so you do not have to 
manually create Kafka topics.  You can edit the kafka server.properties located on /usr/local/etc/kafka and add 
auto.create.topics.enable=true

##  Core Versions 
1. Apache Kafka 0.9.0.1 on kafka server
2. Apache Kafka 0.8.2.2 on kafka client (producer and consumer(spark streaming) code)
3. Apache Spark 1.6.1 on Spark Streaming
4. Apache Zookeeper 3.4.8 on server
5. Scala Version 2.10.6

## Config
The ConfigFactory is built around typesafe config libraries
 
1. reference.conf Base Config for Kafka, Hadoop, Spark, and Camel
2. sandbox.conf Config for cluster in etech space

### Building Real Time Streaming

To build Real Time Streaming from a git checkout, run:

    ./gradlew clean build

### Testing Real Time Streaming

To run all tests:

    ./gradlew clean test

### Running App on local machine 
        
Start ZooKeeper server (/usr/local/Cellar/zookeeper/3.4.8):

    ./bin/zkserver start

Start Kafka server (/usr/local/Cellar/kafka/0.9.0.1/libexec):

    ./bin/kafka-server-start.sh /usr/local/etc/kafka/server.properties

Start Spark server (/usr/local/Cellar/apache-spark/1.6.1/libexec) :

    ./sbin/start-all.sh

Start Camel Context for MLLP Generating HL7 messages (currently set to read template.hl7 files from src/main/resources/testdata):

    ./gradlew activateCamelContext

Start Spark consumer for transforming hl7 messages into XML and JSON format in a Kafka Topic:

    ./gradlew transformHL7IntoXMLAndJsonViaSparkStreaming
    
## Bambooo Build Wallboard
* [Bamboo Wallboard](https://bamboo.advisory.com/telemetry.action?filter=showPlanAndBranches&planKey=CDP-SDDEV)
    