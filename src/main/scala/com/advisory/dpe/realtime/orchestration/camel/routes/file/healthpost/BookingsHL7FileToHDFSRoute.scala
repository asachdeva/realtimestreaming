package com.advisory.dpe.realtime.orchestration.camel.routes.file.healthpost

import com.advisory.dpe.realtime.orchestration.camel.routes.file.FileRouteUtils
import org.apache.camel.CamelContext
import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder

class BookingsHL7FileToHDFSRoute(context: CamelContext) extends ScalaRouteBuilder(context) with FileRouteUtils {

  from("file:src/test/resources/testdata?noop=true&readLock=rename")
    .convertBodyTo(classOf[String])
    .choice {
      when(fileEndsWith(_, "hl7")).to("hfs2://xxx.xxx.xxx.xxx:8020/archive/camel")
    }

}
