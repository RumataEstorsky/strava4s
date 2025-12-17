package strava.auth

import cats.effect.Sync
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.Decoder

import java.io.{File, PrintWriter}
import scala.io.Source

/**
 * Trait for storing and retrieving tokens
 */
trait TokenStorage[F[_]]:
  def save(token: StravaToken): F[Unit]
  def load(): F[Option[StravaToken]]

object TokenStorage:
  /**
   * File-based token storage implementation
   */
  def file[F[_]](file: File)(using F: Sync[F]): TokenStorage[F] = new TokenStorage[F]:
    def save(token: StravaToken): F[Unit] = F.delay:
      val writer = PrintWriter(file)
      try writer.write(token.asJson.noSpaces)
      finally writer.close()

    def load(): F[Option[StravaToken]] = F.delay:
      if !file.exists() then None
      else
        val source = Source.fromFile(file)
        try
          val content = source.mkString
          given Decoder[StravaToken] = StravaToken.decoder
          decode[StravaToken](content).toOption
        finally source.close()

  /**
   * In-memory token storage for testing
   */
  def inMemory[F[_]](using F: Sync[F]): F[TokenStorage[F]] = F.delay:
    var token: Option[StravaToken] = None

    new TokenStorage[F]:
      def save(t: StravaToken): F[Unit] = F.delay:
        token = Some(t)

      def load(): F[Option[StravaToken]] = F.delay(token)
