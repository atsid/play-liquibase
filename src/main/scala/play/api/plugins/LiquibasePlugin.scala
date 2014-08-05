package play.api.plugins

import liquibase.Liquibase
import scala.collection.JavaConversions._
import liquibase.changelog.ChangeSet
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import play.api._
import play.api.db.{DB, DBPlugin}

/**
 * @date: 03.04.12
 * @author: Kaa
 */

class LiquibasePlugin(app: Application) extends Plugin {
  val TestContext = "test"
  val DeveloperContext = "dev"
  val ProductionContext = "prod"

  private def getScriptDescriptions(changeSets: Seq[ChangeSet]) = {
    changeSets.zipWithIndex.map {
      case (cl, num) =>
        "" + num + ". " + cl.getId +
          Option(cl.getDescription).map(" (" + _ + ")").getOrElse("") +
          " by " + cl.getAuthor
    }.mkString("\n")
  }

  override def onStart() {
    val api = app.plugin[DBPlugin].map(_.api).getOrElse(throw new Exception("there should be a database plugin registered at this point but looks like it's not available, so liquibase won't work. Please make sure you register a db plugin properly"))

    api.datasources.foreach {
      case (ds, dbName) => {
        val fileOpener = new FileSystemResourceAccessor(app.path.getAbsolutePath)
        DB.withConnection(dbName)(connection => {
          val defaultChangeLogPath = "conf/liquibase/" + dbName + "/changelog.xml"
          val changeLogPath = app.configuration.getString("applyLiquibase.changeLogPath." + dbName).orElse(Some(defaultChangeLogPath)).get
          val applyUpdatesKey = "applyLiquibase." + dbName + ".apply"

          val liqui = new Liquibase(changeLogPath, fileOpener, new JdbcConnection(connection))
          app.mode match {
            case Mode.Test => liqui.update(TestContext)
            case Mode.Dev if app.configuration.getBoolean(applyUpdatesKey).filter(_ == true).isDefined => liqui.update(DeveloperContext)
            case Mode.Prod if app.configuration.getBoolean(applyUpdatesKey).filter(_ == true).isDefined => liqui.update(ProductionContext)
            case Mode.Prod => {
              Logger("play").warn("Your production database [" + dbName + "] needs Liquibase updates! \n\n" + getScriptDescriptions(liqui.listUnrunChangeSets(ProductionContext)))
              Logger("play").warn("Run with -DapplyLiquibase." + dbName + "=true if you want to run them automatically (be careful)")

              throw new PlayException("Liquibase script should be applied, set " + applyUpdatesKey + "=true in application.conf", getScriptDescriptions(liqui.listUnrunChangeSets(ProductionContext)))
            }
            case _ => new PlayException("Liquibase script should be applied, set " + applyUpdatesKey + "=true in application.conf", getScriptDescriptions(liqui.listUnrunChangeSets(ProductionContext)))
          }
        })(app)
      }
    }
  }

  override lazy val enabled = {
    app.configuration.getConfig("db").isDefined && {
      !app.configuration.getString("liquibaseplugin").filter(_ == "disabled").isDefined
    }
  }
}
