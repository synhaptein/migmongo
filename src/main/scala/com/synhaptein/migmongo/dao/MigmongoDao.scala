package com.synhaptein.migmongo.dao

import com.synhaptein.migmongo.commands.ChangeSet
import java.util.Date
import reactivemongo.api.DefaultDB
import reactivemongo.bson.{BSONDateTime, BSONDocument}
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.core.commands.Count
import scala.concurrent.ExecutionContext.Implicits.global

case class MigmongoDao(db: DefaultDB) {
  private val migmongo = db[BSONCollection]("migmongo")

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

    db.command(Count(migmongo.name, Some(query))) map (_ > 0)
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
