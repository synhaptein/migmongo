import scala.concurrent.ExecutionContext.Implicits.global
import com.synhaptein.migmongo.commands.{ChangeGroup}
import com.synhaptein.migmongo.MigmongoEngine
import concurrent.Await
import org.scalatest.FunSuite
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONDocument
import scala.concurrent.duration._

class MigMongoSuite extends FunSuite {
  test("Test a migration") {
    def createConnection = MigmongoEngine.db("mongodb://localhost/migmongo-test")

    case class MigrationV1(group: String) extends ChangeGroup {
      changeSet("ChangeSet-1", "author1") { db =>
        Thread.sleep(5000)
        List(
          db[BSONCollection]("table1").insert(BSONDocument("name" -> "John Doe")),
          db[BSONCollection]("table2").indexesManager.ensure(Index(Seq("field1" -> IndexType.Ascending, "field2" -> IndexType.Descending)))
        )
      }

      // Will be fire-and-forget
      asyncChangeSet("ChangeSet-2", "author2") { db =>
        Thread.sleep(5000)
        List(
        db[BSONCollection]("collection1").update(
          selector = BSONDocument(),
          update = BSONDocument("$set" -> BSONDocument("price" -> 180)),
          multi = true)
        )
      }

      changeSet("ChangeSet-3", "author1") { db =>
        Thread.sleep(5000)
        List(
          db[BSONCollection]("table1").insert(BSONDocument("name" -> "Jane Doe")),
          db[BSONCollection]("table2").indexesManager.ensure(Index(Seq("field1" -> IndexType.Ascending, "field2" -> IndexType.Descending)))
        )
      }

      changeSet("ChangeSet-4", "author1") { db =>
        List(
          db[BSONCollection]("table1").insert(BSONDocument("name" -> "Jane Doe")),
          db[BSONCollection]("table2").indexesManager.ensure(Index(Seq("field1" -> IndexType.Ascending, "field2" -> IndexType.Descending)))
        )
      }
    }

    case class MigTest(db: DefaultDB) extends MigmongoEngine {
      changeGroups(
        MigrationV1("v1")
      )
    }

    val db = createConnection

    Await.result(db[BSONCollection]("migmongo").remove(BSONDocument()), 20 seconds)

    assert(Await.result(MigTest(db).process(), 30 seconds) === 4)
    assert(Await.result(MigTest(createConnection).process(), 30 seconds) === 0)
  }
}
