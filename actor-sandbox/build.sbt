name := "actor-sandbox"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.7.19",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.3.4",
  "com.typesafe.akka" % "akka-cluster_2.10" % "2.3.4"
)
    