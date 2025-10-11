package strava.api

import cats.effect.Sync
import io.circe.generic.auto._
import strava.core.StravaError
import strava.http.HttpClient
import strava.models._
import strava.models.api._

/**
 * API for club-related endpoints
 */
class ClubsApi[F[_]: Sync](httpClient: HttpClient[F]) {

  /**
   * Get club by ID
   * @param id Club ID
   */
  def getClubById(id: Long): F[Either[StravaError, DetailedClub]] = {
    httpClient.get[DetailedClub](s"clubs/$id")
  }

  /**
   * Get clubs for logged-in athlete
   * @param page Page number
   * @param perPage Number of items per page
   */
  def getLoggedInAthleteClubs(
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[SummaryClub]]] = {
    val params = Map("page" -> page.toString, "per_page" -> perPage.toString)
    httpClient.get[List[SummaryClub]]("athlete/clubs", params)
  }

  /**
   * Get club activities
   * @param id Club ID
   * @param page Page number
   * @param perPage Number of items per page
   */
  def getClubActivitiesById(
    id: Long,
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[SummaryActivity]]] = {
    val params = Map("page" -> page.toString, "per_page" -> perPage.toString)
    httpClient.get[List[SummaryActivity]](s"clubs/$id/activities", params)
  }

  /**
   * Get club members
   * @param id Club ID
   * @param page Page number
   * @param perPage Number of items per page
   */
  def getClubMembersById(
    id: Long,
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[ClubAthlete]]] = {
    val params = Map("page" -> page.toString, "per_page" -> perPage.toString)
    httpClient.get[List[ClubAthlete]](s"clubs/$id/members", params)
  }

  /**
   * Get club admins
   * @param id Club ID
   * @param page Page number
   * @param perPage Number of items per page
   */
  def getClubAdminsById(
    id: Long,
    page: Int = 1,
    perPage: Int = 30
  ): F[Either[StravaError, List[SummaryAthlete]]] = {
    val params = Map("page" -> page.toString, "per_page" -> perPage.toString)
    httpClient.get[List[SummaryAthlete]](s"clubs/$id/admins", params)
  }
}

object ClubsApi {
  def apply[F[_]: Sync](httpClient: HttpClient[F]): ClubsApi[F] =
    new ClubsApi[F](httpClient)
}

