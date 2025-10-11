package strava.http

import cats.effect.{Ref, Sync, Temporal}
import cats.syntax.all._
import scala.concurrent.duration._

/**
 * Rate limiter for Strava API calls
 * Strava has rate limits: 100 requests per 15 minutes, 1000 requests per day
 */
trait RateLimiter[F[_]] {
  def checkAndWait(): F[Unit]
  def recordRequest(): F[Unit]
  def recordResponse(limitRemaining: Option[Int]): F[Unit]
}

object RateLimiter {
  /**
   * State for rate limiting
   */
  case class RateLimitState(
    requestCount15Min: Int,
    requestCountDaily: Int,
    window15MinStart: Long,
    windowDailyStart: Long,
    apiLimit15Min: Option[Int],
    apiLimitDaily: Option[Int]
  )

  /**
   * Create a rate limiter with default Strava limits
   */
  def create[F[_]: Temporal]: F[RateLimiter[F]] = {
    val now = System.currentTimeMillis()
    Ref.of[F, RateLimitState](
      RateLimitState(
        requestCount15Min = 0,
        requestCountDaily = 0,
        window15MinStart = now,
        windowDailyStart = now,
        apiLimit15Min = Some(100),
        apiLimitDaily = Some(1000)
      )
    ).map { stateRef =>
      new RateLimiter[F] {
        private val fifteenMinutes = 15.minutes.toMillis
        private val oneDay = 1.day.toMillis

        def checkAndWait(): F[Unit] = {
          stateRef.get.flatMap { state =>
            val now = System.currentTimeMillis()

            // Reset windows if expired
            val resetState = {
              var s = state
              if (now - s.window15MinStart >= fifteenMinutes) {
                s = s.copy(requestCount15Min = 0, window15MinStart = now)
              }
              if (now - s.windowDailyStart >= oneDay) {
                s = s.copy(requestCountDaily = 0, windowDailyStart = now)
              }
              s
            }

            // Check if we need to wait
            val needsWait = (
              state.apiLimit15Min.exists(_ <= resetState.requestCount15Min) ||
              state.apiLimitDaily.exists(_ <= resetState.requestCountDaily)
            )

            if (needsWait) {
              // Calculate wait time
              val wait15Min = if (state.apiLimit15Min.exists(_ <= resetState.requestCount15Min)) {
                Some(fifteenMinutes - (now - resetState.window15MinStart))
              } else None

              val waitDaily = if (state.apiLimitDaily.exists(_ <= resetState.requestCountDaily)) {
                Some(oneDay - (now - resetState.windowDailyStart))
              } else None

              val waitTime = Seq(wait15Min, waitDaily).flatten.min

              Temporal[F].sleep(waitTime.millis) >> stateRef.set(resetState)
            } else {
              stateRef.set(resetState)
            }
          }
        }

        def recordRequest(): F[Unit] = {
          stateRef.update { state =>
            state.copy(
              requestCount15Min = state.requestCount15Min + 1,
              requestCountDaily = state.requestCountDaily + 1
            )
          }
        }

        def recordResponse(limitRemaining: Option[Int]): F[Unit] = {
          limitRemaining match {
            case Some(remaining) =>
              stateRef.update { state =>
                // Update our limits based on API response
                state.copy(apiLimit15Min = Some(remaining))
              }
            case None =>
              Temporal[F].unit
          }
        }
      }
    }
  }

  /**
   * No-op rate limiter for testing
   */
  def noop[F[_]: Sync]: RateLimiter[F] = new RateLimiter[F] {
    def checkAndWait(): F[Unit] = Sync[F].unit
    def recordRequest(): F[Unit] = Sync[F].unit
    def recordResponse(limitRemaining: Option[Int]): F[Unit] = Sync[F].unit
  }
}

