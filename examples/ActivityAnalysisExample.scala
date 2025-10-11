package examples

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File
import java.time.ZonedDateTime

/**
 * Analyze your activities with statistics and insights.
 * This example shows you how to:
 *   - Get activities from a specific time period
 *   - Calculate distance, time, and pace statistics
 *   - Group activities by type
 *   - Find your longest/fastest activities
 */
object ActivityAnalysisExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )
    val tokenFile = new File("strava-token.json")

    StravaClient.resource[IO](config, tokenFile).use { client =>
      for {
        _ <- IO.println("=== Activity Analysis ===\n")
        
        // Get last 30 days of activities
        _ <- IO.println("Analyzing last 30 days...\n")
        activitiesResult <- client.activities.getActivitiesByDateRange(
          from = ZonedDateTime.now().minusDays(30),
          to = ZonedDateTime.now(),
          perPage = 200
        )
        
        _ <- activitiesResult match {
          case Right(activities) if activities.nonEmpty =>
            // Group by activity type
            val byType = activities.groupBy(a => a.`type`.map(_.toString).getOrElse("Unknown"))
            
            IO.println(s"Total activities: ${activities.size}\n") >>
            IO.println("Activity types:") >>
            byType.toList.traverse_ { case (activityType, acts) =>
              IO.println(s"  • $activityType: ${acts.size}")
            } >>
            IO.println("") >>
            
            // Calculate totals
            {
              val totalDistance = activities.flatMap(_.distance).sum / 1000 // km
              val totalTime = activities.flatMap(_.moving_time).sum / 3600.0 // hours
              val totalElevation = activities.flatMap(_.total_elevation_gain).sum // meters
              
              IO.println("Totals:") >>
              IO.println(f"  Distance: $totalDistance%.1f km") >>
              IO.println(f"  Time: $totalTime%.1f hours") >>
              IO.println(f"  Elevation gain: $totalElevation%.0f m") >>
              IO.println("")
            } >>
            
            // Find longest activity
            activities.maxByOption(_.distance.getOrElse(0f)).traverse_ { longest =>
              IO.println("Longest activity:") >>
              IO.println(s"  ${longest.name.getOrElse("Unnamed")}") >>
              IO.println(f"  ${longest.distance.map(_ / 1000).getOrElse(0f)}%.2f km") >>
              IO.println("")
            } >>
            
            // Find fastest average speed
            activities.maxByOption(_.average_speed.getOrElse(0f)).traverse_ { fastest =>
              val speedKmh = fastest.average_speed.map(_ * 3.6).getOrElse(0f)
              IO.println("Fastest average speed:") >>
              IO.println(s"  ${fastest.name.getOrElse("Unnamed")}") >>
              IO.println(f"  $speedKmh%.1f km/h") >>
              IO.println("")
            } >>
            
            // Recent activities table
            IO.println("Recent activities:") >>
            IO.println("┌─────────────────────────────────┬──────────┬──────────┬────────┐") >>
            IO.println("│ Name                            │ Distance │ Time     │ Type   │") >>
            IO.println("├─────────────────────────────────┼──────────┼──────────┼────────┤") >>
            activities.take(10).traverse_ { activity =>
              val name = activity.name.getOrElse("Unnamed").take(30).padTo(31, ' ')
              val distance = f"${activity.distance.map(_ / 1000).getOrElse(0f)}%.1f km".padTo(8, ' ')
              val time = formatDuration(activity.moving_time.getOrElse(0)).padTo(8, ' ')
              val actType = activity.`type`.map(_.toString).getOrElse("?").take(6).padTo(6, ' ')
              IO.println(s"│ $name │ $distance │ $time │ $actType │")
            } >>
            IO.println("└─────────────────────────────────┴──────────┴──────────┴────────┘")
            
          case Right(_) =>
            IO.println("No activities found in the last 30 days.")
            
          case Left(error) =>
            IO.println(s"Error: ${error.message}")
        }
      } yield ()
    }.as(ExitCode.Success)
  }

  private def formatDuration(seconds: Int): String = {
    val hours = seconds / 3600
    val mins = (seconds % 3600) / 60
    if (hours > 0) f"${hours}h${mins}%02dm" else f"${mins}m"
  }
}

