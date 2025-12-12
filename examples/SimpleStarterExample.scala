package examples

import cats.effect.{ExitCode, IO, IOApp}
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File

/**
 * The simplest possible example to get you started with the Strava API.
 * This example shows you how to connect and fetch your basic information.
 * 
 * Setup:
 *   1. Set environment variables: STRAVA_CLIENT_ID and STRAVA_CLIENT_SECRET
 *   2. Run AuthenticationExample.scala first to get your token
 *   3. Run this example
 */
object SimpleStarterExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    // 1. Configure with your credentials (from environment variables)
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )

    // 2. Point to your token file (created by AuthenticationExample)
    val tokenFile = new File("strava-token.json")

    // 3. Use the client
    StravaClient.resource[IO](config, tokenFile).use { client =>
      for {
        // Get your athlete info
        result <- client.athletes.getLoggedInAthlete()
        
        _ <- result match {
          case Right(athlete) =>
            IO.println(s"Hello, ${athlete.firstname.getOrElse("Athlete")}!") >>
            IO.println(s"You're from ${athlete.city.getOrElse("Unknown")}, ${athlete.country.getOrElse("Unknown")}")
            
          case Left(error) =>
            IO.println(s"Error: ${error.message}") >>
            IO.println("Tip: Run AuthenticationExample first to get your token")
        }
      } yield ()
    }.as(ExitCode.Success)
  }
}
