package data.mongo

import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import org.specs2.mutable.Specification

trait EmbeddedMongo extends EmbedConnection with CleanAfterExample {
  this: Specification =>

  isolated

  val host = "localhost"

  val port = 12345

  val database = "embedded-licence-applications"

  val embeddedMongoConfiguration = Map("mongodb.servers" -> List(s"$host:$port"), "mongodb.db" -> database)
}