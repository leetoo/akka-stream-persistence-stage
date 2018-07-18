name := "akka-stream-persistence-stage"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++=  Seq(
  "com.lmax" % "disruptor" % "3.3.6",
  "org.apache.logging.log4j" % "log4j-core" % "2.8.2",
  "org.apache.logging.log4j" % "log4j-api" % "2.8.2",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.8.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.iq80.leveldb" % "leveldb" % "0.10",
  "org.iq80.leveldb" % "leveldb-api" % "0.10",
  "com.typesafe.akka" %% "akka-actor" % "2.5.14",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.14",
  "com.typesafe.akka" %% "akka-stream" % "2.5.14"
)