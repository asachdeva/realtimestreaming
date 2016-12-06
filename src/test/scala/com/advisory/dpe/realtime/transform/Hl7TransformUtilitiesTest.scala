package com.advisory.dpe.realtime.transform

import org.junit.Test

class Hl7TransformUtilitiesTest {

  @Test
  def testHl7Transforms: Unit = {
    val file:String = "src/test/resources/testdata/flatfile_2_Records_1_7.hl7"
    val source = scala.io.Source.fromFile(file)
    val lines = try source.mkString finally source.close()

    val messageInXML = HL7TransformUtilities.transformHL7ToXMLv23(lines)
    assert(messageInXML.mkString.substring(0,5).equals("<?xml"))

    val messageInJson = HL7TransformUtilities.transformHL7ToJSON(messageInXML)
    assert(messageInJson.mkString.substring(0,2).equals("[{"))
  }
}
