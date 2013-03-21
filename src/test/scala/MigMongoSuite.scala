import com.mongodb.casbah.MongoDB
import com.synhaptein.migmongo.commands.{Script, ChangeGroup}
import com.synhaptein.migmongo.MigmongoEngine
import concurrent.Await
import org.scalatest.FunSuite
import scala.concurrent.duration._

class MigMongoSuite extends FunSuite {
  test("Test a migration") {
    val db = MigmongoEngine.db("mongodb://localhost/migmongo-test")
    case class MigTest(db: MongoDB) extends MigmongoEngine {
      changeGroups(
        MigrationV1("v1")
      )
    }

    case class MigrationV1(group: String) extends ChangeGroup {
      changeSet("ChangeSet-1", "author1",
        Script("""db.table1.drop();"""),
        Script("""db.table2.ensureIndex({"field1":1, "field2":-1});""")
      )

      // Will be fire-and-forget
      asyncChangeSet("ChangeSet-2", "author2",
        Script("""db.collection1.update({}, {$set:{price:180}}, false ,true);""")
      )
    }
    db("migmongo").drop()
    assert(Await.result(MigTest(db).process(), 10 seconds) === 2)
    assert(Await.result(MigTest(db).process(), 10 seconds) === 0)
  }
}
