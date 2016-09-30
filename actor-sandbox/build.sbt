name := "actor-sandbox"

version := "1.0"

scalaVersion := "2.11.7"
val libraryVersion = "1.2.0"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.9.33",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.4",
  "com.typesafe.akka" % "akka-cluster_2.11" % "2.3.4",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.0",
  "com.mfglabs" %% "precepte-core" % "0.3.0",
  "com.mfglabs" %% "precepte-core-scalaz" % "0.3.0",
  "com.mfglabs" %% "precepte-logback" % "0.3.0",
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.0",
  "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "com.rabbitmq" % "amqp-client" % "3.1.3",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "io.argonaut" %% "argonaut" % "6.1",
  "org.elasticsearch" % "elasticsearch" % "2.3.4",
  "org.spire-math" %% "algebra" % "0.3.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.2",
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.16",
  "com.github.julien-truffaut"  %%  "monocle-core"    % libraryVersion,
  "com.github.julien-truffaut"  %%  "monocle-generic" % libraryVersion,
  "com.github.julien-truffaut"  %%  "monocle-macro"   % libraryVersion,
  "com.github.julien-truffaut"  %%  "monocle-state"   % libraryVersion,
  "com.github.julien-truffaut"  %%  "monocle-refined" % libraryVersion,
  "com.github.julien-truffaut"  %%  "monocle-law"     % libraryVersion % "test",
  "org.apache.storm" % "storm-core" % "1.0.2",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.apache.spark" %% "spark-core" % "2.0.0",
  "org.apache.spark" %% "spark-streaming" % "2.0.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-8" % "2.0.0"

)

resolvers ++= Seq(
  "Maven" at "http://repo.maven.apache.org/maven2/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "MfgLabs" at "http://dl.bintray.com/mfglabs/maven",
  "OSS" at "http://oss.sonatype.org/content/repositories/releases",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "clojars" at "http://clojars.org/repo/",
  "clojure-releases" at "http://build.clojure.org/releases",
  Resolver.sonatypeRepo("releases")
)


addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
//addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)