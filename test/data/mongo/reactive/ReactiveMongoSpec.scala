package data.mongo.reactive

import scala.concurrent.duration._
import scala.concurrent.Await
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.{JsValue, Json}
import reactivemongo.api._
import org.specs2.mutable.Specification
import data.mongo.EmbeddedMongo

class ReactiveMongoSpec extends Specification with EmbeddedMongo {
  "JSON" should {
    "be saved in Mongo" in {
      val mongoDriver = new MongoDriver
      val mongoConnection = mongoDriver.connection(List(s"$host:$port"))
      val mongodb = mongoConnection(database)

      val collection = mongodb.collection[JSONCollection]("applications")

      Await.result(collection.save(Json.obj("key" -> "value")), DurationInt(5).seconds)

      Await.result(collection.find(Json.obj("key" -> "value")).one[JsValue], DurationInt(5).seconds) must beLike {
        case Some(json: JsValue) => (json \ "key").as[String] mustEqual "value"
      }
    }
  }
}