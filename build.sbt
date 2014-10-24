import com.typesafe.sbt.web.SbtWeb.autoImport._
import com.typesafe.sbt.less.Import.LessKeys
import play.PlayScala

name := "json-schema-ui"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

scalacOptions ++= Seq(
  "-feature",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(jdbc, anorm, cache, ws)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23" withSources(),
  "org.mongodb" %% "casbah" % "2.7.2",
  "org.apache.httpcomponents" % "httpclient" % "4.3.3",
  "com.github.athieriot" %% "specs2-embedmongo" % "0.7.0" withSources(),
  "com.github.detro.ghostdriver" % "phantomjsdriver" % "1.1.0" % "test" withSources(),
  "com.github.fge" % "json-schema-validator" % "2.2.5" withSources()
)

pipelineStages := Seq(rjs, digest, gzip)

includeFilter in (Assets, LessKeys.less) := "*.less"

// For minified *.min.css files
// LessKeys.compress := true