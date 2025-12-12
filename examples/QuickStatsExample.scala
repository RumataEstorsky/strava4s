package examples

import cats.effect.{ExitCode, IO, IOApp}
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File

/**
 * Get quick statistics overview - perfect for a daily dashboard.
 * Shows your recent week, current month, and all-time stats at a glance.
 */
object QuickStatsExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )
    val tokenFile = new File("strava-token.json")

    StravaClient.resource[IO](config, tokenFile).use { client =>
      for {
        // Get athlete info
        athleteResult <- client.athletes.getLoggedInAthlete()
        
        _ <- athleteResult match {
          case Right(athlete) =>
            val name = s"${athlete.firstname.getOrElse("Athlete")} ${athlete.lastname.getOrElse("")}"
            
            for {
              _ <- IO.println("=" * 60)
              _ <- IO.println(s"Strava Dashboard - $name")
              _ <- IO.println("=" * 60)
              _ <- IO.println("")
              
              // Get all-time stats
              _ <- athlete.id match {
                case Some(athleteId) =>
                  client.athletes.getStats(athleteId).flatMap {
                    case Right(stats) =>
                      for {
                        _ <- IO.println("All-Time Stats:")
                        _ <- IO.println("")
                        
                        // Recent run totals
                        _ <- stats.recent_run_totals match {
                          case Some(runTotals) =>
                            IO.println("Recent Run Totals:") >>
                            IO.println(f"  Count: ${runTotals.count.getOrElse(0)}") >>
                            IO.println(f"  Distance: ${runTotals.distance.map(_ / 1000).getOrElse(0f)}%.1f km") >>
                            IO.println(f"  Time: ${runTotals.moving_time.map(_ / 3600.0).getOrElse(0.0)}%.1f hours") >>
                            IO.println(f"  Elevation: ${runTotals.elevation_gain.getOrElse(0f)}%.0f m") >>
                            IO.println("")
                          case None => IO.unit
                        }
                        
                        // Recent ride totals
                        _ <- stats.recent_ride_totals match {
                          case Some(rideTotals) =>
                            IO.println("Recent Ride Totals:") >>
                            IO.println(f"  Count: ${rideTotals.count.getOrElse(0)}") >>
                            IO.println(f"  Distance: ${rideTotals.distance.map(_ / 1000).getOrElse(0f)}%.1f km") >>
                            IO.println(f"  Time: ${rideTotals.moving_time.map(_ / 3600.0).getOrElse(0.0)}%.1f hours") >>
                            IO.println(f"  Elevation: ${rideTotals.elevation_gain.getOrElse(0f)}%.0f m") >>
                            IO.println("")
                          case None => IO.unit
                        }
                        
                        // Year-to-date run totals
                        _ <- stats.ytd_run_totals match {
                          case Some(runTotals) =>
                            IO.println("Year-to-Date Run Totals:") >>
                            IO.println(f"  Count: ${runTotals.count.getOrElse(0)}") >>
                            IO.println(f"  Distance: ${runTotals.distance.map(_ / 1000).getOrElse(0f)}%.1f km") >>
                            IO.println(f"  Time: ${runTotals.moving_time.map(_ / 3600.0).getOrElse(0.0)}%.1f hours") >>
                            IO.println("")
                          case None => IO.unit
                        }
                        
                        // All-time run totals
                        _ <- stats.all_run_totals match {
                          case Some(runTotals) =>
                            IO.println("All-Time Run Totals:") >>
                            IO.println(f"  Count: ${runTotals.count.getOrElse(0)}") >>
                            IO.println(f"  Distance: ${runTotals.distance.map(_ / 1000).getOrElse(0f)}%.1f km") >>
                            IO.println(f"  Time: ${runTotals.moving_time.map(_ / 3600.0).getOrElse(0.0)}%.1f hours") >>
                            IO.println("")
                          case None => IO.unit
                        }
                        
                        _ <- IO.println("=" * 60)
                        _ <- IO.println("")
                        _ <- IO.println("Stats refreshed!")
                      } yield ()
                      
                    case Left(error) =>
                      IO.println(s"Error getting stats: ${error.message}")
                  }
                case None =>
                  IO.println("Could not get athlete ID")
              }
            } yield ()
            
          case Left(error) =>
            IO.println(s"Error: ${error.message}") >>
            IO.println("Tip: Run AuthenticationExample first")
        }
      } yield ()
    }.as(ExitCode.Success)
  }
}
