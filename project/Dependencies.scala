import sbt.*

object Dependencies {
  object V {
    val circe = "0.14.15"
    val sttp = "3.11.0"
    val scalaTest = "3.2.19"
    val catsEffect = "3.6.3"
    val cats = "2.13.0"
    val slf4j = "2.0.17"
    val logback = "1.5.22"
  }

  val circe = Seq(
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-parser" % V.circe
  )

  val cats = Seq(
    "org.typelevel" %% "cats-core" % V.cats,
    "org.typelevel" %% "cats-effect" % V.catsEffect
  )

  val http = Seq(
    "com.softwaremill.sttp.client3" %% "core" % V.sttp,
    "com.softwaremill.sttp.client3" %% "circe" % V.sttp,
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % V.sttp
  )

  val logging = Seq(
    "org.slf4j" % "slf4j-api" % V.slf4j,
    "ch.qos.logback" % "logback-classic" % V.logback
  )

  val testDependencies = Seq(
    "org.scalactic" %% "scalactic" % V.scalaTest,
    "org.scalatest" %% "scalatest" % V.scalaTest % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.7.0" % Test
  )

}
