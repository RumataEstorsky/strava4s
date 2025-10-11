# Strava Client for Scala

A production-ready Strava API client library for Scala with functional programming support.

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Scala 2.13](https://img.shields.io/badge/scala-2.13-red.svg)](https://www.scala-lang.org)
[![Scala 3.6](https://img.shields.io/badge/scala-3.6-red.svg)](https://www.scala-lang.org)

## Features

- Complete API Coverage: Activities, Athletes, Segments, Clubs, Streams
- Built-in OAuth with automatic token refresh
- Automatic rate limiting that respects Strava's API limits
- Type-safe API leveraging Scala's type system
- Functional programming with Cats Effect
- Comprehensive test suite with real API response examples
- Resource-based API with automatic cleanup

## Quick Start

### Requirements

- Scala 2.13.x or 3.x
- sbt 1.x

**Note:** The library compiles and runs on both Scala 2.13 and 3.x, but the test suite currently only runs on Scala 2.13.x due to compatibility issues.

### Installation

Add to your `build.sbt`:

```scala
libraryDependencies += "valerii.svechikhin" %% "strava4s" % "<version>"
```

### Basic Usage

```scala
import cats.effect.{IO, IOApp}
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File

object MyApp extends IOApp.Simple {
  def run: IO[Unit] = {
    val config = StravaConfig(
      clientId = "your-client-id",
      clientSecret = "your-client-secret"
    )
    
    val tokenFile = new File("strava-token.json")
    
    StravaClient.resource[IO](config, tokenFile).use { client =>
      for {
        // Get authenticated athlete
        athlete <- client.athletes.getLoggedInAthlete()
        _ <- IO.println(s"Logged in as: ${athlete.map(_.firstname)}")
        
        // Get recent activities
        activities <- client.activities.getLoggedInAthleteActivities(perPage = 10)
        _ <- IO.println(s"Found ${activities.map(_.size)} activities")
      } yield ()
    }
  }
}
```

### First-Time Authentication

Before using the API, you need to authenticate:

```bash
export STRAVA_CLIENT_ID='your-client-id'
export STRAVA_CLIENT_SECRET='your-client-secret'
sbt "runMain examples.AuthenticationExample"
```

This creates a `strava-token.json` file that will be automatically refreshed.

## Key Features

### Activities API

```scala
// List recent activities
client.activities.getLoggedInAthleteActivities(perPage = 30)

// Get by date range
client.activities.getActivitiesByDateRange(
  from = ZonedDateTime.now().minusDays(30),
  to = ZonedDateTime.now()
)

// Get detailed activity
client.activities.getActivityById(activityId, includeAllEfforts = true)

// Get all activities with automatic pagination
client.activities.getAllActivities(maxPages = 50)
```

### Athletes API

```scala
// Get current athlete
client.athletes.getLoggedInAthlete()

// Get athlete stats
client.athletes.getStats(athleteId)

// Update athlete
client.athletes.updateLoggedInAthlete(weight = Some(75.0f))
```

### Segments API

```scala
// Get segment details
client.segments.getSegmentById(segmentId)

// Explore segments in area
client.segments.exploreSegments(
  bounds = (37.821362, -122.505373, 37.842038, -122.465977),
  activityType = Some("running")
)

// Star a segment
client.segments.starSegment(segmentId, starred = true)
```

### Error Handling

All API methods return `F[Either[StravaError, T]]`:

```scala
client.activities.getActivityById(activityId).flatMap {
  case Right(activity) =>
    IO.println(s"Activity: ${activity.name}")
  case Left(error) =>
    error match {
      case StravaError.NotFoundError(msg) => IO.println(s"Not found: $msg")
      case StravaError.RateLimitError(msg, retryAfter) => IO.println(s"Rate limited, retry after: $retryAfter")
      case other => IO.println(s"Error: ${other.message}")
    }
}
```

## Examples

The library includes ready-to-run examples in the [`examples/`](examples/) directory:

| Example | Description |
|---------|-------------|
| **SimpleStarterExample** | Your first program (~30 lines) |
| **AuthenticationExample** | Complete OAuth flow |
| **QuickStatsExample** | Daily dashboard with all your stats |
| **ActivityAnalysisExample** | Analyze activities with statistics |
| **MonthlyStatsExample** | Compare monthly progress |
| **SegmentExplorerExample** | Discover segments in any area |
| **ExportActivitiesExample** | Export activities to CSV |
| **ClubActivitiesExample** | View club members and activities |

Run any example:

```bash
sbt "runMain examples.QuickStatsExample"
```

See the [Examples Guide](examples/README.md) for details.

## Documentation

- **[User Guide](GUIDE.md)** - Comprehensive usage guide with patterns and best practices
- **[Architecture](ARCHITECTURE.md)** - Technical architecture and design decisions
- **[Contributing](CONTRIBUTING.md)** - How to contribute to the project
- **[Examples](examples/README.md)** - Detailed examples documentation
- **[Changelog](CHANGELOG.md)** - Version history

## Configuration

```scala
val config = StravaConfig(
  clientId = "your-client-id",
  clientSecret = "your-client-secret",
  baseUrl = "https://www.strava.com/api/v3", // default
  requestTimeout = 30.seconds,                // default
  maxRetries = 3,                             // default
  retryDelay = 1.second,                      // default
  enableRateLimiting = true                   // default
)

// Or load from environment variables
val configFromEnv = StravaConfig.fromEnv()
```

## Troubleshooting

### Authentication Issues

Delete the token file and re-authenticate:
```bash
rm strava-token.json
sbt "runMain examples.AuthenticationExample"
```

### Rate Limiting

Rate limiting is enabled by default. The client automatically waits when approaching limits.

### Missing Activity Data

Not all fields are always present in Strava responses. Use pattern matching or `.getOrElse()` to handle `Option` fields.

## Support

- [Open an Issue](https://github.com/vsvechikhin/strava4s/issues)
- [Strava API Documentation](https://developers.strava.com/docs/reference/)
- [Register Your Application](https://www.strava.com/settings/api)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
