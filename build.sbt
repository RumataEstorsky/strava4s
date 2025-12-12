import Dependencies.*

ThisBuild / scalaVersion     := "2.13.18"
ThisBuild / crossScalaVersions := Seq("2.13.18", "3.6.2")
ThisBuild / version          := "1.0.0"
ThisBuild / organization     := "valerii.svechikhin"
ThisBuild / organizationName := "ValeriiSvechikhin"

lazy val root = (project in file("."))
  .settings(
    name := "strava4s",
    libraryDependencies ++= circe ++ cats ++ http ++ logging ++ testDependencies,
    // Tests are only run for Scala 2.13.x due to compatibility issues with Scala 3.x
    Test / skip := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => true
        case _ => false
      }
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) =>
          Seq(
            "-encoding", "UTF-8",
            "-feature",
            "-language:existentials",
            "-language:higherKinds",
            "-language:implicitConversions",
            "-unchecked",
            "-Xlint",
            "-Ywarn-dead-code",
            "-Ywarn-numeric-widen",
            "-Ywarn-value-discard",
            "-deprecation"
          )
        case Some((3, _)) =>
          Seq(
            "-encoding", "UTF-8",
            "-feature",
            "-language:implicitConversions",
            "-unchecked",
            "-deprecation",
            "-Xkind-projector:underscores",
            "-Xmax-inlines", "64"
          )
        case _ =>
          Seq()
      }
    },
    // Scoverage settings
    coverageMinimumStmtTotal := 80,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    // Publishing settings
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomIncludeRepository := { _ => false },
    homepage := Some(url("https://github.com/vsvechikhin/strava4s")),
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/vsvechikhin/strava4s"),
        "scm:git@github.com:RumataEstorsky/strava4s.git"
      )
    ),
    developers := List(
      Developer(
        id = "vsvechikhin",
        name = "Valerii Svechikhin",
        email = "",
        url = url("https://github.com/vsvechikhin")
      )
    )
  )

