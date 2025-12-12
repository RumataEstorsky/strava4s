package examples

import cats.effect.{ExitCode, IO, IOApp}
import strava.StravaClient
import strava.core.{StravaConfig, StravaError}

import java.io.File

/**
 * Example demonstrating the OAuth authentication flow
 * with comprehensive error handling and user guidance
 */
object AuthenticationExample extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    program.as(ExitCode.Success).handleErrorWith { error =>
      IO.println(s"Fatal error: ${error.getMessage}") >>
      IO.println("Please check your configuration and try again.").as(ExitCode.Error)
    }
  }

  def program: IO[Unit] = {
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )

    val tokenFile = new File("strava-token.json")
    val redirectUri = "http://localhost:8080/callback"

    for {
      _ <- IO.println("=== Strava OAuth Authentication ===\n")
      _ <- validateConfig(config)
      _ <- checkTokenFile(tokenFile)
      _ <- IO.println("")

      _ <- StravaClient.resource[IO](config, tokenFile).use { client =>
        IO.delay(tokenFile.exists()).flatMap { existingToken =>
          if (existingToken) {
            // Test existing token
            IO.println("Testing existing authentication token...\n") >>
            client.athletes.getLoggedInAthlete().flatMap {
              case Right(athlete) =>
                val name = s"${athlete.firstname.getOrElse("Unknown")} ${athlete.lastname.getOrElse("")}"
                IO.println(s"Successfully authenticated as: $name") >>
                IO.println(s"   Location: ${athlete.city.getOrElse("Unknown")}, ${athlete.country.getOrElse("Unknown")}") >>
                IO.println(s"   Premium: ${athlete.premium.getOrElse(false)}") >>
                IO.println("") >>
                IO.println("You're all set! Run BasicExample to use the API.")
              
              case Left(error) =>
                IO.println(s"Token validation failed: ${error.message}\n") >>
                handleAuthError(error) >>
                IO.println("") >>
                IO.println("Suggestion: Delete the token file and re-authenticate:") >>
                IO.println(s"  rm ${tokenFile.getAbsolutePath}")
            }
          } else {
            // Perform OAuth flow
            val authUrl = client.auth.authorizationUrl(
              redirectUri = redirectUri,
              scope = "read,activity:read_all,activity:write"
            )

            printAuthInstructions(authUrl, redirectUri) >>
            IO.print("Enter authorization code: ") >>
            IO.readLine.flatMap { code =>
              if (code.trim.isEmpty) {
                IO.println("") >>
                IO.println("No code provided. Exiting.") >>
                IO.println("   Run this example again when you have the authorization code.")
              } else {
                IO.println("") >>
                IO.println("Exchanging authorization code for access token...") >>
                client.auth.exchangeToken(code.trim, redirectUri).flatMap {
                  case Right(token) =>
                    val expiresInHours = token.expiresIn / 3600
                    IO.println("") >>
                    IO.println("Authentication successful!") >>
                    IO.println(s"   Token expires in: ${expiresInHours} hours") >>
                    IO.println(s"   Token saved to: ${tokenFile.getAbsolutePath}") >>
                    IO.println("") >>
                    IO.println("Next steps:") >>
                    IO.println("- Run BasicExample to explore the API") >>
                    IO.println("- Token will auto-refresh when it expires") >>
                    IO.println("- Keep your token file secure (it's in .gitignore)")
                  
                  case Left(error) =>
                    IO.println("") >>
                    handleAuthError(error)
                }
              }
            }
          }
        }
      }
    } yield ()
  }

  // Helper methods

  private def validateConfig(config: StravaConfig): IO[Unit] = {
    if (config.clientId == "your-client-id" || config.clientSecret == "your-client-secret") {
      IO.println("") >>
      IO.println("ERROR: Strava API credentials not configured!") >>
      IO.println("") >>
      IO.println("To use this example, you need to:") >>
      IO.println("1. Create a Strava API application at: https://www.strava.com/settings/api") >>
      IO.println("2. Set the following environment variables:") >>
      IO.println("   export STRAVA_CLIENT_ID='your-client-id'") >>
      IO.println("   export STRAVA_CLIENT_SECRET='your-client-secret'") >>
      IO.println("3. Run this example again") >>
      IO.println("") >>
      IO.raiseError(new IllegalArgumentException("Missing Strava API credentials"))
    } else {
      IO.println("Configuration validated\n")
    }
  }

  private def checkTokenFile(tokenFile: File): IO[Unit] = {
    IO.delay(tokenFile.exists()).flatMap { exists =>
      if (exists) {
        IO.println(s"Token file found at: ${tokenFile.getAbsolutePath}")
      } else {
        IO.println(s"Token file will be created at: ${tokenFile.getAbsolutePath}")
      }
    }
  }

  private def handleAuthError(error: StravaError): IO[Unit] = {
    error match {
      case StravaError.AuthenticationError(msg) =>
        IO.println(s"Authentication failed: $msg") >>
        IO.println("") >>
        IO.println("Common issues:") >>
        IO.println("- Invalid authorization code (codes expire quickly)") >>
        IO.println("- Incorrect client credentials") >>
        IO.println("- Network connectivity issues") >>
        IO.println("") >>
        IO.println("Please try again with a fresh authorization code.")

      case StravaError.NetworkError(msg, cause) =>
        IO.println(s"Network error: $msg") >>
        cause.fold(IO.unit)(t => IO.println(s"   Cause: ${t.getMessage}")) >>
        IO.println("") >>
        IO.println("Please check your internet connection and try again.")

      case StravaError.TokenExpiredError(msg) =>
        IO.println(s"Token expired: $msg") >>
        IO.println("") >>
        IO.println("Please delete the token file and re-authenticate:") >>
        IO.println(s"  rm strava-token.json")

      case other =>
        IO.println(s"Unexpected error: ${other.message}") >>
        IO.println("") >>
        IO.println("If this persists, please file an issue at:") >>
        IO.println("https://github.com/vsvechikhin/strava4s/issues")
    }
  }

  private def printAuthInstructions(authUrl: String, redirectUri: String): IO[Unit] = {
    IO.println("") >>
    IO.println("═══════════════════════════════════════════════════════════") >>
    IO.println("  Strava OAuth Authorization") >>
    IO.println("═══════════════════════════════════════════════════════════") >>
    IO.println("") >>
    IO.println("Follow these steps to authorize the application:") >>
    IO.println("") >>
    IO.println("1. Copy and visit this URL in your browser:") >>
    IO.println("") >>
    IO.println(s"   $authUrl") >>
    IO.println("") >>
    IO.println("2. Click 'Authorize' on the Strava page") >>
    IO.println("") >>
    IO.println("3. You'll be redirected to a URL that looks like:") >>
    IO.println(s"   $redirectUri?code=AUTHORIZATION_CODE&scope=...") >>
    IO.println("") >>
    IO.println("4. Copy the 'code' parameter from that URL") >>
    IO.println("") >>
    IO.println("5. Paste it below when prompted") >>
    IO.println("") >>
    IO.println("═══════════════════════════════════════════════════════════") >>
    IO.println("")
  }
}
