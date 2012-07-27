name := "json-compare"

organization := "com.lunatech"

version := "0.2-SNAPSHOT"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "play" %% "play" % "2.0",
  "org.specs2" %% "specs2" % "1.11" % "test")

publishTo <<= version { (v: String) => 
  val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
  Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
}
