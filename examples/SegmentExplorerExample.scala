package examples

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File

/**
 * Explore and discover popular segments in your area.
 * Find challenging climbs, fast sprints, and see your starred segments.
 */
object SegmentExplorerExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )
    val tokenFile = new File("strava-token.json")

    StravaClient.resource[IO](config, tokenFile).use { client =>
      for {
        _ <- IO.println("=== Segment Explorer ===\n")
        
        // Get your starred segments
        _ <- IO.println("Your Starred Segments:\n")
        starredResult <- client.segments.getLoggedInAthleteStarredSegments(perPage = 20)
        
        _ <- starredResult match {
          case Right(segments) if segments.nonEmpty =>
            segments.take(10).zipWithIndex.traverse_ { case (segment, idx) =>
              val name = segment.name.getOrElse("Unnamed")
              val distance = f"${segment.distance.getOrElse(0f) / 1000}%.2f km"
              val elevation = f"${segment.elevation_high.getOrElse(0f) - segment.elevation_low.getOrElse(0f)}%.0f m"
              val avgGrade = segment.average_grade.map(g => f"${g}%.1f%%").getOrElse("N/A")
              
              IO.println(f"${idx + 1}%2d. $name") >>
              IO.println(f"    Distance: $distance, Elevation: $elevation, Grade: $avgGrade")
            } >>
            IO.println("")
            
          case Right(_) =>
            IO.println("  No starred segments yet. Star some segments to see them here!\n")
            
          case Left(error) =>
            IO.println(s"  Error: ${error.message}\n")
        }
        
        // Example: Explore segments in a specific area (San Francisco)
        _ <- IO.println("🗺️  Exploring Segments (Example: San Francisco):\n")
        _ <- IO.println("   Note: Change the bounds parameter to explore your own area")
        _ <- IO.println("   Get bounds from: https://boundingbox.klokantech.com/\n")
        
        exploreResult <- client.segments.exploreSegments(
          bounds = (37.75, -122.45, 37.80, -122.40), // SF coordinates
          activityType = Some("running"),
          minCat = Some(0),
          maxCat = Some(5)
        )
        
        _ <- exploreResult match {
          case Right(explore) =>
            explore.segments.take(5).zipWithIndex.traverse_ { case (segment, idx) =>
              val name = segment.name.getOrElse("Unnamed")
              val distance = f"${segment.distance.getOrElse(0f) / 1000}%.2f km"
              val avgGrade = segment.avg_grade.map(g => f"${g}%.1f%%").getOrElse("N/A")
              val climbCategory = segment.climb_category.map(_.toString).getOrElse("0")
              
              IO.println(f"${idx + 1}%2d. $name") >>
              IO.println(f"    Distance: $distance, Grade: $avgGrade, Category: $climbCategory")
            } >>
            IO.println("")
            
          case Left(error) =>
            IO.println(s"  Error: ${error.message}\n")
        }
        
        // Get details of first starred segment
        _ <- starredResult match {
          case Right(segments) if segments.nonEmpty =>
            segments.headOption.flatMap(_.id).traverse_ { segmentId =>
              for {
                _ <- IO.println(s"Segment Details (ID: $segmentId):\n")
                detailsResult <- client.segments.getSegmentById(segmentId)
                
                _ <- detailsResult match {
                  case Right(segment) =>
                    IO.println(s"  Name: ${segment.name.getOrElse("Unnamed")}") >>
                    IO.println(s"  Distance: ${segment.distance.map(d => f"${d / 1000}%.2f km").getOrElse("N/A")}") >>
                    IO.println(s"  Average grade: ${segment.average_grade.map(g => f"$g%.1f%%").getOrElse("N/A")}") >>
                    IO.println(s"  Maximum grade: ${segment.maximum_grade.map(g => f"$g%.1f%%").getOrElse("N/A")}") >>
                    IO.println(s"  Elevation gain: ${segment.total_elevation_gain.map(e => f"$e%.0f m").getOrElse("N/A")}") >>
                    IO.println(s"  Athlete count: ${segment.athlete_count.getOrElse(0)}") >>
                    IO.println(s"  Effort count: ${segment.effort_count.getOrElse(0)}")
                    
                  case Left(error) =>
                    IO.println(s"  Error: ${error.message}")
                }
              } yield ()
            }
          case _ => IO.unit
        }
        
        _ <- IO.println("")
        _ <- IO.println("Tips:")
        _ <- IO.println("  • Use exploreSegments to find new challenges in your area")
        _ <- IO.println("  • Star segments to track your progress on them")
        _ <- IO.println("  • Check segment leaderboards to see how you compare")
        
      } yield ()
    }.as(ExitCode.Success)
  }
}

