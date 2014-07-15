package controllers

import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.test.{FakeApplication, FakeRequest, PlaySpecification, WithApplication}
import com.mongodb.util.JSON
import com.mongodb.casbah.Imports._
import data.mongo.EmbeddedMongoSpecification
import data.json._

class DataSpec extends PlaySpecification with EmbeddedMongoSpecification {
  override def seed(db: MongoDB) = db("schemas").insert {
    val schema: String = getClass.getResource("/json/schema/example-1-schema.json")
    JSON.parse(schema.replace("$schema", "schema"))
  }

  "Data" should {
    val data = Json.obj(
      "id" -> "http://www.kissthinker.com/json-schema-ui/example",
      "applicantDetails" -> Json.obj(
        "personalDetails" -> Json.obj(
          "title" -> "Mr",
          "fullName" -> "Batman"
        )
      )
    )

    "not be matched with appropriate JSON schema" in new WithApplication(app = FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val result = Data.schema("non-existing")(FakeRequest())
      status(result) mustEqual NOT_FOUND
    }

    "be matched with appropriate JSON schema" in new WithApplication(app = FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val result = Data.schema("http://www.kissthinker.com/json-schema-ui/example")(FakeRequest())

      status(result) mustEqual OK
      contentType(result) must beSome(MimeTypes.JSON)

      val jsonSchema = contentAsJson(result)
      (jsonSchema \ "id").as[String] mustEqual "http://www.kissthinker.com/json-schema-ui/example"
    }

    "not be created because of no matching JSON schema given data" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val invalidData = data ++ Json.obj("id" -> "non-existing")

      val request = FakeRequest().withBody(invalidData)
      val result = Data.save(request)
      status(result) mustEqual NOT_FOUND
    }

    "be created" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val request = FakeRequest().withBody(data)
      val result = Data.save(request)
      status(result) mustEqual CREATED
    }

    "not be created because of missing full name" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val fullNamePath = __ \ "applicantDetails" \ "personalDetails" \ "fullName"
      val request = FakeRequest().withBody(data.transform(fullNamePath.json.prune).get)
      val result = Data.save(request)
      status(result) mustEqual BAD_REQUEST
    }

    "not allow included 'licensing number' for no 'premise licence' or 'certificate'" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val dataWithNeitherPremisesRelationship =
        data ++ Json.obj(
          "premises" -> Json.obj(
            "location" -> "Completed as this is required",
            "licensingPremisesRelationship" -> "Neither",
            "licensingNumber" -> "INVALID AS NEITHER SELECTED"
          )
        )

      val request = FakeRequest().withBody(dataWithNeitherPremisesRelationship)
      val result = Data.save(request)
      status(result) mustEqual BAD_REQUEST
    }

    "not allow included 'licensing number' for no 'licensing premises reletionship'" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val dataWithNeitherPremisesRelationship =
        data ++ Json.obj(
          "premises" -> Json.obj(
            "location" -> "Completed as this is required",
            "licensingNumber" -> "INVALID FOR NO SELECTION"
          )
        )

      val request = FakeRequest().withBody(dataWithNeitherPremisesRelationship)
      val result = Data.save(request)
      status(result) mustEqual BAD_REQUEST
    }

    "not include 'licensing number' for no 'premise licence' or 'certificate'" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val dataWithNeitherPremisesRelationship =
        data ++ Json.obj(
          "premises" -> Json.obj(
            "location" -> "Completed as this is required",
            "licensingPremisesRelationship" -> "Neither"
          )
        )

      val request = FakeRequest().withBody(dataWithNeitherPremisesRelationship)
      val result = Data.save(request)
      status(result) mustEqual CREATED
    }

    "not include 'licensing number' of 'licensing premises relationship'" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val dataWithNeitherPremisesRelationship =
        data ++ Json.obj(
          "premises" -> Json.obj(
            "location" -> "Completed as this is required"
          )
        )

      val request = FakeRequest().withBody(dataWithNeitherPremisesRelationship)
      val result = Data.save(request)
      status(result) mustEqual CREATED
    }

    "include 'licensing number' for 'premise licence' or 'certificate'" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val dataWithNeitherPremisesRelationship =
        data ++ Json.obj(
          "premises" -> Json.obj(
            "location" -> "Completed as this is required",
            "licensingPremisesRelationship" -> "Club premises certificate",
            "licensingNumber" -> "club"
          )
        )

      val request = FakeRequest().withBody(dataWithNeitherPremisesRelationship)
      val result = Data.save(request)
      status(result) mustEqual CREATED
    }

    "not allow missing 'licensing number' for 'premise licence' or 'certificate'" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val dataWithNeitherPremisesRelationship =
        data ++ Json.obj(
          "premises" -> Json.obj(
            "location" -> "Completed as this is required",
            "licensingPremisesRelationship" -> "Club premises certificate"
          )
        )

      val request = FakeRequest().withBody(dataWithNeitherPremisesRelationship)
      val result = Data.save(request)
      status(result) mustEqual BAD_REQUEST
    }
  }

  val example = Json.parse(getClass.getResource("/json/schema/example-1-schema.json"))

  "Data example" should {
    "be created" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val request = FakeRequest().withBody(example)
      val result = Data.save(request)
      status(result) mustEqual CREATED
    }

    "be created again" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val request = FakeRequest().withBody(example)
      val result = Data.save(request)
      status(result) mustEqual CREATED
    }

    "not be created because of missing 'full name'" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val fullNameRemovalTransformer = (__ \ 'applicantDetails \ 'personalDetails \ 'fullName).json.prune

      val request = FakeRequest().withBody(example.transform(fullNameRemovalTransformer).get)
      val result = Data.save(request)
      status(result) mustEqual BAD_REQUEST
    }
  }

  "Data example via its route" should {
    "be created" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val Some(result) = route(FakeRequest(POST, "/api/data"), example)
      status(result) mustEqual CREATED
    }

    "not be created because of missing 'full name'" in new WithApplication(FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      val fullNameRemovalTransformer = (__ \ 'applicantDetails \ 'personalDetails \ 'fullName).json.prune

      val Some(result) = route(FakeRequest(POST, "/api/data").withHeaders("content-type" -> "application/json"), example.transform(fullNameRemovalTransformer).get)

      status(result) mustEqual BAD_REQUEST
    }
  }
}