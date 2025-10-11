# Strava Client User Guide

Complete guide to using the Strava Client library.

## Table of Contents

- [Getting Started](#getting-started)
- [Authentication](#authentication)
- [Core Concepts](#core-concepts)
- [API Reference](#api-reference)
- [Common Patterns](#common-patterns)
- [Advanced Usage](#advanced-usage)
- [Testing](#testing)

## Getting Started

### Prerequisites

1. Create a Strava application at https://www.strava.com/settings/api
2. Note your Client ID and Client Secret
3. Set them as environment variables:

```bash
export STRAVA_CLIENT_ID='your-client-id'
export STRAVA_CLIENT_SECRET='your-client-secret'
```

### First Steps

1. **Authenticate** (run once):
   ```bash
   sbt "runMain examples.AuthenticationExample"
   ```

2. **Run your first example**:
   ```bash
   sbt "runMain examples.SimpleStarterExample"
   ```

3. **Start building** your own application

## Authentication

### OAuth Flow

```
┌────────────────────────────────────────────────────────────┐
│                     Authentication Flow                     │
└────────────────────────────────────────────────────────────┘

1. Generate authorization URL
   ↓
2. User visits URL in browser
   ↓
3. User authorizes application
   ↓
4. Strava redirects with code
   ↓
5. Exchange code for token
   ↓
6. Token saved to file
   ↓
7. Token automatically refreshed when expired
```

### Implementation

```scala
// Generate authorization URL
val authUrl = client.auth.authorizationUrl(
  redirectUri = "http://localhost:8080/callback",
  scope = "read,activity:read_all,activity:write"
)

// Exchange code for token
client.auth.exchangeToken(code, redirectUri).flatMap {
  case Right(token) => IO.println("Authentication successful!")
  case Left(error) => IO.println(s"Error: ${error.message}")
}
```

### Token Scopes

- `read` - Read public profile data
- `activity:read` - Read public activities
- `activity:read_all` - Read all activities (including private)
- `activity:write` - Create and update activities
- `profile:read_all` - Read private profile data
- `profile:write` - Update profile

### Token Storage

The library supports two storage modes:

**File-based** (recommended):
```scala
val tokenFile = new File("strava-token.json")
StravaClient.resource[IO](config, tokenFile).use { client =>
  // Token automatically saved and loaded
}
```

**In-memory** (for testing):
```scala
StravaClient.withInMemoryStorage[IO](config).use { client =>
  // Token only in memory
}
```

## Core Concepts

### Effect Type

The library is built on Cats Effect. All operations return `F[Either[StravaError, T]]`:

- `F[_]` - The effect type (usually `IO`)
- `Either` - Explicit error handling
- `StravaError` - Domain-specific errors
- `T` - The success result type

### Resource Management

Use `Resource` for automatic cleanup:

```scala
StravaClient.resource[IO](config, tokenFile).use { client =>
  // Client is automatically cleaned up after use
  client.activities.getLoggedInAthleteActivities()
}
```

### Error Handling

All errors are typed and explicit:

```scala
result.flatMap {
  case Right(data) => 
    // Success - use data
    IO.println(s"Success: $data")
    
  case Left(error) => 
    error match {
      case StravaError.NotFoundError(msg) => 
        IO.println(s"Resource not found: $msg")
      
      case StravaError.AuthenticationError(msg) => 
        IO.println(s"Auth failed: $msg")
      
      case StravaError.RateLimitError(msg, retryAfter) => 
        IO.println(s"Rate limited, retry after $retryAfter seconds")
      
      case StravaError.HttpError(code, msg, body) => 
        IO.println(s"HTTP $code: $msg")
      
      case StravaError.DecodingError(msg, cause) => 
        IO.println(s"Failed to parse response: $msg")
      
      case other => 
        IO.println(s"Error: ${other.message}")
    }
}
```

## API Reference

### Activities

#### List Activities

```scala
// Recent activities
client.activities.getLoggedInAthleteActivities(
  before = Some(epochTimestamp),
  after = Some(epochTimestamp),
  page = 1,
  perPage = 30
)

// By date range
client.activities.getActivitiesByDateRange(
  from = ZonedDateTime.now().minusDays(30),
  to = ZonedDateTime.now(),
  perPage = 200
)

// All activities (automatic pagination)
client.activities.getAllActivities(
  after = Some(ZonedDateTime.now().minusMonths(6).toEpochSecond),
  perPage = 200,
  maxPages = 50
)
```

#### Get Activity Details

```scala
// Basic details
client.activities.getActivityById(activityId)

// With all segment efforts
client.activities.getActivityById(activityId, includeAllEfforts = true)

// Activity comments
client.activities.getCommentsByActivityId(activityId)

// Activity kudos
client.activities.getKudoersByActivityId(activityId)

// Activity laps
client.activities.getLapsByActivityId(activityId)

// Activity zones
client.activities.getZonesByActivityId(activityId)
```

#### Create and Update Activities

```scala
// Create manual activity
client.activities.createActivity(
  name = "Morning Run",
  activityType = "Run",
  startDateLocal = ZonedDateTime.now(),
  elapsedTime = 3600,
  description = Some("Easy run"),
  distance = Some(10000f)
)

// Update activity
val updatable = UpdatableActivity(
  name = Some("Updated Activity Name"),
  description = Some("New description"),
  activityType = Some("Run"),
  gearId = Some("b12345")
)
client.activities.updateActivityById(activityId, updatable)
```

### Athletes

```scala
// Get current athlete
client.athletes.getLoggedInAthlete()

// Get athlete zones
client.athletes.getLoggedInAthleteZones()

// Get athlete stats
client.athletes.getStats(athleteId)

// Update athlete
client.athletes.updateLoggedInAthlete(
  weight = Some(75.0f)
)
```

### Segments

```scala
// Get segment details
client.segments.getSegmentById(segmentId)

// Star/unstar segment
client.segments.starSegment(segmentId, starred = true)

// Get starred segments
client.segments.getLoggedInAthleteStarredSegments()

// Explore segments in area
client.segments.exploreSegments(
  bounds = (minLat, minLng, maxLat, maxLng),
  activityType = Some("running"),
  minCat = Some(0),  // climb category
  maxCat = Some(5)
)

// Get segment effort
client.segments.getSegmentEffortById(effortId)

// Get all efforts on a segment
client.segments.getEffortsBySegmentId(segmentId)
```

### Clubs

```scala
// Get club details
client.clubs.getClubById(clubId)

// Get athlete's clubs
client.clubs.getLoggedInAthleteClubs()

// Get club activities
client.clubs.getClubActivitiesById(clubId)

// Get club members
client.clubs.getClubMembersById(clubId)

// Get club admins
client.clubs.getClubAdminsById(clubId)
```

### Streams

Streams provide detailed activity data (GPS, heart rate, power, etc.):

```scala
// Activity streams
client.streams.getActivityStreams(
  activityId,
  keys = Seq("time", "latlng", "distance", "altitude", "heartrate", "watts")
)

// Segment effort streams
client.streams.getSegmentEffortStreams(
  effortId,
  keys = Seq("time", "distance", "altitude")
)

// Segment streams
client.streams.getSegmentStreams(
  segmentId,
  keys = Seq("latlng", "altitude", "distance")
)

// Route streams
client.streams.getRouteStreams(routeId)
```

Available stream keys:
- `time` - Time in seconds
- `latlng` - GPS coordinates [lat, lng]
- `distance` - Distance in meters
- `altitude` - Altitude in meters
- `velocity_smooth` - Smoothed velocity
- `heartrate` - Heart rate in bpm
- `cadence` - Cadence in rpm
- `watts` - Power in watts
- `temp` - Temperature in celsius
- `moving` - Boolean moving flag
- `grade_smooth` - Smoothed grade percentage

## Common Patterns

### Filtering Activities

```scala
for {
  activities <- client.activities.getLoggedInAthleteActivities(perPage = 100)
  result <- activities match {
    case Right(acts) =>
      // Filter by type
      val runs = acts.filter(_.`type`.contains(ActivityType.Run))
      val rides = acts.filter(_.`type`.contains(ActivityType.Ride))
      
      // Filter by distance (> 10km)
      val longActivities = acts.filter(_.distance.exists(_ > 10000))
      
      // Filter by date
      val thisMonth = acts.filter { act =>
        act.start_date.exists { date =>
          date.getMonth == ZonedDateTime.now().getMonth
        }
      }
      
      IO.println(s"Runs: ${runs.size}, Rides: ${rides.size}")
    
    case Left(error) =>
      IO.println(s"Error: ${error.message}")
  }
} yield result
```

### Calculating Statistics

```scala
for {
  activities <- client.activities.getActivitiesByDateRange(
    from = ZonedDateTime.now().minusDays(30),
    to = ZonedDateTime.now()
  )
  
  _ <- activities match {
    case Right(acts) =>
      val totalDistance = acts.flatMap(_.distance).sum
      val totalTime = acts.flatMap(_.moving_time).sum
      val totalElevation = acts.flatMap(_.total_elevation_gain).sum
      
      IO.println(s"Total distance: ${totalDistance / 1000} km") >>
      IO.println(s"Total time: ${totalTime / 3600} hours") >>
      IO.println(s"Total elevation: $totalElevation m")
    
    case Left(error) =>
      IO.println(s"Error: ${error.message}")
  }
} yield ()
```

### Manual Pagination

```scala
import strava.api.Pagination

// Fetch all pages
Pagination.fetchAll(
  (page, perPage) => client.activities.getLoggedInAthleteActivities(
    page = page,
    perPage = perPage
  ),
  perPage = 200
)

// Manual control with iterator
Pagination.paginate(
  (page, perPage) => client.activities.getLoggedInAthleteActivities(
    page = page,
    perPage = perPage
  )
).flatMap { iterator =>
  def processPages(): IO[Unit] =
    iterator.next().flatMap {
      case Some(Right(activities)) if activities.nonEmpty =>
        IO.println(s"Processing ${activities.size} activities...") >>
        processPages()
      
      case Some(Left(error)) =>
        IO.println(s"Error: ${error.message}")
      
      case _ =>
        IO.println("Done!")
    }
  
  processPages()
}
```

### Working with Segments

```scala
// Find climbing segments in San Francisco
for {
  result <- client.segments.exploreSegments(
    bounds = (37.7, -122.5, 37.8, -122.4),
    activityType = Some("riding"),
    minCat = Some(3),  // Category 3 climbs or harder
    maxCat = Some(5)   // Up to HC climbs
  )
  
  _ <- result match {
    case Right(response) =>
      val segments = response.segments.getOrElse(Nil)
      segments.traverse { seg =>
        IO.println(s"${seg.name}: ${seg.climb_category} " +
                   s"(${seg.distance.getOrElse(0f) / 1000} km)")
      }
    
    case Left(error) =>
      IO.println(s"Error: ${error.message}")
  }
} yield ()
```

### Exporting to CSV

```scala
def exportToCSV(activities: List[SummaryActivity]): IO[Unit] = {
  val csv = activities.map { act =>
    val date = act.start_date.map(_.toString).getOrElse("")
    val name = act.name.getOrElse("")
    val distance = act.distance.map(_ / 1000).getOrElse(0f)
    val time = act.moving_time.map(_ / 60).getOrElse(0)
    val elevation = act.total_elevation_gain.getOrElse(0f)
    
    s"$date,$name,$distance,$time,$elevation"
  }.mkString("\n")
  
  val header = "Date,Name,Distance (km),Time (min),Elevation (m)\n"
  
  IO(Files.writeString(
    Paths.get("activities.csv"),
    header + csv
  ))
}
```

## Advanced Usage

### Custom HTTP Backend

```scala
import sttp.client3.httpclient.cats.HttpClientCatsBackend

val customBackend: Resource[IO, SttpBackend[IO, Any]] = 
  HttpClientCatsBackend.resource[IO]()

customBackend.use { backend =>
  StravaClient.create[IO](config, tokenFile, backend).flatMap { client =>
    // Use client
    client.athletes.getLoggedInAthlete()
  }
}
```

### Custom Configuration

```scala
val config = StravaConfig(
  clientId = "your-client-id",
  clientSecret = "your-client-secret",
  baseUrl = "https://www.strava.com/api/v3",
  requestTimeout = 60.seconds,     // Longer timeout
  maxRetries = 5,                  // More retries
  retryDelay = 2.seconds,          // Longer delay between retries
  enableRateLimiting = true
)
```

### Disabling Rate Limiting

For testing or when you want to handle rate limits manually:

```scala
val config = StravaConfig(
  clientId = "your-client-id",
  clientSecret = "your-client-secret",
  enableRateLimiting = false  // Disable automatic rate limiting
)
```

### Parallel Requests

```scala
import cats.implicits._

// Fetch multiple activities in parallel
val activityIds = List(123L, 456L, 789L)

activityIds.parTraverse { id =>
  client.activities.getActivityById(id)
}.flatMap { results =>
  results.traverse {
    case Right(activity) => 
      IO.println(s"Got ${activity.name}")
    case Left(error) => 
      IO.println(s"Error: ${error.message}")
  }
}
```

## Testing

### Using In-Memory Storage

```scala
StravaClient.withInMemoryStorage[IO](config).use { client =>
  // Set a test token
  val testToken = StravaToken(
    tokenType = "Bearer",
    accessToken = "test-token",
    expiresAt = System.currentTimeMillis() / 1000 + 3600,
    expiresIn = 3600,
    refreshToken = "refresh-token"
  )
  
  client.auth.storeToken(testToken) >>
  client.athletes.getLoggedInAthlete()
}
```

### Mocking Responses

For unit tests, you can provide your own HTTP backend that returns mock responses.

## Rate Limiting

The library automatically manages Strava's rate limits:

- **15-minute limit**: 100 requests per 15 minutes
- **Daily limit**: 1,000 requests per day

When approaching limits, the client automatically waits before making additional requests.

```
┌─────────────────────────────────────────────────────┐
│              Rate Limiting Behavior                  │
└─────────────────────────────────────────────────────┘

Request → Check limits → Under limit? → Make request
                              ↓ No
                         Wait until reset
```

## Best Practices

1. **Always use environment variables** for credentials
2. **Handle errors explicitly** with pattern matching
3. **Use pagination** for large result sets
4. **Respect rate limits** - keep automatic limiting enabled
5. **Use `Resource`** for proper cleanup
6. **Store tokens securely** - protect the token file
7. **Test with small datasets first** before fetching large amounts of data
8. **Cache responses** when appropriate to reduce API calls
9. **Use appropriate scopes** - request only what you need
10. **Monitor your rate limit usage** - check response headers

## Troubleshooting

### Authentication Fails

1. Check credentials are correct
2. Verify token file exists and is readable
3. Try deleting token file and re-authenticating
4. Check you have the correct scopes

### No Data Returned

1. Verify you have activities in the date range
2. Check your token has appropriate scopes
3. Ensure the resource ID is correct
4. Check for error messages in the response

### Rate Limit Errors

1. Rate limiting is enabled by default
2. The client should wait automatically
3. If needed, manually check the `retryAfter` value
4. Consider reducing request frequency

### JSON Parsing Errors

1. Ensure your library version is up to date
2. Check if Strava changed their API format
3. Report the issue with the failing JSON

## Additional Resources

- [Strava API Documentation](https://developers.strava.com/docs/reference/)
- [Strava API Playground](https://developers.strava.com/playground/)
- [Register Application](https://www.strava.com/settings/api)
- [Cats Effect Documentation](https://typelevel.org/cats-effect/)

