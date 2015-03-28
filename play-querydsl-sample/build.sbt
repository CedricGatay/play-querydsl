name := """play-querydsl-sample"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala, QueryDSLPlugin)

queryDSLPackage := "models/included"

//logLevel in Compile := Level.Debug

libraryDependencies ++= Seq(
  jdbc,
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final",
  cache,
  ws
)
