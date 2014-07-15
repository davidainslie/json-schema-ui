package controllers

import play.api.libs.ws.WS
import play.api.libs.json._
import play.api.test._
import com.mongodb.util.JSON
import com.mongodb.casbah.Imports._
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.specs2.mutable.Tags
import data.mongo.EmbeddedMongoSpecification
import data.json._

/**
 * Note on "browser" and "server" examples.
 * One example shows a "browser submission" commented out -
 * that "submit" is not tested as the server responds with asynchronous JSON which is better tested using "WithServer" i.e. a "server integration" example.
 */
class DataIntegrationSpec extends PlaySpecification with EmbeddedMongoSpecification with Tags {
  override def seed(db: MongoDB) = db("schemas").insert {
    val schema: String = getClass.getResource("/json/schema/example-1-schema.json")
    JSON.parse(schema.replace("$schema", "schema"))
  }

  "Data" should {
    // TODO
    /*"be presented" in new WithBrowser(webDriver = new PhantomJSDriver, app = FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      browser.goTo("#/single-page/http://www.kissthinker.com/json-schema-ui/example-1")
      browser.pageSource() must contain("Example 1")

      browser.findFirst("#premiseslicensingNumber").isDisplayed must beFalse
    }*/

    // TODO Decide if "submit" button should initially be disabled
    /*"not be submittable when not all necessary data is provided" in new WithBrowser(webDriver = new PhantomJSDriver, app = FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      browser.goTo("#/data/example-1")
      browser.findFirst("#submitForm").isEnabled must beFalse

      // SEE CLASS DOCUMENTATION
      // browser.click("#submit")
    }*/

    // TODO Must provide all data to enable "submit" button
    /*"be submittable when all necessary data is provided" in new WithBrowser(webDriver = new PhantomJSDriver, app = FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      browser.goTo("#/data/example-1")

      browser.click("#personalDetailsTitle option[value='Mr']")
      browser.fill("#personalDetailsFirstName-input") `with` "Bat"
      browser.fill("#personalDetailsLastName-input") `with` "Man"

      browser.findFirst("#submit").isEnabled must beTrue
    }*/
  } section("browser", "integration")

  "Data submission" should {
    "fail when not all necessary data is provided" in new WithServer(app = FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      import data.json._

      val example = Json.parse(getClass.getResource("/json/example-1.json"))
      val fullNamePath = __ \ "applicantDetails" \ "personalDetails" \ "fullName"

      await(WS.url("http://localhost:19001/api/data").post(example.transform(fullNamePath.json.prune).get)).status mustEqual BAD_REQUEST
    }

    "occur when all necessary data is provided" in new WithServer(app = FakeApplication(additionalConfiguration = embeddedMongoConfiguration)) {
      import data.json._

      val example = Json.parse(getClass.getResource("/json/example-1.json"))

      await(WS.url("http://localhost:19001/api/data").post(example)).status mustEqual CREATED
    }
  } section("server", "integration")
}