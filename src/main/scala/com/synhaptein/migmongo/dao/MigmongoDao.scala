package com.synhaptein.migmongo.dao

import com.synhaptein.migmongo.commands.ChangeSet
import java.util.Date
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDateTime, BSONDocument}
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.core.commands.Count
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MigmongoDao(db: DefaultDB) {
  private val migmongo = db[BSONCollection]("migmongo")

  private def retry[T](n: Int)(fn: => Future[T]): Future[T] = {
    fn.recoverWith {
      case e: Throwable =>
        Thread.sleep(300)
        if (n > 1) retry(n - 1)(fn)
        else throw e
    }
  }

  def ensureIndex = {
    val index = Index(Seq("file" -> IndexType.Ascending, "changeId" -> IndexType.Ascending,  "author" -> IndexType.Ascending))
    migmongo.indexesManager.ensure(index)
  }

  def wasExecuted(group: String, changeSet: ChangeSet) = {
    val query = BSONDocument(
      "group" -> group,
      "changeId" -> changeSet.changeId,
      "author" -> changeSet.author
    )

    retry(10) {
      db.command(Count(migmongo.name, Some(query))) map (_ > 0)
    }
  }

  def logChangeSet(group: String, changeSet: ChangeSet) = {
    val log = BSONDocument(
      "group" -> group,
      "changeId" -> changeSet.changeId,
      "author" -> changeSet.author,
      "timestamp" -> BSONDateTime(new Date().getTime)
    )

    migmongo.insert(log)
  }
}
