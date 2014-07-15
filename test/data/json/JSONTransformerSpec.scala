package data.json

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError
import org.specs2.mutable.Specification

class JSONTransformerSpec extends Specification {
  val emptyObject = __.json.put(Json.obj())

  "JSON" should {
    "be read and validated" in {
      val reads: Reads[JsObject] = (
        (__ \ 'name).read[String] ~
        (__ \ 'telephone).read[String]
      ).tupled ~> implicitly[Reads[JsObject]]

      val json = Json.obj("name" -> "Batman", "telephone" -> "123")

      json.transform(reads) must beLike {
        case JsSuccess(j: JsObject, _) =>
          (j \ "name").as[String] mustEqual "Batman"
          (j \ "telephone").as[String] mustEqual "123"
      }
    }
  }

  "Contact with mandatory name and telephone" should {
    val contactReads: Reads[JsObject] = (
      (__ \ 'name).json.pickBranch and
      (__ \ 'telephone).json.pickBranch
    ).reduce

    "be transformed" in {
      val json = Json.obj("name" -> "Batman", "telephone" -> "123")

      json.transform(contactReads) must beLike {
        case JsSuccess(j: JsObject, _) =>
          (j \ "name").as[String] mustEqual "Batman"
          (j \ "telephone").as[String] mustEqual "123"
      }
    }

    "not be transformed because of missing mandatory telephone" in {
      val json = Json.obj("name" -> "Batman")

      json.transform(contactReads) must beLike {
        case JsError(List((jsPath, List(ValidationError(message, _*))))) =>
          jsPath mustEqual __ \ 'telephone
          message mustEqual "error.path.missing"
      }
    }
  }

  "Contact with mandatory name and optional telephone" should {
    val contactReads: Reads[JsObject] = (
      (__ \ 'name).json.pickBranch and
      ((__ \ 'telephone).json.pickBranch or emptyObject)
    ).reduce

    "be transformed" in {
      val json = Json.obj("name" -> "Batman", "telephone" -> "123")

      json.transform(contactReads) must beLike {
        case JsSuccess(j: JsObject, _) =>
          (j \ "name").as[String] mustEqual "Batman"
          (j \ "telephone").as[String] mustEqual "123"
      }
    }

    "be transformed when missing optional telephone" in {
      val json = Json.obj("name" -> "Batman")

      json.transform(contactReads) must beLike {
        case JsSuccess(j: JsObject, _) =>
          (j \ "name").as[String] mustEqual "Batman"
          (j \ "telephone").asOpt[String] must beNone
      }
    }
  }

  "Contact with 'validation rules' and mandatory name and telephone" should {
    val contactReads: Reads[JsObject] = (
      (__ \ 'name).json.pickBranch and
      (__ \ 'telephone).json.pickBranch(of[JsString] <~ minLength[String](5)) // OR verifying[String](_.length >= 5)
    ).reduce

    "be transformed" in {
      val json = Json.obj("name" -> "Batman", "telephone" -> "12345")

      json.transform(contactReads) must beLike {
        case JsSuccess(j: JsObject, _) =>
          (j \ "name").as[String] mustEqual "Batman"
          (j \ "telephone").as[String] mustEqual "12345"
      }
    }

    "be transformed when missing optional telephone" in {
      val json = Json.obj("name" -> "Batman", "telephone" -> "1")

      json.transform(contactReads) must beLike {
        case JsError(List((jsPath, e @ List(ValidationError(message, _*))))) =>
          println(e)
          jsPath mustEqual __ \ 'telephone
          // message mustEqual "error.invalid" WHEN using commented out "verifying"
          message mustEqual "error.minLength"
      }
    }
  }

  "Temp Event Licence application consisting of Contact with mandatory name and telephone" should {
    val contactReads: Reads[JsObject] =
      (__ \ 'tempEvent).json.pickBranch(
        (__ \ 'contact).json.pickBranch((
          (__ \ 'name).json.pickBranch and
          (__ \ 'telephone).json.pickBranch).reduce
        )
      )

    "be transformed" in {
      val json = Json.obj(
        "tempEvent" -> Json.obj(
          "contact" -> Json.obj(
            "name" -> "Batman",
            "telephone" -> "12345"
          )
        )
      )

      json.transform(contactReads) must beLike {
        case JsSuccess(j: JsObject, _) =>
          (j \ "tempEvent" \ "contact" \ "name").as[String] mustEqual "Batman"
          (j \ "tempEvent" \ "contact" \ "telephone").as[String] mustEqual "12345"
      }
    }

    "not be transformed because of missing mandatory telephone" in {
      val json = Json.obj(
        "tempEvent" -> Json.obj(
          "contact" -> Json.obj(
            "name" -> "Batman"
          )
        )
      )

      json.transform(contactReads) must beLike {
        case JsError(List((jsPath, List(ValidationError(message, _*))))) =>
          jsPath mustEqual __ \ 'tempEvent \ 'contact \ 'telephone
          message mustEqual "error.path.missing"
      }
    }
  }

  "Data dynamically wrapped around contact with mandatory name and telephone" should {
    val contactReads: Reads[JsObject] =
      (__ \ 'data).json.copyFrom(
        (__ \ 'contact).json.pickBranch((
          (__ \ 'name).json.pickBranch and
          (__ \ 'telephone).json.pickBranch).reduce))

    "be transformed" in {
      val json = Json.obj(
        "contact" -> Json.obj(
          "name" -> "Batman",
          "telephone" -> "12345"
        )
      )

      json.transform(contactReads) must beLike {
        case JsSuccess(j: JsObject, _) =>
          (j \ "data" \ "contact" \ "name").as[String] mustEqual "Batman"
          (j \ "data" \ "contact" \ "telephone").as[String] mustEqual "12345"
      }
    }

    "not be transformed because of missing mandatory telephone" in {
      val json = Json.obj(
        "contact" -> Json.obj(
          "name" -> "Batman"
        )
      )

      json.transform(contactReads) must beLike {
        case JsError(List((jsPath, List(ValidationError(message, _*))))) =>
          jsPath mustEqual __ \ 'contact \ 'telephone
          message mustEqual "error.path.missing"
      }
    }
  }
}