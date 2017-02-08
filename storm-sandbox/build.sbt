name := "storm-sandbox"

version := "1.0"

scalaVersion := "2.11.7"
val libraryVersion = "1.2.0"

libraryDependencies ++= Seq(
  "org.apache.storm" % "storm-core" % "1.0.2",
  "com.rabbitmq" % "amqp-client" % "3.1.3",
  "org.json4s" %% "json4s-native" % "3.4.2"
).map(_.excludeAll(ExclusionRule("slf4j-log4j12")))

resolvers ++= Seq(
  "Maven" at "http://http://central.maven.org/maven2/",
  "MavenR" at "http://http://repo.maven.org/maven2/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "MfgLabs" at "http://dl.bintray.com/mfglabs/maven",
  "OSS" at "http://oss.sonatype.org/content/repositories/releases",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "clojars" at "http://clojars.org/repo/",
  "clojure-releases" at "http://build.clojure.org/releases",
  Resolver.sonatypeRepo("releases")
)