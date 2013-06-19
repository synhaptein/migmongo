package com.synhaptein.migmongo.commands

import collection.mutable
import com.mongodb.casbah.MongoDB

trait ChangeGroup {
  protected[migmongo] val changeSets = mutable.MutableList[ChangeSet]()
  val group: String

  protected def changeSet(changeId: String, author: String)(changes: (MongoDB) => Unit) {
    changeSets += SyncChangeSet(changeId, author, changes)
  }

  protected def asyncChangeSet(changeId: String, author: String)(changes: (MongoDB) => Unit) {
    changeSets += AsyncChangeSet(changeId, author, changes)
  }
}
