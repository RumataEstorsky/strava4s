# Architecture

Technical architecture and design decisions for the Strava Client library.

## Design Principles

1. Type Safety - Uses Scala's type system to catch errors at compile time
2. Functional Programming - Pure functions and immutable data
3. Explicit Error Handling - Returns `Either[StravaError, T]` instead of throwing exceptions
4. Resource Management - Uses `Resource` for automatic cleanup
5. Composability - APIs can be easily composed
6. Testability - Interface-based design for easy mocking

## System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Strava Client                          │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              API Layer (strava.api)                  │  │
│  │  Activities│Athletes│Segments│Clubs│Streams         │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           HTTP Client (strava.http)                  │  │
│  │  • Request/Response handling                         │  │
│  │  • Retry logic                                       │  │
│  │  • Error mapping                                     │  │
│  └──────────────────────────────────────────────────────┘  │
│            ↓                        ↓                       │
│  ┌──────────────────┐    ┌─────────────────────────────┐  │
│  │  Rate Limiter    │    │   Auth Manager              │  │
│  │  • 15min: 100req │    │   • Token refresh           │  │
│  │  • Daily: 1000req│    │   • OAuth flow              │  │
│  └──────────────────┘    └─────────────────────────────┘  │
│                                     ↓                       │
│                          ┌─────────────────────────────┐   │
│                          │   Token Storage             │   │
│                          │   • File-based              │   │
│                          │   • In-memory               │   │
│                          └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────────┐
                    │   Strava API        │
                    │   (REST/JSON)       │
                    └─────────────────────┘
```

## Module Structure

### Core Module (`strava.core`)

Foundation types and configuration.

```scala
// Configuration
case class StravaConfig(
  clientId: String,
  clientSecret: String,
  baseUrl: String,
  requestTimeout: FiniteDuration,
  maxRetries: Int,
  retryDelay: FiniteDuration,
  enableRateLimiting: Boolean
)

// Error ADT
sealed trait StravaError {
  def message: String
}

case class HttpError(code: Int, message: String, body: Option[String])
case class AuthenticationError(message: String)
case class RateLimitError(message: String, retryAfter: Option[Long])
case class DecodingError(message: String, cause: Option[Throwable])
// ... more error types
```

### Authentication Module (`strava.auth`)

OAuth and token management.

**Components:**
- `StravaToken` - Immutable token with expiration logic
- `TokenStorage[F[_]]` - Trait for token persistence
- `AuthManager[F[_]]` - Token lifecycle management

**Token Lifecycle:**

```
┌─────────────────────────────────────────────────────────┐
│              Token Lifecycle                            │
└─────────────────────────────────────────────────────────┘

    User visits auth URL
           ↓
    Authorizes app
           ↓
    Redirect with code
           ↓
    Exchange code → Token
           ↓
    Save to storage
           ↓
    ┌──────────────────┐
    │  Use token       │ ← Token valid? → Yes → Make request
    │  for requests    │                   ↓ No
    └──────────────────┘                   ↓
           ↑                        Refresh token
           │                               ↓
           └───────────────────────── Update storage
```

### HTTP Layer (`strava.http`)

Low-level HTTP communication.

**HttpClient[F[_]]:**
- Generic HTTP operations (GET, POST, PUT, DELETE)
- Automatic retry logic with exponential backoff
- Error mapping to domain errors
- Token injection

**RateLimiter[F[_]]:**
- Tracks 15-minute and daily windows
- Automatic waiting when limits approached
- Syncs with API response headers

```
┌──────────────────────────────────────────────────────┐
│           Request Flow with Rate Limiting            │
└──────────────────────────────────────────────────────┘

Request
   ↓
Check rate limits
   ↓
Under limit? ───Yes──→ Proceed
   ↓ No                   ↓
Wait until reset      Get valid token
   ↓                      ↓
Proceed               Make HTTP request
                          ↓
                      Parse response
                          ↓
                      Update rate limits
                          ↓
                      Return result
```

### API Endpoints (`strava.api`)

High-level API implementations.

```
┌─────────────────────────────────────────────────────┐
│              API Endpoint Structure                 │
└─────────────────────────────────────────────────────┘

        StravaClient[F]
              │
    ┌─────────┼─────────┬──────────┬──────────┐
    ↓         ↓         ↓          ↓          ↓
Activities Athletes Segments   Clubs    Streams
    │         │         │          │          │
    └─────────┴─────────┴──────────┴──────────┘
                       ↓
                 HttpClient[F]
```

**Each API provides:**
- Type-safe operations
- Automatic pagination helpers
- Consistent error handling
- Comprehensive documentation

### Models (`strava.models.api`)

Data models generated from OpenAPI specification.

**Characteristics:**
- Case classes for immutability
- `Option[T]` for nullable fields
- Circe for JSON serialization/deserialization
- Type-safe enumerations

**Key model types:**
- `SummaryActivity` / `DetailedActivity`
- `SummaryAthlete` / `DetailedAthlete`
- `SummarySegment` / `DetailedSegment`
- `DetailedClub`
- Stream types (GPS, heart rate, power, etc.)

## Data Flow

### Typical API Call

```
┌──────────────────────────────────────────────────────────────┐
│                   API Call Flow                              │
└──────────────────────────────────────────────────────────────┘

