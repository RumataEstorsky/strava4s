package strava.core

import scala.concurrent.duration.*

/**
 * Configuration for the Strava API client
 *
 * @param clientId Strava application client ID
 * @param clientSecret Strava application client secret
 * @param baseUrl Base URL for Strava API (defaults to v3)
 * @param requestTimeout Timeout for HTTP requests
 * @param maxRetries Maximum number of retries for failed requests
 * @param retryDelay Delay between retry attempts
 */
case class StravaConfig(
  clientId: String,
  clientSecret: String,
  baseUrl: String = "https://www.strava.com/api/v3",
  requestTimeout: FiniteDuration = 30.seconds,
  maxRetries: Int = 3,
  retryDelay: FiniteDuration = 1.second,
  enableRateLimiting: Boolean = true
)

object StravaConfig:
  /**
   * Create config from environment variables
   * Expects STRAVA_CLIENT_ID and STRAVA_CLIENT_SECRET
   */
  def fromEnv(): Either[StravaError, StravaConfig] =
    for
      clientId <- sys.env.get("STRAVA_CLIENT_ID")
        .toRight(StravaError.ConfigurationError("STRAVA_CLIENT_ID environment variable not set"))
      clientSecret <- sys.env.get("STRAVA_CLIENT_SECRET")
        .toRight(StravaError.ConfigurationError("STRAVA_CLIENT_SECRET environment variable not set"))
    yield StravaConfig(clientId, clientSecret)
