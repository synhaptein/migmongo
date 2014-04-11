package com.synhaptein.migmongo.commands

import collection.mutable
import reactivemongo.api.DefaultDB
import scala.concurrent.Future

trait ChangeGroup {
  protected[migmongo] val changeSets = mutable.MutableList[ChangeSet]()
  val group: String

  protected def changeSet(changeId: String, author: String)(changes: (DefaultDB) => List[Future[_]]) {
    changeSets += SyncChangeSet(changeId, author, changes)
  }

  protected def asyncChangeSet(changeId: String, author: String)(changes: (DefaultDB) => List[Future[_]]) {
    changeSets += AsyncChangeSet(changeId, author, changes)
  }
}
