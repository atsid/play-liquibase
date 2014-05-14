import play.Project._
import bintray.Plugin._
import bintray.Keys._

name := "play-liquibase-plugin"

organization := "com.tjelp"

version := "1.2"

sbtPlugin := false

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(javaJdbc)

libraryDependencies += "org.liquibase" % "liquibase-core" % "3.1.1"

bintraySettings

publishMavenStyle := true

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

scalacOptions := Seq("-feature", "-deprecation")

playScalaSettings

repository in bintray := "maven"

bintrayOrganization in bintray := None
