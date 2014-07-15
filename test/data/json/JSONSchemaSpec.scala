package data.json

import scala.collection.JavaConversions._
import play.api.libs.json.{JsValue, Json}
import org.specs2.mutable.Specification
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import com.github.fge.jackson.JsonLoader

class JSONSchemaSpec extends Specification {
  "JSON schema version 4" should {
    "validate given JSON" in {
      val jsonSchema = JsonSchemaFactory.byDefault().getJsonSchema(JsonLoader.fromResource("/json/schema/test-schema-v4.json"))
      val processingReport = jsonSchema.validate(JsonLoader.fromResource("/json/schema/test.json"))

      processingReport.isSuccess must beTrue
    }
  }

  "JSON schema version 3" should {
    "validate given JSON" in {
      val jsonValidator = JsonSchemaFactory.byDefault().getValidator
      val processingReport = jsonValidator.validate(JsonLoader.fromResource("/json/schema/test-schema-v3.json"), JsonLoader.fromResource("/json/schema/test.json"))

      processingReport.isSuccess must beTrue
    }
  }

  "JSON schema for an example" should {
    "be valid according to JSON schema version 4" in {
      val jsonSchema = JsonLoader.fromResource("/json/schema/example-1-schema.json")

      val syntaxValidator = JsonSchemaFactory.byDefault().getSyntaxValidator

      syntaxValidator.validateSchema(jsonSchema).iterator().toList.foreach(println)
      syntaxValidator.schemaIsValid(jsonSchema) must beTrue
    }

    "be invalid" in {
      val jsonSchema = JsonLoader fromString {
        """
        {
          "id": "http://www.kissthinker.com/json-schema-ui/example-1",
          "$schema": "http://json-schema.org/draft-04/schema",
          "description": "Blah",
          "type": "object",
          "properties": {
            "surname": {
              "type": "INVALID TYPE"
            }
          },
          "required": ["personalDetails"]
        }
        """
      }

      val syntaxValidator = JsonSchemaFactory.byDefault().getSyntaxValidator

      syntaxValidator.validateSchema(jsonSchema).iterator().toList.foreach(println)
      syntaxValidator.validateSchema(jsonSchema).isSuccess must beFalse
    }

    "be valid according to NEW JSON schema version 4" in {
      val jsonSchema = JsonLoader.fromResource("/json/schema/example-1-schema.json")

      val syntaxValidator = JsonSchemaFactory.byDefault().getSyntaxValidator

      syntaxValidator.validateSchema(jsonSchema).iterator().toList.foreach(println)
      syntaxValidator.schemaIsValid(jsonSchema) must beTrue
    }
  }

  "Example JSON schema" should {
    "be valid" in {
      val schema = Json.parse(getClass.getResource("/json/schema/example-1-schema.json"))
      schema.jsonSchema must beAnInstanceOf[JsonSchema]
    }

    "be invalid" in {
      val schema = Json.parse("""
        {
          "id": "http://www.kissthinker.com/json-schema-ui/example-1",
          "$schema": "http://json-schema.org/draft-04/schema",
          "type": "object",
          "properties": {
            "surname": {
              "type": "INVALID TYPE"
            }
          }
        }
        """)

      schema.jsonSchema must throwA[SchemaException].like {
        case SchemaException(errorMessages) => errorMessages exists { _.contains("INVALID TYPE") } must beTrue
      }
    }

    "parse given JSON" in {
      val schema = Json.parse(getClass.getResource("/json/schema/example-1-schema.json"))
      val json = Json.parse(getClass.getResource("/json/example-1.json"))

      schema.parse(json) must beSuccessfulTry[JsValue].like {
        case j: JsValue =>
          (j \ "id").as[String] mustEqual "http://www.kissthinker.com/json-schema-ui/example-1"
          (j \ "applicantDetails" \ "personalDetails" \ "fullName").as[String] mustEqual "Batman"
      }
    }

    "fail to parse given JSON" in {
      val schema = Json.parse(getClass.getResource("/json/schema/example-1-schema.json"))

      val json = Json.parse("""
        {
          "JSON": "This does not conform to the schema"
        }
        """)

      schema.parse(json) must beFailedTry
    }
  }
}