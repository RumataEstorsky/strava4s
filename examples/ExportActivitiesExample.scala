package examples

import cats.effect.{ExitCode, IO, IOApp}
import strava.StravaClient
import strava.core.StravaConfig
import java.io.{File, PrintWriter}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Export your activities to CSV for analysis in Excel, Google Sheets, or other tools.
 * You can customize which fields to export and the date range.
 */
object ExportActivitiesExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )
    val tokenFile = new File("strava-token.json")

    StravaClient.resource[IO](config, tokenFile).use { client =>
      for {
        _ <- IO.println("=== Export Activities to CSV ===\n")
        
        // Get activities from last 6 months
        _ <- IO.println("Fetching activities from last 6 months...")
        activitiesResult <- client.activities.getActivitiesByDateRange(
          from = ZonedDateTime.now().minusMonths(6),
          to = ZonedDateTime.now(),
          perPage = 200
        )
        
        _ <- activitiesResult match {
          case Right(activities) if activities.nonEmpty =>
            for {
              _ <- IO.println(s"Found ${activities.size} activities\n")
              
              // Export to CSV
              outputFile = new File("strava_activities.csv")
              _ <- IO.println(s"Writing to ${outputFile.getAbsolutePath}...")
              
              _ <- IO.blocking {
                val writer = new PrintWriter(outputFile)
                try {
                  // CSV Header
                  writer.println("Date,Name,Distance (km),Time (min),Pace (min/km),Elevation (m),Kudos,Moving Time (min)")
                  
                  // Write each activity
                  activities.foreach { activity =>
                    val date = activity.start_date_local
                      .map(_.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                      .getOrElse("")
                    val name = activity.name.getOrElse("").replace(",", ";") // Escape commas
                    val distance = activity.distance.map(_ / 1000).getOrElse(0f)
                    val time = activity.elapsed_time.map(_ / 60.0).getOrElse(0.0)
                    val pace = if (distance > 0) (time / distance) else 0.0
                    val elevation = activity.total_elevation_gain.getOrElse(0f)
                    val kudos = activity.kudos_count.getOrElse(0)
                    val movingTime = activity.moving_time.map(_ / 60.0).getOrElse(0.0)
                    
                    writer.println(
                      s"$date,$name,${f"$distance%.2f"},${f"$time%.1f"},${f"$pace%.2f"}," +
                      s"${f"$elevation%.0f"},$kudos,${f"$movingTime%.1f"}"
                    )
                  }
                } finally {
                  writer.close()
                }
              }
              
              _ <- IO.println(s"Export complete!\n")
              
              // Show summary
              _ <- IO.println("Export Summary:")
              _ <- IO.println(s"  Total activities: ${activities.size}")
              _ <- IO.println(f"  Total distance: ${activities.flatMap(_.distance).sum / 1000}%.1f km")
              _ <- IO.println(f"  Total time: ${activities.flatMap(_.moving_time).sum / 3600.0}%.1f hours")
              _ <- IO.println(s"  Output file: ${outputFile.getAbsolutePath}\n")
              
              _ <- IO.println("You can now:")
              _ <- IO.println("  • Open the CSV in Excel or Google Sheets")
              _ <- IO.println("  • Create charts and pivot tables")
              _ <- IO.println("  • Analyze trends over time")
              _ <- IO.println("  • Import into other tools")
              
            } yield ()
            
          case Right(_) =>
            IO.println("No activities found in the last 6 months.")
            
          case Left(error) =>
            IO.println(s"Error: ${error.message}")
        }
      } yield ()
    }.as(ExitCode.Success)
  }
}
