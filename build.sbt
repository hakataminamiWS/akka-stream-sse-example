name := """akka-stream-sse-example"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root =
  (project in file(".")).enablePlugins(PlayScala, PlayAkkaHttp2Support)
// lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

val akkaVersion = "2.6.15"

// Akka dependencies used by Play
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"           % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed"         % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed"          % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed"   % akkaVersion % Test
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
