

// Import default settings. This changes `publishTo` settings to use the Sonatype repository and add several commands for publishing.
sonatypeSettings

name := "play-querydsl"

organization := "com.code-troopers.play"

version := "0.1.3"

sbtPlugin := true


resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

/// Dependencies

libraryDependencies ++= Seq(
  "com.querydsl" % "querydsl-apt" % "4.1.4"
)

pomExtra := (
  <url>https://github.com/CedricGatay/play-querydsl</url>
    <licenses>
      <license>
        <name>The Apache Software License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:CedricGatay/play-querydsl.git</url>
      <connection>scm:git:git@github.com:CedricGatay/play-querydsl.git</connection>
    </scm>
    <developers>
      <developer>
        <id>cgatay</id>
        <name>Cedric Gatay</name>
        <url>http://www.code-troopers.com</url>
      </developer>
    </developers>
  )
