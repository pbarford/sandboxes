name := "actor-sandbox"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.9.33",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.4",
  "com.typesafe.akka" % "akka-cluster_2.11" % "2.3.4",
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.6",
  "com.mfglabs" %% "precepte-core" % "0.3.0",
  "com.mfglabs" %% "precepte-core-scalaz" % "0.3.0",
  "com.mfglabs" %% "precepte-logback" % "0.3.0",
  "org.scalaz" %% "scalaz-core" % "7.2.0",
  "org.scalaz" %% "scalaz-concurrent" % "7.2.0",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "com.rabbitmq" % "amqp-client" % "3.1.3",
  "org.elasticsearch" % "elasticsearch" % "2.3.1",
  "org.spire-math" %% "algebra" % "0.3.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.2"
)

resolvers ++= Seq(
  "Maven" at "http://repo.maven.apache.org/maven2/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "MfgLabs" at "http://dl.bintray.com/mfglabs/maven",
  "OSS" at "http://oss.sonatype.org/content/repositories/releases",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  Resolver.sonatypeRepo("releases")
)


addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")