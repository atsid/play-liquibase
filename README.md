This is a simple play 2.2.x plugin for liquibase usage with Scala 2.10.x. and liquibase 3.2.2

Liquibase script files should at conf/liquibase/{dbName}/changelog.xml

Use contexts in your scripts to manage test and prod data and schema. Available contexts are:
 1. "test" - for test mode
 2. "dev" - for dev mode
 3. "prod" - for production mode

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

Add the plugin to your application.conf file:
```
applyLiquibase.{dbName}=true
```
