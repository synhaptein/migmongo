package com.synhaptein.migmongo

import commands._
import collection.mutable
import commands.AsyncChangeSet
import dao.MigmongoDao
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.{DefaultDB, MongoDriver}
import scala.concurrent.{Future, Await}

object MigmongoEngine {
  def db(uriStr: String) = {
    case class MongoURI(host: String, port: Int, username: Option[String], password: Option[String], database: String)

    val UriPattern = "mongodb://(.*):(.*)@(.*):([0-9]*)/(.*)".r
    val UriPatternSimple = "mongodb://(.*)/(.*)".r

    val uri = uriStr match {
      case UriPattern(username, password, host, port, database) =>
        MongoURI(host, port.toInt, Some(username), Some(password), database)
      case UriPatternSimple(host, database) =>
        MongoURI(host, 27017, None, None, database)
      case _ =>
        throw new RuntimeException("Bad format MongoDB URI")
    }

    val connection = {
      val driver = new MongoDriver
      val conn = driver.connection(List(uri.host + ":" + uri.port))

      for {
        username <- uri.username
        password <- uri.password
      }
      {
        Await.result(conn.authenticate(uri.database, username, password), Duration(10, SECONDS))
      }
      conn
    }

    connection(uri.database)
  }
}

trait MigmongoEngine {
  private val loggerName = this.getClass.getName
  private lazy val logger = LoggerFactory.getLogger(loggerName)
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
      val wasExectuted = dao.wasExecuted(changeGroup.group, changeSet)

      val result = changeSet match {
        case changeSet: AsyncChangeSet =>
          wasExectuted flatMap {
            case true =>
              Future.successful(false)
            case _ =>
              logger.info("Start async ChangeSet " + changeSet.changeId)
              Future.sequence(changeSet.changes(db) map (_.map(_ => true))) map (_ => true)
          }
        case changeSet: SyncChangeSet =>
          val isExecute = !Await.result(wasExectuted, Duration(1000, MINUTES))
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
      db.connection.close()
    }

    mergedResults
  }

  protected def changeGroups(changeGroups: ChangeGroup*) {
    this.changeGroups ++= changeGroups
  }
}
