package com.synhaptein.migmongo.commands

import scala.concurrent.Future
import reactivemongo.api.DefaultDB

import scala.concurrent.duration.Duration

trait ChangeSet {
  val changeId: String
  val author: String
  val timeout: Option[Duration]
}

case class SyncChangeSet(changeId: String, author: String, changes: DefaultDB => List[Future[_]], timeout: Option[Duration] = None) extends ChangeSet

case class AsyncChangeSet(changeId: String, author: String, changes: DefaultDB => List[Future[_]], timeout: Option[Duration] = None) extends ChangeSet
