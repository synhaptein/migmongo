import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.MongoDB
import com.synhaptein.migmongo.commands.{ChangeGroup}
import com.synhaptein.migmongo.MigmongoEngine
import concurrent.Await
import org.scalatest.FunSuite
import scala.concurrent.duration._

class MigMongoSuite extends FunSuite {
  test("Test a migration") {
    val db = MigmongoEngine.db("mongodb://localhost/migmongo-test")

    case class MigrationV1(group: String) extends ChangeGroup {
      changeSet("ChangeSet-1", "author1") { db =>
        db("table1").drop()
        db("table2").ensureIndex(MongoDBObject("field1" -> 1, "field2" -> -1))
      }

      // Will be fire-and-forget
      asyncChangeSet("ChangeSet-2", "author2") { db =>
        db("collection1").update(MongoDBObject(), MongoDBObject("$set" -> MongoDBObject("price" -> 180)), false, true)
      }
    }

    case class MigTest(db: MongoDB) extends MigmongoEngine {
      changeGroups(
        MigrationV1("v1")
      )
    }

    db("migmongo").drop()

    assert(Await.result(MigTest(db).process(), 10 seconds) === 2)
    assert(Await.result(MigTest(db).process(), 10 seconds) === 0)
  }
}
