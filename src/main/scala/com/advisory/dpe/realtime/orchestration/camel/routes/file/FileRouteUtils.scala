package com.advisory.dpe.realtime.orchestration.camel.routes.file

import org.apache.camel.Exchange

trait FileRouteUtils {

  def fileEndsWith(ex: Exchange, fileExt: String): Boolean = {
    ex.getIn.getHeader("CamelFileName") match {
      case x: String => x.endsWith(fileExt)
      case _ => false
    }
  }
}
