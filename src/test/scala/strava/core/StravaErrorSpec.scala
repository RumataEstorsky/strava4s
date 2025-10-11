package strava.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StravaErrorSpec extends AnyFlatSpec with Matchers {

  "StravaError" should "have meaningful error messages" in {
    val httpError = StravaError.HttpError(404, "Not found", Some("Resource missing"))
    httpError.message shouldBe "Not found"
    httpError.statusCode shouldBe 404

    val authError = StravaError.AuthenticationError("Invalid token")
    authError.message shouldBe "Invalid token"

    val tokenExpired = StravaError.TokenExpiredError()
    tokenExpired.message shouldBe "Access token has expired"

    val rateLimitError = StravaError.RateLimitError("Too many requests", Some(3600))
    rateLimitError.message shouldBe "Too many requests"
    rateLimitError.retryAfter shouldBe Some(3600)

    val notFoundError = StravaError.NotFoundError("Activity not found")
    notFoundError.message shouldBe "Activity not found"
  }

  it should "be throwable" in {
    val error = StravaError.NetworkError("Connection failed", None)
    error shouldBe a[Throwable]
    error.getMessage shouldBe "Connection failed"
  }

  "ValidationError" should "support error details" in {
    val validationError = StravaError.ValidationError(
      "Validation failed",
      Map("name" -> "Name is required", "distance" -> "Distance must be positive")
    )

    validationError.message shouldBe "Validation failed"
    validationError.errors should have size 2
    validationError.errors("name") shouldBe "Name is required"
  }
}

