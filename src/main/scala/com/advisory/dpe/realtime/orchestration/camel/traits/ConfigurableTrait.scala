package com.advisory.dpe.realtime.orchestration.camel.traits

import com.advisory.dpe.realtime.orchestration.DPEConfig

trait ConfigurableTrait {
//  def environment : String = System.getProperty("environment")
  def environment: String = "sandbox"
  var configFactory = new DPEConfig()

  if (environment != null )
  {
    println(s"Configuring the Streaming Application(s) to be for Environment $environment" )
    configFactory = new DPEConfig(Some(environment))
  }
}
