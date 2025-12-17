package examples

import cats.effect.{ExitCode, IO, IOApp}
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Generate monthly statistics and compare with previous months.
 * Perfect for tracking your progress over time.
 */
object MonthlyStatsExample extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )
    val tokenFile = File("strava-token.json")

    StravaClient.resource[IO](config, tokenFile).use: client =>
      for
        _ <- IO.println("=== Monthly Statistics Report ===\n")
        
        // Get current month
        currentMonth <- getMonthStats(client, ZonedDateTime.now())
        
        // Get previous month
        previousMonth <- getMonthStats(client, ZonedDateTime.now().minusMonths(1))
        
        // Display current month
        _ <- IO.println(s"${currentMonth.monthName}:\n")
        _ <- displayMonthStats(currentMonth)
        _ <- IO.println("")
        
        // Display previous month
        _ <- IO.println(s"${previousMonth.monthName}:\n")
        _ <- displayMonthStats(previousMonth)
        _ <- IO.println("")
        
        // Compare months
        _ <- IO.println("Month-over-month comparison:\n")
        _ <- compareMonths(currentMonth, previousMonth)
        
      yield ()
    .as(ExitCode.Success)

  case class MonthStats(
    monthName: String,
    activityCount: Int,
    totalDistance: Double,
    totalTime: Double,
    totalElevation: Double
  )

  private def getMonthStats(client: strava.StravaClient[IO], date: ZonedDateTime): IO[MonthStats] =
    val startOfMonth = date.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
    val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)
    
    client.activities.getActivitiesByDateRange(
      from = startOfMonth,
      to = endOfMonth,
      perPage = 200
    ).map:
      case Right(activities) =>
        val monthName = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        val totalDistance = activities.flatMap(_.distance).sum / 1000.0 // km
        val totalTime = activities.flatMap(_.moving_time).sum / 3600.0 // hours
        val totalElevation = activities.flatMap(_.total_elevation_gain).map(_.toDouble).sum // meters
        
        MonthStats(monthName, activities.size, totalDistance, totalTime, totalElevation)
        
      case Left(_) =>
        MonthStats(date.format(DateTimeFormatter.ofPattern("MMMM yyyy")), 0, 0, 0, 0)

  private def displayMonthStats(stats: MonthStats): IO[Unit] =
    IO.println(f"  Activities: ${stats.activityCount}") >>
    IO.println(f"  Distance: ${stats.totalDistance}%.1f km") >>
    IO.println(f"  Time: ${stats.totalTime}%.1f hours") >>
    IO.println(f"  Elevation: ${stats.totalElevation}%.0f m")

  private def compareMonths(current: MonthStats, previous: MonthStats): IO[Unit] =
    def showChange(label: String, curr: Double, prev: Double, unit: String): IO[Unit] =
      if prev == 0 then IO.println(f"  $label: $curr%.1f $unit")
      else
        val change = ((curr - prev) / prev * 100)
        val arrow = if change > 0 then "↑" else if change < 0 then "↓" else "→"
        val changeStr = f"${math.abs(change)}%.1f%%"
        IO.println(f"  $label: $curr%.1f $unit ($arrow $changeStr)")
    
    showChange("Activities", current.activityCount.toDouble, previous.activityCount.toDouble, "activities") >>
    showChange("Distance", current.totalDistance, previous.totalDistance, "km") >>
    showChange("Time", current.totalTime, previous.totalTime, "hours") >>
    showChange("Elevation", current.totalElevation, previous.totalElevation, "m")