User Code
    │
    └─→ client.activities.getActivityById(id)
            │
            └─→ ActivitiesApi
                    │
                    └─→ HttpClient.get[DetailedActivity](...)
                            │
                            ├─→ RateLimiter.checkAndWait()
                            │       │
                            │       └─→ Check limits
                            │           Wait if needed
                            │
                            ├─→ AuthManager.getValidToken()
                            │       │
                            │       ├─→ Load token
                            │       └─→ Refresh if expired
                            │
                            └─→ STTP HTTP Request
                                    │
                                    ↓
                            ┌───────────────┐
                            │  Strava API   │
                            └───────────────┘
                                    │
                                    ↓ HTTP Response
                                    │
                            Update rate limits
                                    │
                            Parse JSON (Circe)
                                    │
                            Either[StravaError, DetailedActivity]
                                    │
                                    ↓
                            User Code
```

### Error Handling Flow

```
┌────────────────────────────────────────────────────────┐
│              Error Handling Strategy                   │
└────────────────────────────────────────────────────────┘

HTTP Response
    │
    ├─→ 2xx Success → Parse JSON → Right(data)
    │
    ├─→ 401/403 → AuthenticationError → Left(error)
    │
    ├─→ 404 → NotFoundError → Left(error)
    │
    ├─→ 429 → RateLimitError → Left(error)
    │
    ├─→ 4xx → ValidationError → Left(error)
    │
    ├─→ 5xx → HttpError → Retry → Left(error)
    │
    └─→ Network error → NetworkError → Retry → Left(error)
```

## Key Design Patterns

### Effect System

All operations use `F[_]: Async` for composable effects:

```scala
def getActivityById(id: Long): F[Either[StravaError, DetailedActivity]]
```

**Benefits:**
- Referentially transparent
- Composable with other effects
- Easy to test
- Resource-safe

### Resource Pattern

Automatic cleanup with `Resource`:

```scala
StravaClient.resource[IO](config, tokenFile).use { client =>
  // Client lifecycle managed automatically
  // HTTP connections closed
  // Resources cleaned up
  client.activities.getLoggedInAthleteActivities()
}
```

### Either for Errors

Explicit error handling:

```scala
sealed trait StravaError
// All API methods return Either[StravaError, T]
```

**Benefits:**
- No hidden exceptions
- Forced error handling
- Type-safe error branches
- Clear error types

### Typeclass Constraints

```scala
class HttpClient[F[_]: Async](...)
class ActivitiesApi[F[_]: Async](...)
```

**Benefits:**
- Flexible effect types
- Testable with different F
- Abstract over IO, Task, etc.

## Extension Points

### Custom Token Storage

```scala
trait TokenStorage[F[_]] {
  def save(token: StravaToken): F[Unit]
  def load(): F[Option[StravaToken]]
}

// Implement for your storage backend
class RedisTokenStorage[F[_]: Async] extends TokenStorage[F] {
  def save(token: StravaToken): F[Unit] = ???
  def load(): F[Option[StravaToken]] = ???
}
```

### Custom HTTP Backend

```scala
// Use any STTP backend
import sttp.client3.okhttp.OkHttpFutureBackend

OkHttpFutureBackend.resource().use { backend =>
  StravaClient.create[IO](config, tokenFile, backend)
}
```

### Custom Rate Limiter

```scala
trait RateLimiter[F[_]] {
  def checkAndWait(): F[Unit]
  def recordRequest(): F[Unit]
  def recordResponse(limitRemaining: Option[Int]): F[Unit]
}
```

## Testing Strategy

### Unit Tests

- Model JSON parsing with real fixtures
- Token expiration logic
- Error handling
- Configuration validation

### Integration Tests

- Mock HTTP responses
- Test retry logic
- Test rate limiting
- Test token refresh

### Test Data

All tests use real Strava API response examples from `src/test/resources/strava/`:

```scala
val json = loadJson("get-activity-getactivitybyid.json")
val result = decode[DetailedActivity](json)
```

**Benefits:**
- Tests match real API behavior
- Catches schema changes
- Documents actual API responses

## Performance Considerations

1. **Connection Pooling** - STTP handles automatically
2. **JSON Parsing** - Circe uses compile-time derivation
3. **Rate Limiting** - Minimal overhead with Ref-based state
4. **Resource Management** - No leaks with Resource pattern
5. **Retry Logic** - Exponential backoff prevents thundering herd

## Security Considerations

1. **Token Storage** - File permissions should be restrictive
2. **HTTPS Only** - All requests use HTTPS
3. **No Token Logging** - Tokens never logged
4. **Environment Variables** - Support for env-based config
5. **Scope Restrictions** - Request minimum required scopes

## Dependency Graph

```
┌────────────────────────────────────────────────────┐
│              Dependency Structure                  │
└────────────────────────────────────────────────────┘

                 Cats Effect
                      │
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
    Cats Core      STTP         Circe
        │             │             │
        └─────────────┴─────────────┘
                      ↓
              Strava Client
                      │
        ┌─────────────┼─────────────┬──────────┐
        ↓             ↓             ↓          ↓
     Core         Auth          HTTP         API
        │             │             │          │
        └─────────────┴─────────────┴──────────┘
                      ↓
                  Models
```

**Why these dependencies:**
- **Cats Effect** - Industry standard for functional effects
- **STTP** - Flexible, backend-agnostic HTTP client
- **Circe** - Fast, type-safe JSON with good error messages

## Future Enhancements

Potential improvements:

1. **Streaming** - FS2 streams for large datasets
2. **Caching** - Optional caching layer for GET requests
3. **Circuit Breaker** - Fail-fast when API is down
4. **Metrics** - Built-in metrics collection
5. **Bulk Operations** - Batch multiple requests
6. **WebSocket** - Real-time updates support

## References

- [Strava API Documentation](https://developers.strava.com/docs/reference/)
- [Cats Effect](https://typelevel.org/cats-effect/)
- [STTP](https://sttp.softwaremill.com/)
- [Circe](https://circe.github.io/circe/)
