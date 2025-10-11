package strava.api

import cats.effect.Sync
import io.circe.generic.auto._
import strava.core.StravaError
import strava.http.HttpClient
import strava.models._
import strava.models.api._

/**
 * API for streams (activity data) endpoints
 */
class StreamsApi[F[_]: Sync](httpClient: HttpClient[F]) {

  /**
   * Get activity streams
   * @param id Activity ID
   * @param keys Stream types (e.g., "time", "latlng", "distance", "altitude", etc.)
   * @param keyByType Return streams keyed by type
   */
  def getActivityStreams(
    id: Long,
    keys: Seq[String],
    keyByType: Boolean = true
  ): F[Either[StravaError, StreamSet]] = {
    val params = Map(
      "keys" -> keys.mkString(","),
      "key_by_type" -> keyByType.toString
    )
    httpClient.get[StreamSet](s"activities/$id/streams", params)
  }

  /**
   * Get segment effort streams
   * @param id Segment effort ID
   * @param keys Stream types
   * @param keyByType Return streams keyed by type
   */
  def getSegmentEffortStreams(
    id: Long,
    keys: Seq[String],
    keyByType: Boolean = true
  ): F[Either[StravaError, StreamSet]] = {
    val params = Map(
      "keys" -> keys.mkString(","),
      "key_by_type" -> keyByType.toString
    )
    httpClient.get[StreamSet](s"segment_efforts/$id/streams", params)
  }

  /**
   * Get segment streams
   * @param id Segment ID
   * @param keys Stream types
   * @param keyByType Return streams keyed by type
   */
  def getSegmentStreams(
    id: Long,
    keys: Seq[String],
    keyByType: Boolean = true
  ): F[Either[StravaError, StreamSet]] = {
    val params = Map(
      "keys" -> keys.mkString(","),
      "key_by_type" -> keyByType.toString
    )
    httpClient.get[StreamSet](s"segments/$id/streams", params)
  }

  /**
   * Get route streams
   * @param id Route ID
   */
  def getRouteStreams(id: Long): F[Either[StravaError, StreamSet]] = {
    httpClient.get[StreamSet](s"routes/$id/streams")
  }
}

object StreamsApi {
  def apply[F[_]: Sync](httpClient: HttpClient[F]): StreamsApi[F] =
    new StreamsApi[F](httpClient)
}

