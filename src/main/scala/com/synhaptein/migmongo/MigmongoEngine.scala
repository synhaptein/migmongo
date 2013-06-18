package com.synhaptein.migmongo

import commands._
import collection.mutable
import com.mongodb.casbah.{MongoConnection, MongoDB}
import commands.AsyncChangeSet
import dao.MigmongoDao
import org.slf4j.LoggerFactory
import akka.actor.{Props, ActorSystem, Actor}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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

    lazy val connection = MongoConnection(uri.host, uri.port)

    val dbref = connection(uri.database)
    uri.username foreach ( username => uri.password foreach ( password => dbref.authenticate(username, password.toString)))
    dbref
  }
}

trait MigmongoEngine {
  private val loggerName = this.getClass.getName
  private lazy val logger = LoggerFactory.getLogger(loggerName)
  private val changeGroups = mutable.MutableList[ChangeGroup]()
  private lazy val dao = MigmongoDao(db)
  private lazy val system = ActorSystem("MessageProcessor")
  private val asyncChangeActor = system.actorOf(Props(new AsyncChangeActor(db)))
  val db: MongoDB
  implicit val timeout = Timeout(5 seconds)

  def process(closeDb: Boolean = false) = {
    var count = 0
    for {
      changeGroup <- changeGroups
      changeSet <- changeGroup.changeSets
      if !dao.wasExecuted(changeGroup.group, changeSet)
    }
    yield {
      count += 1
      def runChanges(changes: Change*) = {
        for(change <- changes) {
          try {
            change.run(db)
          }
          catch {
            case e: Exception =>
              logger.error(change.toString, e)
              throw e
          }
        }
        dao.logChangeSet(changeGroup.group, changeSet)
        logger.info("ChangeSet " + changeSet.changeId + " has been executed")
      }

      changeSet match {
        case changeSet: AsyncChangeSet =>
          val run = { () =>
            logger.info("Start async ChangeSet " + changeSet.changeId)
            runChanges(changeSet.changes:_*)
          }
          asyncChangeActor ! run
        case changeSet: SyncChangeSet =>
          logger.info("Start sync ChangeSet " + changeSet.changeId)
          runChanges(changeSet.changes:_*)
      }
    }

    (asyncChangeActor ? CloseDB(closeDb)) map (_ => count)
  }

  protected def changeGroups(changeGroups: ChangeGroup*) {
    this.changeGroups ++= changeGroups
  }
}

case class CloseDB(closeDb: Boolean)

class AsyncChangeActor(db: MongoDB) extends Actor {
  def receive = {
    case f: ( () => Unit ) =>
      f()
    case CloseDB(closeDb) =>
      if(closeDb) {
        db.underlying.getMongo.close()
      }
      sender ! CloseDB(closeDb)
  }
}
