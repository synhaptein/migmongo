package com.synhaptein.migmongo.commands

import collection.mutable

trait ChangeGroup {
  protected[migmongo] val changeSets = mutable.MutableList[ChangeSet]()
  val group: String

  protected def changeSet(changeId: String, author: String, changes: Change*) {
    changeSets += SyncChangeSet(changeId, author, changes:_*)
  }

  protected def asyncChangeSet(changeId: String, author: String, changes: Change*) {
    changeSets += AsyncChangeSet(changeId, author, changes:_*)
  }
}
