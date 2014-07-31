I prefer Liquibase DB evolutions to play 2.0 Evolutions mechanism
This is simple play 2.2.x plugin for liquibase usage with Scala 2.10.x.

Suppose main script files for all DBs used are at conf/liquibase/--dbName--/changelog.xml

Use contexts in your scripts to manage test and prod data and schema. Available contexts are:
 1. "test" - for test mode
 2. "dev" - for dev mode
 3. "prod" - for production mode

In dev and production mode use applyLiquibase.<dbName>=true properties to apply scripts.

To use:

Add the plugin to your 'play.plugins' file:
```
8000:play.api.plugins.LiquibasePlugin
```

Then in Build.scala instantiate the plugin:
```
val liquibasePlugin = RootProject(uri("https://github.com/mcaden/play-liquibase.git"))
```

And add it as a dependency to your main play project:
```
.dependsOn(liquibasePlugin)
```
