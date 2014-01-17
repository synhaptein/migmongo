name := "migmongo"

organization := "com.synhaptein"

version := "2.1.2-SNAPSHOT"

scalaVersion := "2.10.3"

resolvers ++= Seq("Sonatype repository snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "Sonatype repository releases"  at "http://oss.sonatype.org/content/repositories/releases",
                  "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
                  "Typesafe repository releases"  at "http://repo.typesafe.com/typesafe/releases/"
                )
 
scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
	"org.mongodb" %% "casbah" % "2.6.1" pomOnly(),
	"com.typesafe.akka" %% "akka-actor" % "2.2.3",
	"org.slf4j" % "slf4j-api" % "1.7.2",
	"org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/synhaptein/migmongo</url>
  <licenses>
      <license>
          <name>The Apache Software License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
      </license>
  </licenses>
  <scm>
    <url>git@github.com:synhaptein/migmongo.git</url>
    <connection>scm:git:git@github.com:synhaptein/migmongo.git</connection>
  </scm>
  <developers>
      <developer>
          <id>synhaptein</id>
          <name>Philippe L'Heureux</name>
          <email>philippe.lheureux@umontreal.ca</email>
          <url>http://www.syhaptein.com</url>
      </developer>
  </developers>
)
