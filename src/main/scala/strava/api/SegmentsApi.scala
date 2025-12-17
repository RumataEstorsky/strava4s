package strava.api

import cats.effect.Sync
import strava.core.StravaError
import strava.http.HttpClient
import strava.models.api.*
import strava.models.given
import strava.models.api.codecs.given

/**
 * API for segment-related endpoints
 */
class SegmentsApi[F[_]](httpClient: HttpClient[F])(using F: Sync[F]):

  /**
   * Get segment by ID
   * @param id Segment ID
   */
  def getSegmentById(id: Long): F[Either[StravaError, DetailedSegment]] =
    httpClient.get[DetailedSegment](s"segments/$id")

  /**
   * Star a segment
   * @param id Segment ID
   * @param starred Whether to star or unstar
   */
  def starSegment(id: Long, starred: Boolean = true): F[Either[StravaError, DetailedSegment]] =
    val params = Map("starred" -> starred.toString)
    httpClient.put[DetailedSegment, Map[String, String]](s"segments/$id/starred", params)

  /**
   * Get starred segments for logged-in athlete
   * @param page Page number
   * @param perPage Number of items per page
   */
  def getLoggedInAthleteStarredSegments(
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[SummarySegment]]] =
    val params = Map("page" -> page.toString, "per_page" -> perPage.toString)
    httpClient.get[List[SummarySegment]]("segments/starred", params)

  /**
   * Explore segments
   * @param bounds SW and NE corners of bounding box (lat,lng,lat,lng)
   * @param activityType Activity type filter
   * @param minCat Minimum climb category
   * @param maxCat Maximum climb category
   */
  def exploreSegments(
    bounds: (Double, Double, Double, Double),
    activityType: Option[String] = None,
    minCat: Option[Int] = None,
    maxCat: Option[Int] = None
  ): F[Either[StravaError, ExplorerResponse]] =
    val (swLat, swLng, neLat, neLng) = bounds
    val params = Map(
      "bounds" -> s"$swLat,$swLng,$neLat,$neLng"
    ) ++ activityType.map("activity_type" -> _) ++
      minCat.map(c => "min_cat" -> c.toString) ++
      maxCat.map(c => "max_cat" -> c.toString)

    httpClient.get[ExplorerResponse]("segments/explore", params)

  /**
   * Get segment effort by ID
   * @param id Effort ID
   */
  def getSegmentEffortById(id: Long): F[Either[StravaError, DetailedSegmentEffort]] =
    httpClient.get[DetailedSegmentEffort](s"segment_efforts/$id")

  /**
   * Get efforts by segment ID
   * @param segmentId Segment ID
   * @param page Page number
   * @param perPage Number of items per page
   */
  def getEffortsBySegmentId(
    segmentId: Long,
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[DetailedSegmentEffort]]] =
    val params = Map(
      "segment_id" -> segmentId.toString,
      "page" -> page.toString,
      "per_page" -> perPage.toString
    )
    httpClient.get[List[DetailedSegmentEffort]](s"segments/$segmentId/all_efforts", params)

object SegmentsApi:
  def apply[F[_]: Sync](httpClient: HttpClient[F]): SegmentsApi[F] =
    new SegmentsApi[F](httpClient)
