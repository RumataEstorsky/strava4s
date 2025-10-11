package strava.auth

import cats.effect.Sync
import io.circe.parser._
import io.circe.syntax._
import io.circe.Decoder

import java.io.{File, PrintWriter}
import scala.io.Source

/**
 * Trait for storing and retrieving tokens
 */
trait TokenStorage[F[_]] {
  def save(token: StravaToken): F[Unit]
  def load(): F[Option[StravaToken]]
}

object TokenStorage {
  /**
   * File-based token storage implementation
   */
  def file[F[_]: Sync](file: File): TokenStorage[F] = new TokenStorage[F] {
    def save(token: StravaToken): F[Unit] = Sync[F].delay {
      val writer = new PrintWriter(file)
      try {
        writer.write(token.asJson.noSpaces)
      } finally {
        writer.close()
      }
    }

    def load(): F[Option[StravaToken]] = Sync[F].delay {
      if (!file.exists()) {
        None
      } else {
        val source = Source.fromFile(file)
        try {
          val content = source.mkString
          implicit val dec: Decoder[StravaToken] = StravaToken.decoder
          decode[StravaToken](content).toOption
        } finally {
          source.close()
        }
      }
    }
  }

  /**
   * In-memory token storage for testing
   */
  def inMemory[F[_]: Sync]: F[TokenStorage[F]] = Sync[F].delay {
    var token: Option[StravaToken] = None

    new TokenStorage[F] {
      def save(t: StravaToken): F[Unit] = Sync[F].delay {
        token = Some(t)
      }

      def load(): F[Option[StravaToken]] = Sync[F].delay(token)
    }
  }
}

