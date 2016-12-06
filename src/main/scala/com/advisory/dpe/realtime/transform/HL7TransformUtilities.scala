package com.advisory.dpe.realtime.transform

import ca.uhn.hl7v2.parser.{DefaultXMLParser, PipeParser}
import ca.uhn.hl7v2.validation.ValidationContext
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory
import ca.uhn.hl7v2.{DefaultHapiContext, HapiContext}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.xml._

object HL7TransformUtilities extends App {

  /**
    * This method currently supports taking an HL7 Message
    * with data structure of 2.3
    *
    * Implementation delegates to HAPI Libraries to
    * Load, Validate and Transform to XML
    *
    * This has been tested for Bookings Data (SIU) only
    *
    * @param x
    */
  def transformHL7ToXMLv23(x: String): String = {
    try {
      val context: HapiContext = new DefaultHapiContext()
      val validationContext: ValidationContext = ValidationContextFactory.defaultValidation()
      context.setValidationContext(validationContext)

      val parser: PipeParser = context.getPipeParser

      val message = parser.parse(x)
      val xmlParser = new DefaultXMLParser()
      val messageInXML: String = xmlParser.encode(message)
      return messageInXML
    } catch {
      case e: Exception => {
        e.toString
      }
    }
  }

  implicit object NodeFormat extends JsonFormat[Node] {
    def write(node: Node) =
      if (node.child.count(_.isInstanceOf[Text]) == 1)
        JsString(node.text)
      else
        JsObject(node.child.collect {
          case e: Elem => e.label -> write(e)
        }: _*)

    def read(jsValue: JsValue) = null // not implemented
  }

  /**
    *
    * @param x
    * @return
    */
  def transformHL7ToJSON(x: String): String = {
    try {
      val messageInJson = pimpAny(XML.loadString(x).toSeq).toJson.prettyPrint
      return messageInJson
    } catch {
      case e: Exception => {
        e.toString
      }
    }
  }
}
