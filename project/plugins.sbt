resolvers ++= Seq(
        Classpaths.typesafeResolver
)

resolvers += Resolver.url("bintray-sbt-plugin-releases", url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.3")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")
