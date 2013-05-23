import sbt._
import sbt.Keys._

object AconcaguaBuild extends Build {

  lazy val aconcagua = Project(
    id = "aconcagua",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Aconcagua",
      organization := "aconcagua",
      version := "0.1",
      scalaVersion := "2.10.1",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies += "com.typesafe.akka" %% "akka-kernel" % "2.1.2",
      libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.2",
      libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.1.2"
    )
  )
}
