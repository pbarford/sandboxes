

name := "spark-sandbox"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.0",
  "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "org.elasticsearch" % "elasticsearch" % "2.3.4",
  "org.spire-math" %% "algebra" % "0.3.1",
  "org.apache.spark" %% "spark-core" % "2.0.0",
  "org.apache.spark" %% "spark-streaming" % "2.0.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-8" % "2.0.0"
)

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

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
//addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)