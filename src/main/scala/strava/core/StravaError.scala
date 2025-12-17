package strava.core

import io.circe.DecodingFailure

/**
 * Base trait for all Strava API errors
 */
sealed trait StravaError extends Throwable:
  def message: String
  override def getMessage: String = message

object StravaError:
  /** HTTP-related errors */
  case class HttpError(statusCode: Int, message: String, body: Option[String] = None) extends StravaError

  /** Authentication/authorization errors */
  case class AuthenticationError(message: String) extends StravaError

  /** Token has expired and needs refresh */
  case class TokenExpiredError(message: String = "Access token has expired") extends StravaError

  /** Rate limit exceeded */
  case class RateLimitError(message: String, retryAfter: Option[Long] = None) extends StravaError

  /** JSON parsing/decoding errors */
  case class DecodingError(message: String, cause: Option[DecodingFailure] = None) extends StravaError

  /** Network/connection errors */
  case class NetworkError(message: String, cause: Option[Throwable] = None) extends StravaError

  /** Configuration errors */
  case class ConfigurationError(message: String) extends StravaError

  /** Resource not found */
  case class NotFoundError(message: String) extends StravaError

  /** Bad request (validation error) */
  case class ValidationError(message: String, errors: Map[String, String] = Map.empty) extends StravaError

  /** Generic unexpected error */
  case class UnexpectedError(message: String, cause: Option[Throwable] = None) extends StravaError
