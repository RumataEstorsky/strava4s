package strava.auth

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

import java.time.Instant

/**
 * Represents a Strava OAuth token
 *
 * @param tokenType Type of token (usually "Bearer")
 * @param accessToken The access token to use for API requests
 * @param expiresAt Unix timestamp when the token expires
 * @param expiresIn Seconds until expiration (from when received)
 * @param refreshToken Token used to refresh the access token
 */
case class StravaToken(
  tokenType: String,
  accessToken: String,
  expiresAt: Long,
  expiresIn: Int,
  refreshToken: String
) {
  /**
   * Check if the token is expired (with a 5-minute buffer)
   */
  def isExpired: Boolean = {
    val now = Instant.now().getEpochSecond
    val buffer = 300 // 5 minutes
    now >= (expiresAt - buffer)
  }

  /**
   * Time remaining until expiration in seconds
   */
  def timeUntilExpiration: Long = {
    expiresAt - Instant.now().getEpochSecond
  }
}

object StravaToken {
  // Circe codecs for JSON serialization
  implicit val decoder: Decoder[StravaToken] = deriveDecoder[StravaToken]
  implicit val encoder: Encoder[StravaToken] = deriveEncoder[StravaToken]

  // Field name mappings for API responses
  implicit val decoderFromApi: Decoder[StravaToken] = Decoder.instance { cursor =>
    for {
      tokenType <- cursor.get[String]("token_type")
      accessToken <- cursor.get[String]("access_token")
      expiresAt <- cursor.get[Long]("expires_at")
      expiresIn <- cursor.get[Int]("expires_in")
      refreshToken <- cursor.get[String]("refresh_token")
    } yield StravaToken(tokenType, accessToken, expiresAt, expiresIn, refreshToken)
  }
}

