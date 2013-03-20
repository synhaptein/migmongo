package com.synhaptein.migmongo.dao

import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.commons.MongoDBObject
import com.synhaptein.migmongo.commands.ChangeSet
import java.util.Date
import com.mongodb.WriteConcern

case class MigmongoDao(db: MongoDB) {
  def ensureIndex = {
    val indexes = MongoDBObject("file" -> 1, "changeId" -> 1, "author" -> 1)
    db.getCollection("migmongo").ensureIndex(indexes)
  }

  def wasExecuted(group: String, changeSet: ChangeSet) = {
    val query = MongoDBObject(
      "group" -> group,
      "changeId" -> changeSet.changeId,
      "author" -> changeSet.author
    )
    db.getCollection("migmongo").count(query) > 0
  }

  def logChangeSet(group: String, changeSet: ChangeSet) {
    val log = MongoDBObject(
      "group" -> group,
      "changeId" -> changeSet.changeId,
      "author" -> changeSet.author,
      "timestamp" -> new Date()
    )
    db.getCollection("migmongo").insert(log, WriteConcern.SAFE)
  }
}
