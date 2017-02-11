name := "akka-streams-sandbox"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.11",
  "com.rabbitmq" % "amqp-client" % "3.6.6"
)

resolvers ++= Seq(
  "Maven" at "http://http://central.maven.org/maven2/",
  "MavenR" at "http://http://repo.maven.org/maven2/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "MfgLabs" at "http://dl.bintray.com/mfglabs/maven",
  "OSS" at "http://oss.sonatype.org/content/repositories/releases",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "clojars" at "http://clojars.org/repo/",
  "confluent" at "http://packages.confluent.io/maven/",
  "clojure-releases" at "http://build.clojure.org/releases",
  Resolver.sonatypeRepo("releases")
)