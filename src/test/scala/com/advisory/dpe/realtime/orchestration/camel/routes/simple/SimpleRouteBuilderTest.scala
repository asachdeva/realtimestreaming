package com.advisory.dpe.realtime.orchestration.camel.routes.simple

import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.apache.camel.test.junit4.CamelTestSupport
import org.junit.Test
import org.scalatest.junit.JUnitSuiteLike

class SimpleRouteBuilderTest extends CamelTestSupport with RouteBuilderSupport with JUnitSuiteLike {

  override def createRouteBuilder = new SimpleRouteBuilder(context)

  @Test
  def testFilterRouteGold() {
    getMockEndpoint("mock:gold").expectedMessageCount(1)
    getMockEndpoint("mock:no_gold").expectedMessageCount(0)

    template.sendBodyAndHeader("direct:start", "Hello World", "gold", "true")

    assertMockEndpointsSatisfied()
  }

}
