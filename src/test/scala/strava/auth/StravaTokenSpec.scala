package strava.auth

import io.circe.parser._
import io.circe.syntax._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import java.time.Instant

class StravaTokenSpec extends AnyFlatSpec with Matchers with EitherValues {

  "StravaToken" should "detect expired tokens" in {
    val expiredToken = StravaToken(
      tokenType = "Bearer",
      accessToken = "test-token",
      expiresAt = Instant.now().getEpochSecond - 3600, // 1 hour ago
      expiresIn = 21600,
      refreshToken = "refresh-token"
    )
    
    expiredToken.isExpired shouldBe true
  }

  it should "detect valid tokens" in {
    val validToken = StravaToken(
      tokenType = "Bearer",
      accessToken = "test-token",
      expiresAt = Instant.now().getEpochSecond + 7200, // 2 hours from now
      expiresIn = 21600,
      refreshToken = "refresh-token"
    )
    
    validToken.isExpired shouldBe false
  }

  it should "serialize to JSON" in {
    val token = StravaToken(
      tokenType = "Bearer",
      accessToken = "test-token",
      expiresAt = 1234567890,
      expiresIn = 21600,
      refreshToken = "refresh-token"
    )
    
    val json = token.asJson.noSpaces
    json should include("\"accessToken\"")
    json should include("\"test-token\"")
  }

  it should "deserialize from API response format" in {
    val json = """
      {
        "token_type": "Bearer",
        "access_token": "api-token",
        "expires_at": 1234567890,
        "expires_in": 21600,
        "refresh_token": "refresh-token"
      }
    """
    
    val result = decode[StravaToken](json)(StravaToken.decoderFromApi)
    result shouldBe a[Right[_, _]]
    
    val token = result.value
    token.tokenType shouldBe "Bearer"
    token.accessToken shouldBe "api-token"
    token.expiresAt shouldBe 1234567890
  }

  it should "calculate time until expiration" in {
    val now = Instant.now().getEpochSecond
    val token = StravaToken(
      tokenType = "Bearer",
      accessToken = "test-token",
      expiresAt = now + 3600, // 1 hour from now
      expiresIn = 21600,
      refreshToken = "refresh-token"
    )
    
    val timeRemaining = token.timeUntilExpiration
    timeRemaining should be > 3500L
    timeRemaining should be <= 3600L
  }
}

