import com.synhaptein.migmongo.MigmongoEngine
import com.synhaptein.migmongo.commands.ChangeGroup
import org.scalatest.FunSuite
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType._
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class MigMongoSuite extends FunSuite {
  val driver = MongoDriver()

  def connectTo(uriStr: String) = {
    val uri = MongoConnection.parseURI(uriStr) match {
      case Success(parsedUri) =>
        parsedUri
      case Failure(e) =>
        throw e
    }

    val connection = driver.connection(uri)

    uri.authenticate foreach { auth =>
      Await.result(connection.authenticate(auth.db, auth.user, auth.password), Duration(10, SECONDS))
    }

    connection(uri.db.get)
  }

  test("Test a migration") {
    def createConnection = connectTo("mongodb://localhost/migmongo-test")

    case class MigrationMyApp(group: String) extends ChangeGroup {
      changeSet("ChangeSet-1", "author1") { db =>
        List(
          db[BSONCollection]("table1").insert(BSONDocument("name" -> "John Doe")),
          db[BSONCollection]("table2").indexesManager.ensure(Index(Seq("field1" -> Ascending, "field2" -> Descending)))
        )
      }

      // Will be fire-and-forget
      asyncChangeSet("ChangeSet-2", "author2") { db =>
        List(
        db[BSONCollection]("collection1").update(
          selector = BSONDocument(),
          update = BSONDocument("$set" -> BSONDocument("price" -> 180)),
          multi = true)
        )
      }

      changeSet("ChangeSet-3", "author1") { db =>
        List(
          db[BSONCollection]("table1").insert(BSONDocument("name" -> "Jane Doe")),
          db[BSONCollection]("table2").indexesManager.ensure(Index(Seq("field1" -> Ascending, "field2" -> Descending)))
        )
      }

      changeSet("ChangeSet-4", "author1") { db =>
        List(
          db[BSONCollection]("table1").insert(BSONDocument("name" -> "Jane Doe")),
          db[BSONCollection]("table2").indexesManager.ensure(Index(Seq("field1" -> Ascending, "field2" -> Descending)))
        )
      }
    }

    case class MigTest(db: DefaultDB) extends MigmongoEngine {
      changeGroups(
        MigrationMyApp("myApp")
      )
    }

    val db = createConnection

    Await.result(db[BSONCollection]("migmongo").remove(BSONDocument()), 20 seconds)

    assert(Await.result(MigTest(db).process(), 30 seconds) === 4)
    assert(Await.result(MigTest(db).process(), 30 seconds) === 0)
  }
}
