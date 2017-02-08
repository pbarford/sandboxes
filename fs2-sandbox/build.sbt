name := "fs2-sandbox"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "0.9.1",
  "com.chuusai" %% "shapeless" % "2.3.2",
  "org.typelevel" %% "cats" % "0.8.1"
)