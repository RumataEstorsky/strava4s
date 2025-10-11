package strava.api

import cats.effect.Sync
import io.circe.generic.auto._
import strava.core.StravaError
import strava.http.HttpClient
import strava.models._
import strava.models.api._
import java.time.ZonedDateTime

/**
 * API for activity-related endpoints
 */
class ActivitiesApi[F[_]: Sync](httpClient: HttpClient[F]) {

  /**
   * Get logged-in athlete's activities
   * @param before Unix timestamp to filter activities before
   * @param after Unix timestamp to filter activities after
   * @param page Page number
   * @param perPage Number of items per page (max 200)
   */
  def getLoggedInAthleteActivities(
    before: Option[Long] = None,
    after: Option[Long] = None,
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[SummaryActivity]]] = {
    val params = Map(
      "page" -> page.toString,
      "per_page" -> Math.min(perPage, 200).toString
    ) ++ before.map("before" -> _.toString) ++ after.map("after" -> _.toString)

    httpClient.get[List[SummaryActivity]]("athlete/activities", params)
  }

  /**
   * Get activities by date range
   */
  def getActivitiesByDateRange(
    from: ZonedDateTime,
    to: ZonedDateTime,
    perPage: Int = 200
  ): F[Either[StravaError, List[SummaryActivity]]] = {
    val fromEpoch = from.toInstant.getEpochSecond
    val toEpoch = to.toInstant.getEpochSecond
    getLoggedInAthleteActivities(Some(toEpoch), Some(fromEpoch), 1, perPage)
  }

  /**
   * Get detailed activity by ID
   * @param id Activity ID
   * @param includeAllEfforts Include all segment efforts
   */
  def getActivityById(
    id: Long,
    includeAllEfforts: Boolean = false
  ): F[Either[StravaError, DetailedActivity]] = {
    val params = Map("include_all_efforts" -> includeAllEfforts.toString)
    httpClient.get[DetailedActivity](s"activities/$id", params)
  }

  /**
   * Get activity comments
   * @param id Activity ID
   * @param page Page number
   * @param perPage Number of items per page
   */
  def getCommentsByActivityId(
    id: Long,
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[Comment]]] = {
    val params = Map("page" -> page.toString, "per_page" -> perPage.toString)
    httpClient.get[List[Comment]](s"activities/$id/comments", params)
  }

  /**
   * Get activity kudoers
   * @param id Activity ID
   * @param page Page number
   * @param perPage Number of items per page
   */
  def getKudoersByActivityId(
    id: Long,
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[SummaryAthlete]]] = {
    val params = Map("page" -> page.toString, "per_page" -> perPage.toString)
    httpClient.get[List[SummaryAthlete]](s"activities/$id/kudos", params)
  }

  /**
   * Get activity laps
   * @param id Activity ID
   */
  def getLapsByActivityId(id: Long): F[Either[StravaError, List[Lap]]] = {
    httpClient.get[List[Lap]](s"activities/$id/laps")
  }

  /**
   * Get activity zones
   * @param id Activity ID
   */
  def getZonesByActivityId(id: Long): F[Either[StravaError, List[ActivityZone]]] = {
    httpClient.get[List[ActivityZone]](s"activities/$id/zones")
  }

  /**
   * Update activity by ID
   * @param id Activity ID
   * @param updatable Fields to update
   */
  def updateActivityById(
    id: Long,
    updatable: UpdatableActivity
  ): F[Either[StravaError, DetailedActivity]] = {
    httpClient.put[DetailedActivity, UpdatableActivity](s"activities/$id", updatable)
  }

  /**
   * Create a manual activity
   * @param name Activity name
   * @param activityType Activity type
   * @param startDateLocal Start date in local timezone
   * @param elapsedTime Elapsed time in seconds
   * @param description Optional description
   * @param distance Optional distance in meters
   */
  def createActivity(
    name: String,
    activityType: String,
    startDateLocal: ZonedDateTime,
    elapsedTime: Int,
    description: Option[String] = None,
    distance: Option[Float] = None
  ): F[Either[StravaError, DetailedActivity]] = {
    val params = Map(
      "name" -> name,
      "type" -> activityType,
      "start_date_local" -> startDateLocal.toString,
      "elapsed_time" -> elapsedTime.toString
    ) ++ description.map("description" -> _) ++ distance.map(d => "distance" -> d.toString)

    httpClient.post[DetailedActivity, Map[String, String]](
      "activities",
      params
    )
  }

  /**
   * Fetch all activities for the logged-in athlete (handles pagination automatically)
   * @param before Unix timestamp to filter activities before
   * @param after Unix timestamp to filter activities after
   * @param perPage Number of items per page (max 200)
   * @param maxPages Maximum number of pages to fetch (safety limit)
   */
  def getAllActivities(
    before: Option[Long] = None,
    after: Option[Long] = None,
    perPage: Int = 200,
    maxPages: Int = 100
  ): F[Either[StravaError, List[SummaryActivity]]] = {
    Pagination.fetchAll[F, SummaryActivity](
      (page, perPage) => getLoggedInAthleteActivities(before, after, page, perPage),
      perPage,
      maxPages
    )
  }
}

object ActivitiesApi {
  def apply[F[_]: Sync](httpClient: HttpClient[F]): ActivitiesApi[F] =
    new ActivitiesApi[F](httpClient)
}

