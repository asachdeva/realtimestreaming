package com.advisory.dpe.realtime.orchestration

import com.typesafe.config.ConfigFactory
import scala.util.Properties

class DPEConfig(fileNameOption: Option[String] = None) {

  val config = fileNameOption.fold(
    ifEmpty = ConfigFactory.load() )(
    file => ConfigFactory.load(file) )

  def envOrElseConfigAsString(name: String): String = {
    Properties.envOrElse(
      name.toUpperCase.replaceAll("""\.""", "_"),
      config.getString(name)
    )
  }

  def envOrElseConfigAsInt(name: String): Int = {
    config.getInt(name)
  }
}