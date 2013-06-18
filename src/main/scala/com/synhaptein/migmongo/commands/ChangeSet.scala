package com.synhaptein.migmongo.commands

import com.mongodb.casbah.MongoDB

trait ChangeSet {
  val changeId: String
  val author: String
}

case class SyncChangeSet(changeId: String, author: String, changes: MongoDB => Unit) extends ChangeSet

case class AsyncChangeSet(changeId: String, author: String, changes: MongoDB => Unit) extends ChangeSet
