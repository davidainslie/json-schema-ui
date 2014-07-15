package data.json

import play.api.libs.json.{JsValue, Json}
import org.specs2.mutable.Specification
import org.specs2.matcher.JsonMatchers

class JSONSpec extends Specification with JsonMatchers {
  "JSON Matchers" should {
    "match name -> value pair" in {
      val json = """{"name":"Batman"}"""
      json must / ("name" -> "Batman")
    }

    "match name -> value pair with numbers as doubles" in {
      val json = """{"id":1}"""
      json must / ("id" -> 1.0)
    }
  }

  "Play JSON" should {
    "extract top level key" in {
      val json = Json.obj(
        "topLevel" -> Json.obj(
          "key" -> "value"))

      // Check the JSON
      (json \ "topLevel" \ "key").as[String] mustEqual "value"

      // And now the assertion we are interested in
      json.keys.head mustEqual "topLevel"
    }

    "be successfully parsed against JSON schema" in {
      val schema = Json.parse(getClass.getResource("/json/schema/example-1-schema.json"))
      val json = Json.parse(getClass.getResource("/json/example-1.json"))

      schema.parse(json) must beSuccessfulTry[JsValue].like {
        case j: JsValue => (j \ "applicantDetails" \ "personalDetails" \ "fullName").as[String] mustEqual "Batman"
      }
    }

    "be invalidated against JSON schema" in {
      val schema = Json.parse(getClass.getResource("/json/schema/example-1-schema.json"))
      val json = Json.obj("rubbish" -> "json")

      schema.parse(json) must beFailedTry
    }
  }
}