package examples

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*
import strava.StravaClient
import strava.core.StravaConfig

import java.io.File
import java.time.ZonedDateTime

/**
 * Basic example demonstrating common Strava API operations
 * with comprehensive error handling and logging
 */
object BasicExample extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success).handleErrorWith: error =>
      IO.println(s"Fatal error: ${error.getMessage}").as(ExitCode.Error)

  def program: IO[Unit] =
    // Configure the client from environment variables
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )

    // Validate configuration
    validateConfig(config).flatMap: _ =>
      val tokenFile = File("strava-token.json")

      // Use the client with automatic resource management
      StravaClient.resource[IO](config, tokenFile).use: client =>
        for
          _ <- IO.println("=== Strava API Client Example ===\n")

          // Get current athlete
          _ <- IO.println("Fetching athlete information...")
          athleteResult <- client.athletes.getLoggedInAthlete()
          _ <- athleteResult match
            case Right(athlete) =>
              IO.println(s"Logged in as: ${athlete.firstname.getOrElse("Unknown")} ${athlete.lastname.getOrElse("")}") >>
              IO.println(s"  Location: ${athlete.city.getOrElse("Unknown")}, ${athlete.country.getOrElse("Unknown")}") >>
              IO.println(s"  Premium: ${athlete.premium.getOrElse(false)}\n")
            case Left(error) =>
              IO.println(s"Failed to get athlete: ${error.message}\n")

          // Get recent activities
          _ <- IO.println("Fetching recent activities...")
          activitiesResult <- client.activities.getLoggedInAthleteActivities(perPage = 5)
          _ <- activitiesResult match
            case Right(activities) =>
              IO.println(s"Found ${activities.size} recent activities:") >>
              activities.zipWithIndex.traverse_ { case (activity, idx) =>
                val name = activity.name.getOrElse("Unnamed")
                val distance = activity.distance.map(d => f"${d / 1000}%.2f km").getOrElse("N/A")
                val time = activity.moving_time.map(t => f"${t / 60}%d min").getOrElse("N/A")
                IO.println(s"  ${idx + 1}. $name - $distance in $time")
              } >> IO.println("")
            case Left(error) =>
              IO.println(s"Failed to get activities: ${error.message}\n")

          // Get activities from last 7 days
          _ <- IO.println("Fetching activities from last 7 days...")
          weekActivitiesResult <- client.activities.getActivitiesByDateRange(
            from = ZonedDateTime.now().minusDays(7),
            to = ZonedDateTime.now()
          )
          _ <- weekActivitiesResult match
            case Right(activities) =>
              val totalDistance = activities.flatMap(_.distance).sum / 1000
              val totalTime = activities.flatMap(_.moving_time).sum / 3600.0
              IO.println(s"Last 7 days summary:") >>
              IO.println(f"  Activities: ${activities.size}") >>
              IO.println(f"  Total distance: $totalDistance%.2f km") >>
              IO.println(f"  Total time: $totalTime%.2f hours\n")
            case Left(error) =>
              IO.println(s"Failed to get week activities: ${error.message}\n")

          // Get athlete stats
          _ <- IO.println("Fetching athlete statistics...")
          _ <- athleteResult match
            case Right(athlete) =>
              athlete.id.map: id =>
                client.athletes.getStats(id).flatMap:
                  case Right(_) =>
                    IO.println(s"All-time statistics:") >>
                    IO.println(s"  Stats retrieved successfully\n")
                  case Left(error) =>
                    IO.println(s"Failed to get stats: ${error.message}\n")
              .getOrElse(IO.println("Athlete ID not available\n"))
            case Left(_) =>
              IO.println("Cannot fetch stats without athlete info\n")

          // Example: Get details of first activity
          _ <- activitiesResult match
            case Right(activities) if activities.nonEmpty =>
              activities.headOption.flatMap(_.id) match
                case Some(activityId) =>
                  IO.println(s"Fetching details for activity $activityId...") >>
                  client.activities.getActivityById(activityId).flatMap:
                    case Right(detailed) =>
                      IO.println(s"Activity details:") >>
                      IO.println(s"  Name: ${detailed.name.getOrElse("Unknown")}") >>
                      IO.println(s"  Type: ${detailed.`type`.map(_.toString).getOrElse("Unknown")}") >>
                      IO.println(s"  Calories: ${detailed.calories.map(_.toString).getOrElse("N/A")}") >>
                      IO.println(s"  Kudos: ${detailed.kudos_count.getOrElse(0)}") >>
                      IO.println(s"  Comments: ${detailed.comment_count.getOrElse(0)}\n")
                    case Left(error) =>
                      IO.println(s"Failed to get activity details: ${error.message}\n")
                case None =>
                  IO.println("No activity ID available\n")
            case _ =>
              IO.println("No activities to show details for\n")

          _ <- IO.println("=== Example completed ===")
        yield ()

  // Helper methods for better error handling

  private def validateConfig(config: StravaConfig): IO[Unit] =
    if config.clientId == "your-client-id" || config.clientSecret == "your-client-secret" then
      IO.println("Warning: Using placeholder credentials. Set STRAVA_CLIENT_ID and STRAVA_CLIENT_SECRET environment variables.") >>
      IO.raiseError(IllegalArgumentException("Invalid Strava credentials"))
    else IO.println("Configuration validated\n")
