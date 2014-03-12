name := "play-querydsl"

organization := "com.code-troopers.play"

version := "0.1.0"

sbtPlugin := true


resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

/// Dependencies

libraryDependencies ++= Seq(
  "com.mysema.querydsl" % "querydsl-apt" % "3.3.1",
  "org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final"
)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")
