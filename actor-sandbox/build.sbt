name := "actor-sandbox"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.7.19",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.4",
  "com.typesafe.akka" % "akka-cluster_2.11" % "2.3.4"
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")