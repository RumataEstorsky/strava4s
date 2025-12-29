import Dependencies.*

ThisBuild / scalaVersion     := "3.6.2"
ThisBuild / version          := "1.2.1"
ThisBuild / organization := "io.github.RumataEstorsky"
ThisBuild / organizationName := "Valerii Svechikhin"
ThisBuild / description := "Strava Client for Scala"
ThisBuild / licenses := List("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / homepage := Some(url("https://github.com/RumataEstorsky/strava4s"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/RumataEstorsky/strava4s"),
    "scm:git@github.com:RumataEstorsky/strava4s.git"
  )
)
ThisBuild / developers := List(
  Developer("RumataEstorsky", "Valerii Svechikhin", "1156209+RumataEstorsky@users.noreply.github.com", url("https://github.com/RumataEstorsky"))
)

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
  if (isSnapshot.value)
    Some("snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  else
    Some("releases"  at nexus)
}


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
