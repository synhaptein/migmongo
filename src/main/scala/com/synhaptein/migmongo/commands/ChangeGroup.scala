package com.synhaptein.migmongo.commands

import collection.mutable
import reactivemongo.api.DefaultDB
import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait ChangeGroup {
  protected[migmongo] val changeSets = mutable.MutableList[ChangeSet]()
  val group: String

  protected def changeSet(changeId: String, author: String, timeout: Option[Duration] = None)(changes: (DefaultDB) => List[Future[_]]) {
    changeSets += SyncChangeSet(changeId, author, changes, timeout)
  }

  protected def asyncChangeSet(changeId: String, author: String, timeout: Option[Duration] = None)(changes: (DefaultDB) => List[Future[_]]) {
    changeSets += AsyncChangeSet(changeId, author, changes, timeout)
  }
}
