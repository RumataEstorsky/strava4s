# Quick Reference

## Setup

```bash
export STRAVA_CLIENT_ID='your-client-id'
export STRAVA_CLIENT_SECRET='your-client-secret'
sbt "runMain examples.AuthenticationExample"
```

## Examples

| Command | Description |
|---------|-------------|
| `sbt "runMain examples.SimpleStarterExample"` | Hello World |
| `sbt "runMain examples.QuickStatsExample"` | Dashboard |
| `sbt "runMain examples.ActivityAnalysisExample"` | 30 days analysis |
| `sbt "runMain examples.MonthlyStatsExample"` | Monthly comparison |
| `sbt "runMain examples.ExportActivitiesExample"` | Export to CSV |
| `sbt "runMain examples.SegmentExplorerExample"` | Find segments |
| `sbt "runMain examples.ClubActivitiesExample"` | Club info |
| `sbt "runMain examples.BasicExample"` | API reference |

## Minimal Template

```scala
package examples
import cats.effect.{ExitCode, IO, IOApp}
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File

object MyExample extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val config = StravaConfig(
      clientId = sys.env("STRAVA_CLIENT_ID"),
      clientSecret = sys.env("STRAVA_CLIENT_SECRET")
    )
    
    StravaClient.resource[IO](config, new File("strava-token.json")).use { client =>
      client.athletes.getLoggedInAthlete().flatMap {
        case Right(athlete) => IO.println(s"Hello ${athlete.firstname}!")
        case Left(error) => IO.println(s"Error: ${error.message}")
      }
    }.as(ExitCode.Success)
  }
}
```

## Common Patterns

### Get Recent Activities
```scala
client.activities.getLoggedInAthleteActivities(perPage = 10)
```

### Get Last 30 Days
```scala
client.activities.getActivitiesByDateRange(
  from = ZonedDateTime.now().minusDays(30),
  to = ZonedDateTime.now()
)
```

### Get Athlete Stats
```scala
client.athletes.getLoggedInAthlete().flatMap {
  case Right(athlete) => 
    athlete.id.traverse(id => client.athletes.getStats(id))
  case Left(error) => 
    IO.println(s"Error: ${error.message}")
}
```

### Get Starred Segments
```scala
client.segments.getLoggedInAthleteStarredSegments()
```

### Explore Segments
```scala
client.segments.exploreSegments(
  bounds = (minLat, minLng, maxLat, maxLng),
  activityType = Some("running")
)
```

## Error Handling

```scala
result.flatMap {
  case Right(data) => 
    IO.println(s"Success: $data")
  case Left(error) => 
    error match {
      case StravaError.NotFoundError(msg) => IO.println(s"Not found: $msg")
      case StravaError.RateLimitError(msg, retryAfter) => IO.println("Rate limited")
      case StravaError.AuthenticationError(msg) => IO.println(s"Auth error: $msg")
      case other => IO.println(s"Error: ${other.message}")
    }
}
```

## Customizations

### Date Ranges
```scala
ZonedDateTime.now().minusDays(30)   // Last 30 days
ZonedDateTime.now().minusMonths(3)  // Last 3 months
ZonedDateTime.now().minusYears(1)   // Last year
```

### Filter by Type
```scala
activities.filter(_.`type`.contains(ActivityType.Run))
activities.filter(_.`type`.contains(ActivityType.Ride))
```

### Format Values
```scala
distance.map(_ / 1000)              // km
time.map(_ / 3600.0)                // hours
time.map(_ / 60)                    // minutes
(time / (distance / 1000)) / 60     // pace (min/km)
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Invalid credentials | Set environment variables |
| Authentication failed | Run `AuthenticationExample` |
| Token expired | Delete `strava-token.json` and re-auth |
| No activities | Check date range and scopes |

## Resources

- [Examples README](README.md)
- [Main README](../README.md)
- [User Guide](../GUIDE.md)
- [Strava API Docs](https://developers.strava.com/docs/reference/)
