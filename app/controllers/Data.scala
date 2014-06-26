package controllers

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import data.json._
import utility.Logging

trait Data extends MongoController with Logging {
  this: Controller =>

  lazy val (enumerator, channel) = Concurrent.broadcast[JsValue]

  /**
   * JSONCollection (a Collection implementation that is designed to work with JsObject, Reads and Writes.)
   * Note that the "collection" is not a "val", but a "def".
   * By not storing the collection reference, potential problems are avoided in development with Play hot-reloading.
   */
  def dataCollection: JSONCollection = db.collection[JSONCollection]("data")

  def schemasCollection: JSONCollection = db.collection[JSONCollection]("schemas")

  def schema(id: String) = Action.async {
    info(s"Client requested: $id")

    // TODO Put back $ in "schema" property as $ was not allowed to be stored in Mongo
    schemasCollection.find(Json.obj("id" -> id)).one[JsValue].map {
      case Some(schema) => Ok(schema)
      case _ => NotFound
    }
  }

  /**
   * request.body is a JsValue.
   * There is an implicit Writes that turns this JsValue as a JsObject, so you can call insert() with this JsValue.
   * insert() takes a JsObject as parameter, or anything that can be turned into a JsObject using a Writes.
   */
  def save = Action.async(parse.json) { request =>
    request.body \ "id" match {
      case id: JsString =>
        info(s"Client submitting: $id")

        // TODO Put back $ in "schema" property as $ was not allowed to be stored in Mongo
        schemasCollection.find(Json.obj("id" -> id)).one[JsValue].flatMap {
          case Some(schema: JsValue) =>
            schema.parse(request.body).map { data =>
              import reactivemongo.bson.BSONObjectID
              import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat

              val _id = BSONObjectID.generate
              val dataBSONObject = Json.obj("_id" -> _id) ++ data.as[JsObject]
              // TODO Add timestamp - first needs to be declared in schema

              dataCollection.insert(dataBSONObject).map { _ => // Don't care about lastError here (map is called on success)
                info(s"Created: $dataBSONObject")

                channel push dataBSONObject
                Created(Json.obj("response" -> s"""Successfully saved with generated ID: ${_id.stringify}"""))
              }
            } getOrElse {
              error(s"Failed to save because of invalid JSON: ${request.body}")
              Future(BadRequest(Json.obj("response" -> "Data save failure because data was given as invalid JSON.")))
            }

          case _ =>
            error(s"Failed to save data because no schema was found for given JSON: ${request.body}")
            Future(NotFound(Json.obj("response" -> "Data save failure because no schema was found for given JSON.")))
        }

      case e =>
        error(e)
        Future(BadRequest(Json.obj("response" -> "Data save failure because no schema ID was provided.")))
    }
  }

  def subscribe = WebSocket.using[JsValue] { request =>
    info(s"A client connected: ${request.remoteAddress}")
    (Iteratee.ignore, enumerator)
  }
}

object Data extends Controller with Data