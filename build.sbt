enablePlugins(JavaAppPackaging)

name := "punchlines"
organization := "mdulac"
version := "1.0"
scalaVersion := "2.12.2"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-http" % "10.0.5",
    "de.heikoseeberger" %% "akka-http-play-json" % "1.15.0",
    "org.scalacheck" %% "scalacheck" % "1.13.4",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )
}