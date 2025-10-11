package strava.auth

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files
import java.time.Instant

class TokenStorageSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  "InMemory TokenStorage" should "store and retrieve tokens" in {
    val token = StravaToken(
      tokenType = "Bearer",
      accessToken = "test-token",
      expiresAt = Instant.now().getEpochSecond + 3600,
      expiresIn = 21600,
      refreshToken = "refresh-token"
    )

    (for {
      storage <- TokenStorage.inMemory[IO]
      _ <- storage.save(token)
      retrieved <- storage.load()
    } yield {
      retrieved shouldBe Some(token)
    }).asserting(identity)
  }

  it should "return None when no token is stored" in {
    TokenStorage.inMemory[IO].flatMap(_.load()).asserting { result =>
      result shouldBe None
    }
  }

  it should "overwrite existing token" in {
    val token1 = StravaToken(
      tokenType = "Bearer",
      accessToken = "token-1",
      expiresAt = Instant.now().getEpochSecond + 3600,
      expiresIn = 21600,
      refreshToken = "refresh-1"
    )

    val token2 = StravaToken(
      tokenType = "Bearer",
      accessToken = "token-2",
      expiresAt = Instant.now().getEpochSecond + 3600,
      expiresIn = 21600,
      refreshToken = "refresh-2"
    )

    (for {
      storage <- TokenStorage.inMemory[IO]
      _ <- storage.save(token1)
      _ <- storage.save(token2)
      retrieved <- storage.load()
    } yield {
      retrieved shouldBe Some(token2)
      retrieved.get.accessToken shouldBe "token-2"
    }).asserting(identity)
  }

  "File TokenStorage" should "persist and retrieve tokens from file" in {
    val tempFile = Files.createTempFile("strava-token-test", ".json").toFile
    tempFile.deleteOnExit()

    val token = StravaToken(
      tokenType = "Bearer",
      accessToken = "file-token",
      expiresAt = Instant.now().getEpochSecond + 3600,
      expiresIn = 21600,
      refreshToken = "file-refresh"
    )

    val storage = TokenStorage.file[IO](tempFile)

    (for {
      _ <- storage.save(token)
      retrieved <- storage.load()
      _ = tempFile.delete()
    } yield {
      retrieved shouldBe Some(token)
    }).asserting(identity)
  }

  it should "return None when file doesn't exist" in {
    val nonExistentFile = new File("/tmp/non-existent-token-file.json")
    val storage = TokenStorage.file[IO](nonExistentFile)

    storage.load().asserting { result =>
      result shouldBe None
    }
  }
}

