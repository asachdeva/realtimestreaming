package com.advisory.dpe.realtime.orchestration.camel.routes.simple

import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder
import org.apache.camel.{CamelContext, Exchange}

class SimpleRouteBuilder(context: CamelContext) extends ScalaRouteBuilder (context) {

  val myProcessorMethod = (exchange: Exchange) => {
    exchange.getIn.setBody("block test")
  }

  // a route using Scala blocks
  "direct:start" ==> {
    process(myProcessorMethod)
    when(_.in("gold") == "true") {
      --> ("mock:gold")
    } otherwise {
      to ("mock:no_gold")
    }
  }

}