package strava

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import sttp.client3.SttpBackend
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import strava.api.*
import strava.auth.{AuthManager, TokenStorage}
import strava.core.{StravaConfig, StravaError}
import strava.http.{HttpClient, RateLimiter}

import java.io.File

/**
 * Main Strava API client
 * 
 * Example usage:
 * {{{
 *   import cats.effect.IO
 *   
 *   val config = StravaConfig(
 *     clientId = "your-client-id",
 *     clientSecret = "your-client-secret"
 *   )
 *   
 *   StravaClient.resource[IO](config, File("token.json")).use { client =>
 *     for
 *       athlete <- client.athletes.getLoggedInAthlete()
 *       activities <- client.activities.getLoggedInAthleteActivities()
 *     yield (athlete, activities)
 *   }
 * }}}
 */
class StravaClient[F[_]] private (
  val activities: ActivitiesApi[F],
  val athletes: AthletesApi[F],
  val segments: SegmentsApi[F],
  val clubs: ClubsApi[F],
  val streams: StreamsApi[F],
  val auth: AuthManager[F]
)

object StravaClient:
  /**
   * Create a Strava client as a Resource (recommended)
   * The Resource will manage the HTTP backend lifecycle
   */
  def resource[F[_]](
    config: StravaConfig,
    tokenFile: File
  )(using A: Async[F]): Resource[F, StravaClient[F]] =
    for
      backend <- HttpClientCatsBackend.resource[F]()
      client <- Resource.eval(create[F](config, tokenFile, backend))
    yield client

  /**
   * Create a Strava client with a custom backend
   */
  def create[F[_]](
    config: StravaConfig,
    tokenFile: File,
    backend: SttpBackend[F, Any]
  )(using A: Async[F]): F[StravaClient[F]] =
    for
      tokenStorage <- A.delay(TokenStorage.file[F](tokenFile))
      rateLimiter <- if config.enableRateLimiting then RateLimiter.create[F] 
                     else A.pure(RateLimiter.noop[F])
    yield
      val authManager = AuthManager[F](config, tokenStorage, backend)
      val httpClient = HttpClient[F](config, authManager, rateLimiter, backend)
      
      val activitiesApi = ActivitiesApi[F](httpClient)
      val athletesApi = AthletesApi[F](httpClient)
      val segmentsApi = SegmentsApi[F](httpClient)
      val clubsApi = ClubsApi[F](httpClient)
      val streamsApi = StreamsApi[F](httpClient)
      
      StravaClient[F](
        activitiesApi,
        athletesApi,
        segmentsApi,
        clubsApi,
        streamsApi,
        authManager
      )

  /**
   * Create a client from environment variables
   */
  def fromEnv[F[_]](tokenFile: File)(using A: Async[F]): F[Either[StravaError, StravaClient[F]]] =
    StravaConfig.fromEnv() match
      case Right(config) =>
        resource[F](config, tokenFile).use: client =>
          A.pure(Right(client))
      case Left(error) =>
        A.pure(Left(error))

  /**
   * Helper to create a client with in-memory token storage (for testing)
   */
  def withInMemoryStorage[F[_]](
    config: StravaConfig
  )(using A: Async[F]): Resource[F, StravaClient[F]] =
    for
      backend <- HttpClientCatsBackend.resource[F]()
      tokenStorage <- Resource.eval(TokenStorage.inMemory[F])
      rateLimiter <- Resource.eval(
        if config.enableRateLimiting then RateLimiter.create[F]
        else A.pure(RateLimiter.noop[F])
      )
      authManager = AuthManager[F](config, tokenStorage, backend)
      httpClient = HttpClient[F](config, authManager, rateLimiter, backend)
      
      activitiesApi = ActivitiesApi[F](httpClient)
      athletesApi = AthletesApi[F](httpClient)
      segmentsApi = SegmentsApi[F](httpClient)
      clubsApi = ClubsApi[F](httpClient)
      streamsApi = StreamsApi[F](httpClient)
    yield StravaClient[F](
      activitiesApi,
      athletesApi,
      segmentsApi,
      clubsApi,
      streamsApi,
      authManager
    )
