package strava.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class StravaConfigSpec extends AnyFlatSpec with Matchers {

  "StravaConfig" should "have sensible defaults" in {
    val config = StravaConfig(
      clientId = "test-id",
      clientSecret = "test-secret"
    )

    config.baseUrl shouldBe "https://www.strava.com/api/v3"
    config.requestTimeout shouldBe 30.seconds
    config.maxRetries shouldBe 3
    config.retryDelay shouldBe 1.second
    config.enableRateLimiting shouldBe true
  }

  it should "allow custom configuration" in {
    val config = StravaConfig(
      clientId = "custom-id",
      clientSecret = "custom-secret",
      baseUrl = "https://custom.api.com",
      requestTimeout = 60.seconds,
      maxRetries = 5,
      retryDelay = 2.seconds,
      enableRateLimiting = false
    )

    config.baseUrl shouldBe "https://custom.api.com"
    config.requestTimeout shouldBe 60.seconds
    config.maxRetries shouldBe 5
    config.retryDelay shouldBe 2.seconds
    config.enableRateLimiting shouldBe false
  }

  "StravaConfig.fromEnv" should "load from environment variables" in {
    // This test would need actual env vars set, so we just verify the error case
    StravaConfig.fromEnv() match {
      case Left(StravaError.ConfigurationError(msg)) =>
        msg should include("STRAVA_CLIENT_ID")
      case Right(_) =>
        // If env vars are actually set, that's fine too
        succeed
      case Left(other) =>
        fail(s"Unexpected error: $other")
    }
  }
}

