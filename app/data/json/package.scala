package data

import java.net.URL
import scala.io.Source
import scala.util.{Failure, Success, Try}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json, JsValue}
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.exceptions.ProcessingException
import com.github.fge.jsonschema.main.JsonSchemaFactory
import utility.Logging

package object json extends Logging {
  implicit def urlContentToString(url: URL): String = Source.fromURL(url).getLines().mkString

  implicit def urlContentToJsValue(url: URL): JsValue = Json.parse(url)

  implicit def jsValueToString(j: JsValue): String = Json.stringify(j)

  def jsClasspathValue(classpath: String): JsValue = Json.parse(getClass.getResource(classpath))

  def jsClasspathObject(classpath: String): JsObject = Json.parse(getClass.getResource(classpath)).as[JsObject]

  def jsObject(fields: (String, JsValueWrapper)*): JsObject = Json.obj(fields: _*)

  implicit def jsValueToSchema(schema: JsValue) = new {
    def parse(json: JsValue): Try[JsValue] = try {
      val processingReport = jsonSchema.validate(JsonLoader.fromString(json.toString()))

      if (processingReport.isSuccess) {
        Success(json)
      } else {
        import scala.collection.JavaConversions._

        error(s"Given JSON is invalid")

        val errorMessages = for {
          processingMessage <- processingReport.iterator().toList
          message = processingMessage.toString if !message.contains("the following keywords are unknown and will be ignored")
        } yield message

        error(errorMessages)
        Failure(ParseException(errorMessages))
      }
    } catch {
      case e: ProcessingException => Failure(ParseException(List(e.getProcessingMessage.getMessage)))
      case t: Throwable => Failure(ParseException(List(t.getMessage)))
    }

    def jsonSchema = {
      val jsonSchemaNode = JsonLoader.fromString(schema.toString().replace("schema", "$schema"))

      val jsonSchema = {
        val syntaxValidator = JsonSchemaFactory.byDefault().getSyntaxValidator
        val processingReport = syntaxValidator.validateSchema(jsonSchemaNode)

        if (processingReport.isSuccess) {
          JsonSchemaFactory.byDefault().getJsonSchema(jsonSchemaNode)
        } else {
          import scala.collection.JavaConversions._

          error(s"Given JSON schema is invalid: $processingReport")
          throw SchemaException(processingReport.iterator().toList map { processingMessage => processingMessage.getMessage})
        }
      }

      jsonSchema
    }
  }

  case class SchemaException(errorMessages: List[String]) extends RuntimeException {
    override def getMessage = errorMessages.mkString(", ")
  }

  case class ParseException(errorMessages: List[String]) extends RuntimeException {
    override def getMessage = errorMessages.mkString(", ")
  }
}