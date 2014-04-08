name := "json-compare"

organization := "com.lunatech"

version := "1.1"

scalaVersion := "2.10.3"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.2.2" % "provided",
  "org.specs2" %% "specs2" % "1.12.3" % "test")

publishTo <<= version { (v: String) => 
  val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
  Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
}
