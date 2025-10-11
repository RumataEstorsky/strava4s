# Examples

Practical examples to help you get started with the Strava API client.

## Quick Start

### First Time Setup

```bash
# 1. Set credentials
export STRAVA_CLIENT_ID='your-client-id'
export STRAVA_CLIENT_SECRET='your-client-secret'

# 2. Authenticate (creates strava-token.json)
sbt "runMain examples.AuthenticationExample"

# 3. Run any example
sbt "runMain examples.SimpleStarterExample"
```

## Available Examples

### For Beginners

#### SimpleStarterExample
**The simplest way to get started (~30 lines)**

```bash
sbt "runMain examples.SimpleStarterExample"
```

Perfect for:
- First-time users
- Understanding basics
- Quick verification

---

#### AuthenticationExample
**Complete OAuth authentication flow**

```bash
sbt "runMain examples.AuthenticationExample"
```

Perfect for:
- Setting up your token
- Testing credentials
- Understanding OAuth

---

### Activity Examples

#### QuickStatsExample
**Your daily dashboard**

```bash
sbt "runMain examples.QuickStatsExample"
```

Shows:
- Recent activity totals
- Year-to-date stats
- All-time statistics
- Separated by run/ride

Output:
```
============================================================
Strava Dashboard - John Doe
============================================================

Recent Run Totals:
  Count: 12
  Distance: 89.3 km
  Time: 7.2 hours
  Elevation: 456 m
```

---

#### ActivityAnalysisExample
**Analyze your activities with statistics**

```bash
sbt "runMain examples.ActivityAnalysisExample"
```

Features:
- Last 30 days summary
- Group by activity type
- Longest/fastest activities
- Beautiful ASCII tables

---

#### MonthlyStatsExample
**Compare monthly progress**

```bash
sbt "runMain examples.MonthlyStatsExample"
```

Shows:
- Current month stats
- Previous month comparison
- Month-over-month changes (↑ ↓)
- Activity breakdown by type

---

#### ExportActivitiesExample
**Export your data to CSV**

```bash
sbt "runMain examples.ExportActivitiesExample"
```

Creates `strava_activities.csv` with:
- Date, name, distance, time
- Pace, elevation, heart rate
- Ready for Excel/Google Sheets

---

### Segment Examples

#### SegmentExplorerExample
**Discover segments in your area**

```bash
sbt "runMain examples.SegmentExplorerExample"
```

Features:
- View starred segments
- Explore segments by location
- See segment statistics
- Find new challenges

**Note:** Change the `bounds` parameter to explore your area. Get coordinates from [BoundingBox](https://boundingbox.klokantech.com/).

---

### Club Examples

#### ClubActivitiesExample
**Work with your Strava clubs**

```bash
sbt "runMain examples.ClubActivitiesExample"
```

Shows:
- Your clubs
- Recent club activities
- Club members
- Club admins

---

### Reference

#### BasicExample
**Comprehensive API demonstration**

```bash
sbt "runMain examples.BasicExample"
```

Perfect for:
- Learning all API endpoints
- Reference implementation
- Error handling patterns

---

## Customization

### Change Date Range

```scala
// Last 30 days
ZonedDateTime.now().minusDays(30)

// Last 6 months
ZonedDateTime.now().minusMonths(6)

// Last year
ZonedDateTime.now().minusYears(1)
```

### Filter Activities

```scala
// After getting activities
val runs = activities.filter(_.`type`.contains(ActivityType.Run))
val rides = activities.filter(_.`type`.contains(ActivityType.Ride))
val longRuns = runs.filter(_.distance.exists(_ > 10000))
```

### Format Values

```scala
// Distance in km
distance.map(_ / 1000)

// Time in hours
time.map(_ / 3600.0)

// Pace (min/km)
(timeSeconds / (distanceMeters / 1000)) / 60
```

### Explore Your Area

```scala
client.segments.exploreSegments(
  bounds = (minLat, minLng, maxLat, maxLng),
  activityType = Some("running"), // or "cycling"
  minCat = Some(0),
  maxCat = Some(5)
)
```

## Example Template

Create your own example:

```scala
package examples

import cats.effect.{ExitCode, IO, IOApp}
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File

object MyCustomExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", ""),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "")
    )
    
    val tokenFile = new File("strava-token.json")

    StravaClient.resource[IO](config, tokenFile).use { client =>
      for {
        athlete <- client.athletes.getLoggedInAthlete()
        _ <- athlete match {
          case Right(a) => IO.println(s"Hello ${a.firstname}!")
          case Left(e) => IO.println(s"Error: ${e.message}")
        }
      } yield ()
    }.as(ExitCode.Success)
  }
}
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| "Invalid credentials" | Set `STRAVA_CLIENT_ID` and `STRAVA_CLIENT_SECRET` environment variables |
| "Authentication failed" | Run `AuthenticationExample` first |
| "Token expired" | Delete `strava-token.json` and re-authenticate |
| "No activities found" | Check date range and token scopes |

## Learning Path

1. **AuthenticationExample** - Get your token
2. **SimpleStarterExample** - Understand basics
3. **QuickStatsExample** - See available data
4. **ActivityAnalysisExample** - Learn operations
5. Explore other examples based on your needs!

## Tips

- Token is automatically refreshed when it expires
- Rate limits are respected automatically
- All examples show proper error handling patterns
- Copy and modify examples for your needs
- Use environment variables for credentials

## Resources

- [Main README](../README.md) - Complete API reference
- [User Guide](../GUIDE.md) - Comprehensive usage guide
- [Strava API Docs](https://developers.strava.com/docs/reference/)
- [Get API Keys](https://www.strava.com/settings/api)
