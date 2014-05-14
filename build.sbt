name := "play-liquibase"

organization := "play"

version := "1.2"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(javaJdbc)

libraryDependencies += "org.liquibase" % "liquibase-core" % "3.1.1"
