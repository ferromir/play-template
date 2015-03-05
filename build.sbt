import ScoverageSbtPlugin._
import ScoverageSbtPlugin.ScoverageKeys._

name := "play-template"

scalaVersion := "2.11.4"

Defaults.itSettings

Revolver.settings

scalariformSettings

lazy val root = (project in file(".")) enablePlugins(PlayScala) configs(IntegrationTest)

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= {
  val playVersion = play.core.PlayVersion.current
  Seq(
    "com.typesafe.play" %% "play-test"     % playVersion % "test,it",
    "com.typesafe.play" %% "play-ws"       % playVersion % "it",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
    "org.specs2" %% "specs2-core" % "3.0" % "test",
    "org.specs2" %% "specs2-html" % "3.0" % "test"
  )
}

outputPath in assembly := file("target/assembly/" + name.value + ".jar")

test in assembly := {}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case "play/core/server/ServerWithStop.class" => MergeStrategy.first
    case s: String if (s contains "org/apache/commons/logging/") => MergeStrategy.first
    case s: String if (s contains ".conf") => MergeStrategy.concat
    case s: String if (s contains ".properties") => MergeStrategy.last
    case s: String if (s contains ".xml") => MergeStrategy.last
    case s: String if (s contains ".class") => MergeStrategy.last
    case s: String if (s contains ".txt") => MergeStrategy.last
    case x => old(x)
  }
}

(testOptions in Test) += Tests.Argument(TestFrameworks.Specs2, "html", "console")

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

(compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle

scapegoatIgnoredFiles := Seq(".*/routes_routing.scala", ".*/routes_reverseRouting.scala")

coverageMinimum := 100

coverageExcludedPackages := "<empty>;Reverse.*"

scalaSource in IntegrationTest <<= baseDirectory(_ / "it")

lazy val qa = TaskKey[Unit]("qa")

qa := clean.value

qa <<= qa dependsOn (test in IntegrationTest)

qa <<= qa dependsOn (test in Test)

qa <<= qa dependsOn coverage

qa <<= qa dependsOn clean

javaOptions in Revolver.reStart += "-Dconfig.file=conf/local.application.conf"

javaOptions in Test += "-Dconfig.file=conf/test.application.conf"
