package com.synhaptein.migmongo.commands

import scala.concurrent.Future
import reactivemongo.api.DefaultDB

trait ChangeSet {
  val changeId: String
  val author: String
}

case class SyncChangeSet(changeId: String, author: String, changes: DefaultDB => List[Future[_]]) extends ChangeSet

case class AsyncChangeSet(changeId: String, author: String, changes: DefaultDB => List[Future[_]]) extends ChangeSet
