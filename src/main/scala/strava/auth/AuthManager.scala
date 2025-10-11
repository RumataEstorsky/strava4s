package strava.auth

import cats.effect.Sync
import cats.syntax.all._
import sttp.client3._
import sttp.client3.circe._
import sttp.model.Uri
import strava.core.{StravaConfig, StravaError}
import org.slf4j.LoggerFactory

/**
 * Manages OAuth authentication and token refresh for Strava API
 */
class AuthManager[F[_]: Sync](
  config: StravaConfig,
  storage: TokenStorage[F],
  backend: SttpBackend[F, Any]
) {
  private val logger = LoggerFactory.getLogger(getClass)

  /**
   * Get a valid access token, refreshing if necessary
   */
  def getValidToken(): F[Either[StravaError, StravaToken]] = {
    for {
      tokenOpt <- storage.load()
      result <- tokenOpt match {
        case None =>
          Sync[F].pure(Left(StravaError.AuthenticationError(
            "No token found. Please authenticate first."
          )): Either[StravaError, StravaToken])
        case Some(token) if token.isExpired =>
          logger.info("Token expired, refreshing...")
          refreshToken(token)
        case Some(token) =>
          logger.debug(s"Token valid for ${token.timeUntilExpiration} seconds")
          Sync[F].pure(Right(token): Either[StravaError, StravaToken])
      }
    } yield result
  }

  /**
   * Refresh an expired token
   */
  def refreshToken(oldToken: StravaToken): F[Either[StravaError, StravaToken]] = {
    val uri = Uri.parse(s"${config.baseUrl.replace("/api/v3", "")}/oauth/token").toOption.get

    implicit val decoder: io.circe.Decoder[StravaToken] = StravaToken.decoderFromApi

    val request = basicRequest
      .post(uri)
      .body(Map(
        "grant_type" -> "refresh_token",
        "refresh_token" -> oldToken.refreshToken,
        "client_id" -> config.clientId,
        "client_secret" -> config.clientSecret
      ))
      .response(asJson[StravaToken])

    request.send(backend).flatMap { response =>
      response.body match {
        case Right(token) =>
          logger.info("Token refreshed successfully")
          storage.save(token).map(_ => Right(token): Either[StravaError, StravaToken])
        case Left(error) =>
          val errorMsg = error match {
            case HttpError(body, statusCode) =>
              s"Failed to refresh token: HTTP $statusCode - $body"
            case DeserializationException(body, error) =>
              s"Failed to parse token response: ${error.getMessage}"
          }
          logger.error(errorMsg)
          Sync[F].pure(Left(StravaError.AuthenticationError(errorMsg)): Either[StravaError, StravaToken])
      }
    }.handleErrorWith { e =>
      logger.error("Network error during token refresh", e)
      Sync[F].pure(Left(StravaError.NetworkError(
        s"Failed to refresh token: ${e.getMessage}",
        Some(e)
      )): Either[StravaError, StravaToken])
    }
  }

  /**
   * Store a new token (e.g., after initial OAuth flow)
   */
  def storeToken(token: StravaToken): F[Unit] = {
    storage.save(token)
  }

  /**
   * Exchange an authorization code for tokens (initial OAuth flow)
   */
  def exchangeToken(code: String, redirectUri: String): F[Either[StravaError, StravaToken]] = {
    val uri = Uri.parse(s"${config.baseUrl.replace("/api/v3", "")}/oauth/token").toOption.get

    implicit val decoder: io.circe.Decoder[StravaToken] = StravaToken.decoderFromApi

    val request = basicRequest
      .post(uri)
      .body(Map(
        "client_id" -> config.clientId,
        "client_secret" -> config.clientSecret,
        "code" -> code,
        "grant_type" -> "authorization_code",
        "redirect_uri" -> redirectUri
      ))
      .response(asJson[StravaToken])

    request.send(backend).flatMap { response =>
      response.body match {
        case Right(token) =>
          storage.save(token).map(_ => Right(token): Either[StravaError, StravaToken])
        case Left(error) =>
          val errorMsg = error match {
            case HttpError(body, statusCode) =>
              s"Failed to exchange code: HTTP $statusCode - $body"
            case DeserializationException(body, error) =>
              s"Failed to parse token response: ${error.getMessage}"
          }
          Sync[F].pure(Left(StravaError.AuthenticationError(errorMsg)): Either[StravaError, StravaToken])
      }
    }.handleErrorWith { e =>
      Sync[F].pure(Left(StravaError.NetworkError(
        s"Failed to exchange code: ${e.getMessage}",
        Some(e)
      )): Either[StravaError, StravaToken])
    }
  }

  /**
   * Generate OAuth authorization URL
   */
  def authorizationUrl(redirectUri: String, scope: String = "read,activity:read_all"): String = {
    val baseUri = config.baseUrl.replace("/api/v3", "")
    s"$baseUri/oauth/authorize?client_id=${config.clientId}&response_type=code&redirect_uri=$redirectUri&approval_prompt=auto&scope=$scope"
  }
}

object AuthManager {
  def apply[F[_]: Sync](
    config: StravaConfig,
    storage: TokenStorage[F],
    backend: SttpBackend[F, Any]
  ): AuthManager[F] = new AuthManager[F](config, storage, backend)
}

