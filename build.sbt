name := "migmongo"

organization := "com.synhaptein"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

resolvers ++= Seq("snapshots"           at "http://oss.sonatype.org/content/repositories/snapshots",
                  "releases"            at "http://oss.sonatype.org/content/repositories/releases",
                  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
                )
 
scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
	"org.mongodb" % "casbah_2.9.2" % "2.5.0",
	"org.slf4j" % "slf4j-api" % "1.7.2",
	"com.typesafe.akka" % "akka-actor" % "2.0.5"
)
