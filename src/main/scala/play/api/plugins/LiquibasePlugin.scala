package play.api.plugins

import java.sql.Connection

import liquibase.Liquibase
import scala.collection.JavaConversions._
import liquibase.changelog.ChangeSet
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.{ResourceAccessor, CompositeResourceAccessor, ClassLoaderResourceAccessor, FileSystemResourceAccessor}
import play.api._
import play.api.db.{DB, DBPlugin}

/**
 * The ApplyLiquibase Play Plugin
 */
class LiquibasePlugin(app: Application) extends Plugin {
  val TestContext = "test"
  val DeveloperContext = "dev"
  val ProductionContext = "prod"

  override lazy val enabled = {
    isDatabaseConfigured && !isPluginDisabled
  }

  override def onStart() {
    val api = app.plugin[DBPlugin].map(_.api).getOrElse(throw new Exception("there should be a database plugin registered at this point but looks like it's not available, so liquibase won't work. Please make sure you register a db plugin properly"))
    api.datasources.foreach {
      case (ds, dbName) => {
        DB.withConnection(dbName)(connection => { migrateDatabase(dbName, connection) })(app)
      }
    }
  }

  def migrateDatabase(dbName: String, connection: Connection) {
    val context = liquibaseContextName(dbName)
    val liqui = makeLiquibase(dbName, connection)

    if (isUpdateEnabled(dbName)) {
      liqui.update(context)
    } else if (ProductionContext.equals(context)) {
      emitUpdateRequiredError(dbName, liqui.listUnrunChangeSets(ProductionContext))
    }
  }

  def makeLiquibase(dbName: String, connection: Connection): Liquibase = {
    new Liquibase(changeLogPath(dbName), resourceAccessor, new JdbcConnection(connection))
  }

  def emitUpdateRequiredError(dbName: String,  unrunChangesets: java.util.List[ChangeSet]) {
    val configKey = applyUpdatesKey(dbName)
    val unrunDescription = getScriptDescriptions(unrunChangesets)
    Logger("play").warn("Your production database [" + dbName + "] needs Liquibase updates! \n\n" + unrunDescription)
    Logger("play").warn("Run with -DapplyLiquibase." + dbName + "=true if you want to run them automatically (be careful)")
    throw new PlayException("Liquibase script should be applied, set " + configKey + "=true in application.conf", unrunDescription)
  }

  private def changeLogPath (dbName:String): String = {
    app.configuration.getString(changelogPathKey(dbName)).orElse(Some(defaultChangelogPath(dbName))).get
  }

  private def liquibaseContextName (dbName:String): String = {
    val contextConfigKey = contextNameKey(dbName)
    val appModeContext = app.mode match {
      case Mode.Test => TestContext
      case Mode.Dev  => DeveloperContext
      case Mode.Prod => ProductionContext
      case _ => ProductionContext
    }
    app.configuration.getString(contextConfigKey).orElse(Some(appModeContext)).get
  }

  private def resourceAccessor: ResourceAccessor = {
    new CompositeResourceAccessor(
      new FileSystemResourceAccessor(app.path.getAbsolutePath),
      new ClassLoaderResourceAccessor()
    )
  }

  def isDatabaseConfigured: Boolean = {
    app.configuration.getConfig("db").isDefined
  }

  def isPluginDisabled: Boolean = {
    app.configuration.getString("liquibaseplugin").filter(_ == "disabled").isDefined
  }

  private def isUpdateEnabled (dbName: String): Boolean = {
    app.configuration.getBoolean(applyUpdatesKey(dbName)).filter(_ == true).isDefined
  }

  def defaultChangelogPath(dbName: String): String = {
    "conf/liquibase/" + dbName + "/changelog.xml"
  }

  def changelogPathKey(dbName: String): String = {
    "applyLiquibase." + dbName + ".changelogPath"
  }

  def contextNameKey(dbName: String): String = {
    "applyLiquibase." + dbName + ".context"
  }

  private def applyUpdatesKey (dbName: String): String = {
    "applyLiquibase." + dbName + ".apply"
  }

  private def getScriptDescriptions(changeSets: Seq[ChangeSet]): String = {
    changeSets.zipWithIndex.map {
      case (cl, num) =>
        "" + num + ". " + cl.getId +
          Option(cl.getDescription).map(" (" + _ + ")").getOrElse("") +
          " by " + cl.getAuthor
    }.mkString("\n")
  }
}
