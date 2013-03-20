package com.synhaptein.migmongo.commands

trait ChangeSet {
  val changeId: String
  val author: String
}

case class SyncChangeSet(changeId: String, author: String, changes: Change*) extends ChangeSet

case class AsyncChangeSet(changeId: String, author: String, changes: Change*) extends ChangeSet
