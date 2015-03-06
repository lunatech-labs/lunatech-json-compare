name := "json-compare"

organization := "com.lunatech"

version := "1.2"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", scalaVersion.value)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.3.8" % "provided",
  "org.qirx" %% "little-spec" % "0.4" % "test"
)

publishTo <<= version { (v: String) => 
  val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
  Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
}

testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")