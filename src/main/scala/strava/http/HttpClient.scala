package strava.http

import cats.effect.Async
import cats.syntax.all.*
import io.circe.{Decoder, Encoder}
import io.circe.parser.*
import io.circe.syntax.*
import sttp.client3.*
import sttp.model.{StatusCode, Uri}
import strava.auth.{AuthManager, StravaToken}
import strava.core.{StravaConfig, StravaError}
import org.slf4j.LoggerFactory

/**
 * HTTP client for making requests to Strava API
 */
class HttpClient[F[_]](
  config: StravaConfig,
  authManager: AuthManager[F],
  rateLimiter: RateLimiter[F],
  backend: SttpBackend[F, Any]
)(using A: Async[F]):
  private val logger = LoggerFactory.getLogger(getClass)

  /** Make a GET request */
  def get[T: Decoder](
    path: String,
    params: Map[String, String] = Map.empty
  ): F[Either[StravaError, T]] =
    makeRequest[T, String](path, params, RequestMethod.GET, None)

  /** Make a POST request */
  def post[T: Decoder, B: Encoder](
    path: String,
    body: B,
    params: Map[String, String] = Map.empty
  ): F[Either[StravaError, T]] =
    makeRequest[T, B](path, params, RequestMethod.POST, Some(body))

  /** Make a PUT request */
  def put[T: Decoder, B: Encoder](
    path: String,
    body: B,
    params: Map[String, String] = Map.empty
  ): F[Either[StravaError, T]] =
    makeRequest[T, B](path, params, RequestMethod.PUT, Some(body))

  /** Make a DELETE request */
  def delete[T: Decoder](
    path: String,
    params: Map[String, String] = Map.empty
  ): F[Either[StravaError, T]] =
    makeRequest[T, String](path, params, RequestMethod.DELETE, None)

  /** Core request method with retry logic */
  private def makeRequest[T: Decoder, B: Encoder](
    path: String,
    params: Map[String, String],
    method: RequestMethod,
    body: Option[B],
    retryCount: Int = 0
  ): F[Either[StravaError, T]] =
    for
      // Rate limiting
      _ <- if config.enableRateLimiting then rateLimiter.checkAndWait() else A.unit

      // Get valid token
      tokenResult <- authManager.getValidToken()

      result <- tokenResult match
        case Left(error) =>
          A.pure(Left(error))
        case Right(token) =>
          executeRequest[T, B](path, params, method, body, token, retryCount)
    yield result

  private def executeRequest[T: Decoder, B: Encoder](
    path: String,
    params: Map[String, String],
    method: RequestMethod,
    body: Option[B],
    token: StravaToken,
    retryCount: Int
  ): F[Either[StravaError, T]] =
    val url = s"${config.baseUrl}/$path"
    val uri = Uri.parse(url).map(_.addParams(params)).getOrElse(uri"$url")

    logger.debug(s"$method $uri")
    
    val sttpMethod = method match
      case RequestMethod.GET => sttp.model.Method.GET
      case RequestMethod.POST => sttp.model.Method.POST
      case RequestMethod.PUT => sttp.model.Method.PUT
      case RequestMethod.DELETE => sttp.model.Method.DELETE

    val baseRequest = basicRequest
      .method(sttpMethod, uri)
      .header("Authorization", s"Bearer ${token.accessToken}")
      .readTimeout(config.requestTimeout)
      .response(asString)

    val request = body match
      case Some(b) => baseRequest.body(b.asJson.noSpaces).header("Content-Type", "application/json")
      case None => baseRequest

    request.send(backend).flatMap: response =>
      // Record rate limit info from Strava headers
      // X-RateLimit-Limit: "100,1000" (15-min limit, daily limit)
      // X-RateLimit-Usage: "34,76" (15-min usage, daily usage)
      val rateLimitUsage = response.header("X-RateLimit-Usage").flatMap: usage =>
        usage.split(",").headOption.flatMap(_.trim.toIntOption)
      val rateLimitMax = response.header("X-RateLimit-Limit").flatMap: limit =>
        limit.split(",").headOption.flatMap(_.trim.toIntOption)
      // Calculate remaining from usage and limit
      val remaining = for
        used <- rateLimitUsage
        max <- rateLimitMax
      yield max - used
      
      rateLimiter.recordResponse(remaining) >>
      rateLimiter.recordRequest() >>
      handleResponse[T, B](response, path, params, method, body, token, retryCount)
    .handleErrorWith: e =>
      logger.error(s"Network error: ${e.getMessage}", e)
      if retryCount < config.maxRetries then
        logger.info(s"Retrying request (attempt ${retryCount + 1}/${config.maxRetries})...")
        A.sleep(config.retryDelay) >>
          executeRequest[T, B](path, params, method, body, token, retryCount + 1)
      else A.pure(Left(StravaError.NetworkError(e.getMessage, Some(e))))

  private def handleResponse[T: Decoder, B: Encoder](
    response: Response[Either[String, String]],
    path: String,
    params: Map[String, String],
    method: RequestMethod,
    body: Option[B],
    token: StravaToken,
    retryCount: Int
  ): F[Either[StravaError, T]] =
    response.code match
      case StatusCode.Ok | StatusCode.Created =>
        response.body match
          case Right(json) =>
            decode[T](json) match
              case Right(data) =>
                A.pure(Right(data))
              case Left(error) =>
                logger.error(s"Failed to decode response: ${error.getMessage}")
                val decodingFailure = error match
                  case df: io.circe.DecodingFailure => Some(df)
                  case _ => None
                A.pure(Left(StravaError.DecodingError(
                  s"Failed to parse response: ${error.getMessage}",
                  decodingFailure
                )))
          case Left(error) =>
            A.pure(Left(StravaError.HttpError(
              response.code.code,
              "Request failed",
              Some(error)
            )))

      case StatusCode.Unauthorized =>
        // Token might be invalid, try refreshing
        if retryCount < config.maxRetries then
          logger.info("Received 401, refreshing token...")
          authManager.refreshToken(token).flatMap:
            case Right(newToken) =>
              executeRequest[T, B](path, params, method, body, newToken, retryCount + 1)
            case Left(error) =>
              A.pure(Left(error))
        else A.pure(Left(StravaError.AuthenticationError("Unauthorized")))

      case StatusCode.NotFound =>
        A.pure(Left(StravaError.NotFoundError(s"Resource not found: $path")))

      case StatusCode.TooManyRequests =>
        // Parse Retry-After header (seconds to wait)
        val retryAfter = response.header("Retry-After").flatMap(_.toLongOption).getOrElse(900L) // Default 15 min
        if retryCount < config.maxRetries then
          import scala.concurrent.duration.*
          logger.warn(s"Rate limit exceeded (429). Waiting ${retryAfter}s before retry (attempt ${retryCount + 1}/${config.maxRetries})...")
          A.sleep(retryAfter.seconds) >>
            executeRequest[T, B](path, params, method, body, token, retryCount + 1)
        else
          A.pure(Left(StravaError.RateLimitError(
            "Rate limit exceeded after max retries",
            Some(retryAfter)
          )))

      case StatusCode.BadRequest =>
        response.body match
          case Right(json) =>
            A.pure(Left(StravaError.ValidationError(s"Bad request: $json")))
          case Left(error) =>
            A.pure(Left(StravaError.ValidationError(error)))

      case code =>
        A.pure(Left(StravaError.HttpError(
          code.code,
          s"HTTP ${code.code}",
          response.body.left.toOption
        )))

object HttpClient:
  def apply[F[_]: Async](
    config: StravaConfig,
    authManager: AuthManager[F],
    rateLimiter: RateLimiter[F],
    backend: SttpBackend[F, Any]
  ): HttpClient[F] = new HttpClient[F](config, authManager, rateLimiter, backend)

// Helper for method types
private[http] enum RequestMethod:
  case GET, POST, PUT, DELETE
