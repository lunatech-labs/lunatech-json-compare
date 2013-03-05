name := "json-compare"

organization := "com.lunatech"

version := "1.0"

scalaVersion := "2.10.0"

crossScalaVersions := Seq("2.9.1", "2.10.0")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies <++= (scalaVersion) { version => Seq(
  "play" %% "play" % (if(version == "2.9.1") "2.0" else "2.1.0"),
  "org.specs2" %% "specs2" % "1.12.3" % "test")
}

publishTo <<= version { (v: String) => 
  val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
  Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
}
