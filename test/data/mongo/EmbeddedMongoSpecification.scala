package data.mongo

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification.AroundExample
import com.mongodb.casbah.Imports._

trait EmbeddedMongoSpecification extends EmbeddedMongo with AroundExample {
  this: Specification =>

  implicit def toDBObject(o: Object) = o.asInstanceOf[DBObject]

  def seed(db: MongoDB) = ()

  override protected def around[T : AsResult](t: => T) = {
    import com.mongodb.casbah.Imports._

    val mongoClient = MongoClient(host, port)
    val db = mongoClient(database)

    seed(db)

    try {
      AsResult(t)
    } finally {
      db.dropDatabase()
    }
  }
}