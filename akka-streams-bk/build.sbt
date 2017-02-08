name := "akka-streams"

version := "1.0"

scalaVersion := "2.11.7"
val libraryVersion = "1.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.11",
  "com.typesafe.akka" %% "akka-cluster" % "2.4.11",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.11",
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.19",
  "com.typesafe.akka" %% "akka-cluster-metrics" % "2.4.11",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.11",
  "com.typesafe.akka" %% "akka-stream" % "2.4.11",
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.0",
  "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.rabbitmq" % "amqp-client" % "3.1.3",
  "io.scalac" %% "reactive-rabbit" % "1.1.2",
  "org.json4s" %% "json4s-native" % "3.4.2",
  "net.liftweb" %% "lift-json" % "2.6.3"
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