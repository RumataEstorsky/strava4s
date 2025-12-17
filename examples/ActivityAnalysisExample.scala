package examples

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File
import java.time.ZonedDateTime

/**
 * Analyze your activities with statistics and insights.
 * This example shows you how to:
 *   - Get activities from a specific time period
 *   - Calculate distance, time, and pace statistics
 *   - Find your longest/fastest activities
 */
object ActivityAnalysisExample extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )
    val tokenFile = File("strava-token.json")

    StravaClient.resource[IO](config, tokenFile).use: client =>
      for
        _ <- IO.println("=== Activity Analysis ===\n")
        
        // Get last 30 days of activities
        _ <- IO.println("Analyzing last 30 days...\n")
        activitiesResult <- client.activities.getActivitiesByDateRange(
          from = ZonedDateTime.now().minusDays(30),
          to = ZonedDateTime.now(),
          perPage = 200
        )
        
        _ <- activitiesResult match
          case Right(activities) if activities.nonEmpty =>
            for
              _ <- IO.println(s"Total activities: ${activities.size}\n")
              
              // Calculate totals
              totalDistance = activities.flatMap(_.distance).sum / 1000 // km
              totalTime = activities.flatMap(_.moving_time).sum / 3600.0 // hours
              totalElevation = activities.flatMap(_.total_elevation_gain).sum // meters
              
              _ <- IO.println("Totals:")
              _ <- IO.println(f"  Distance: $totalDistance%.1f km")
              _ <- IO.println(f"  Time: $totalTime%.1f hours")
              _ <- IO.println(f"  Elevation gain: $totalElevation%.0f m")
              _ <- IO.println("")
              
              // Find longest activity
              _ <- activities.maxByOption(_.distance.getOrElse(0f)) match
                case Some(longest) =>
                  IO.println("Longest activity:") >>
                  IO.println(s"  ${longest.name.getOrElse("Unnamed")}") >>
                  IO.println(f"  ${longest.distance.map(_ / 1000).getOrElse(0f)}%.2f km") >>
                  IO.println("")
                case None => IO.unit
              
              // Find fastest average speed
              _ <- activities.maxByOption(_.average_speed.getOrElse(0f)) match
                case Some(fastest) =>
                  val speedKmh: Double = fastest.average_speed.map(_.toDouble * 3.6).getOrElse(0.0)
                  IO.println("Fastest average speed:") >>
                  IO.println(s"  ${fastest.name.getOrElse("Unnamed")}") >>
                  IO.println(f"  $speedKmh%.1f km/h") >>
                  IO.println("")
                case None => IO.unit
              
              // Recent activities table
              _ <- IO.println("Recent activities:")
              _ <- IO.println("┌─────────────────────────────────┬──────────┬──────────┐")
              _ <- IO.println("│ Name                            │ Distance │ Time     │")
              _ <- IO.println("├─────────────────────────────────┼──────────┼──────────┤")
              _ <- activities.take(10).traverse_ { activity =>
                val name = activity.name.getOrElse("Unnamed").take(30).padTo(31, ' ')
                val distance = f"${activity.distance.map(_ / 1000).getOrElse(0f)}%.1f km".padTo(8, ' ')
                val time = formatDuration(activity.moving_time.getOrElse(0)).padTo(8, ' ')
                IO.println(s"│ $name │ $distance │ $time │")
              }
              _ <- IO.println("└─────────────────────────────────┴──────────┴──────────┘")
            yield ()
            
          case Right(_) =>
            IO.println("No activities found in the last 30 days.")
            
          case Left(error) =>
            IO.println(s"Error: ${error.message}")
      yield ()
    .as(ExitCode.Success)

  private def formatDuration(seconds: Int): String =
    val hours = seconds / 3600
    val mins = (seconds % 3600) / 60
    if hours > 0 then f"${hours}h${mins}%02dm" else f"${mins}m"
