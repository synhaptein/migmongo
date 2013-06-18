package com.synhaptein.migmongo

import commands._
import collection.mutable
import com.mongodb.casbah.MongoDB
import commands.AsyncChangeSet
import dao.MigmongoDao
import org.slf4j.LoggerFactory
import akka.actor.ActorSystem
import akka.dispatch.Future

trait MigmongoEngine {
  private implicit val system = ActorSystem("MigmongoEngine")
  private val loggerName = this.getClass.getName
  private lazy val logger = LoggerFactory.getLogger(loggerName)
  private val changeGroups = mutable.MutableList[ChangeGroup]()
  private lazy val dao = MigmongoDao(db)
  val db: MongoDB

  def process() = {
    for {
      changeGroup <- changeGroups
      changeSet <- changeGroup.changeSets
      if !dao.wasExecuted(changeGroup.group, changeSet)
    }
    yield {
      def runChanges(changes: MongoDB => Unit) = {
        try {
          changes(db)
        }
        catch {
          case e: Exception =>
            logger.error(changeSet.toString, e)
            throw e
        }

        dao.logChangeSet(changeGroup.group, changeSet)
        logger.info("ChangeSet " + changeSet.changeId + " has been executed")
      }

      changeSet match {
        case changeSet: AsyncChangeSet =>
          Future {
            logger.info("Start async ChangeSet " + changeSet.changeId)
            runChanges(changeSet.changes)
          }
        case changeSet: SyncChangeSet =>
          logger.info("Start sync ChangeSet " + changeSet.changeId)
          runChanges(changeSet.changes)
      }
    }
  }

  protected def changeGroups(changeGroups: ChangeGroup*) {
    this.changeGroups ++= changeGroups
  }
}
