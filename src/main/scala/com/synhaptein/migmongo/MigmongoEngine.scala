package com.synhaptein.migmongo

import commands._
import collection.mutable
import commands.AsyncChangeSet
import dao.MigmongoDao
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.{MongoConnection, DefaultDB, MongoDriver}
import scala.concurrent.{Future, Await}
import scala.util.{Failure, Success}
import akka.actor.ActorSystem

object Migmongo {
  private val loggerName = "migmongo"
  lazy val logger = LoggerFactory.getLogger(loggerName)
}

trait MigmongoEngine {
  import Migmongo.logger

  private val changeGroups = mutable.MutableList[ChangeGroup]()
  private lazy val dao = MigmongoDao(db)
  val db: DefaultDB

  def process() = {
    logger.info("Running db changeSets...")
    dao.ensureIndex
    val results = for {
      changeGroup <- changeGroups
      changeSet <- changeGroup.changeSets
    }
    yield {
      val wasExecuted = dao.wasExecuted(changeGroup.group, changeSet)

      val result = changeSet match {
        case changeSet: AsyncChangeSet =>
          wasExecuted flatMap {
            case true =>
              Future.successful(false)
            case _ =>
              logger.info("Start async ChangeSet " + changeSet.changeId)
              Future.sequence(changeSet.changes(db) map (_.map(_ => true))) map (_ => true)
          }
        case changeSet: SyncChangeSet =>
          val isExecute = !Await.result(wasExecuted, Duration(1000, MINUTES))
          if(isExecute) {
            logger.info("Start sync ChangeSet " + changeSet.changeId)
            changeSet.changes(db).foreach { change =>
              Await.result(change, Duration(1000, MINUTES))
            }
          }

          Future.successful(isExecute)
      }

      val fresult = result flatMap { r =>
        if(r) {
          dao.logChangeSet(changeGroup.group, changeSet) map { _ =>
            logger.info("ChangeSet " + changeSet.changeId + " has been executed")
            r
          }
        }
        else {
          Future.successful(r)
        }
      }

      fresult.onFailure {
        case e: Throwable =>
          logger.info("ChangeSet " + changeSet.changeId + " could not be executed", e)
          throw e
      }

      fresult
    }

    val mergedResults = Future.sequence(results.toList) map (l => l.filter(r => r).size)

    mergedResults foreach { _ =>
      logger.info("Migrations finished")
    }

    mergedResults
  }

  protected def changeGroups(changeGroups: ChangeGroup*) {
    this.changeGroups ++= changeGroups
  }
}
