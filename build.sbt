import Dependencies.*

ThisBuild / scalaVersion     := "3.6.2"
ThisBuild / version          := "1.2.0"
ThisBuild / organization     := "valerii.svechikhin"
ThisBuild / organizationName := "ValeriiSvechikhin"

lazy val root = (project in file("."))
  .settings(
    name := "strava4s",
    libraryDependencies ++= circe ++ cats ++ http ++ logging ++ testDependencies,
    // Include examples directory in source paths
    Compile / unmanagedSourceDirectories += baseDirectory.value / "examples",
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-feature",
      "-language:implicitConversions",
      "-unchecked",
      "-deprecation",
      "-Xkind-projector:underscores",
      "-Xmax-inlines", "64"
    ),
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
