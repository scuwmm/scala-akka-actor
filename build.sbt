name := "scala-akka-actor"

version in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.13.7"

val AkkaVersion = "2.6.14"

val akkaStack = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
)

libraryDependencies in ThisBuild ++= akkaStack

