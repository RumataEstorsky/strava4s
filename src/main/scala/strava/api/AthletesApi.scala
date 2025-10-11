package strava.api

import cats.effect.Sync
import io.circe.generic.auto._
import strava.core.StravaError
import strava.http.HttpClient
import strava.models._
import strava.models.api._

/**
 * API for athlete-related endpoints
 */
class AthletesApi[F[_]: Sync](httpClient: HttpClient[F]) {

  /**
   * Get the currently authenticated athlete
   */
  def getLoggedInAthlete(): F[Either[StravaError, DetailedAthlete]] = {
    httpClient.get[DetailedAthlete]("athlete")
  }

  /**
   * Get zones for the logged-in athlete
   */
  def getLoggedInAthleteZones(): F[Either[StravaError, Zones]] = {
    httpClient.get[Zones]("athlete/zones")
  }

  /**
   * Get athlete stats
   * @param id Athlete ID
   */
  def getStats(id: Long): F[Either[StravaError, ActivityStats]] = {
    httpClient.get[ActivityStats](s"athletes/$id/stats")
  }

  /**
   * Update the logged-in athlete
   * @param weight Weight in kg
   */
  def updateLoggedInAthlete(weight: Option[Float] = None): F[Either[StravaError, DetailedAthlete]] = {
    val params = weight.map(w => Map("weight" -> w.toString)).getOrElse(Map.empty)
    httpClient.put[DetailedAthlete, Map[String, String]]("athlete", params)
  }
}

object AthletesApi {
  def apply[F[_]: Sync](httpClient: HttpClient[F]): AthletesApi[F] =
    new AthletesApi[F](httpClient)
}

