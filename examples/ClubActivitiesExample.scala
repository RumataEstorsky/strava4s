package examples

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import strava.StravaClient
import strava.core.StravaConfig
import java.io.File

/**
 * Work with Strava clubs - view members, activities, and club statistics.
 * Great for club managers and members who want to track club engagement.
 */
object ClubActivitiesExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config = StravaConfig(
      clientId = sys.env.getOrElse("STRAVA_CLIENT_ID", "your-client-id"),
      clientSecret = sys.env.getOrElse("STRAVA_CLIENT_SECRET", "your-client-secret")
    )
    val tokenFile = new File("strava-token.json")

    StravaClient.resource[IO](config, tokenFile).use { client =>
      for {
        _ <- IO.println("=== Club Activities ===\n")
        
        // Get your clubs
        _ <- IO.println("Your Clubs:\n")
        clubsResult <- client.clubs.getLoggedInAthleteClubs(perPage = 30)
        
        _ <- clubsResult match {
          case Right(clubs) if clubs.nonEmpty =>
            for {
              _ <- clubs.zipWithIndex.traverse_ { case (club, idx) =>
                IO.println(f"${idx + 1}%2d. ${club.name.getOrElse("Unnamed Club")}") >>
                IO.println(f"    Members: ${club.member_count.getOrElse(0)}, Type: ${club.sport_type.getOrElse("?")}")
              }
              _ <- IO.println("")
              
              // Get details and activities for first club
              _ <- clubs.headOption.flatMap(_.id).traverse_ { clubId =>
                for {
                  _ <- IO.println(s"Club Details (${clubs.head.name.getOrElse("Club")}):\n")
                  
                  // Get club details
                  detailsResult <- client.clubs.getClubById(clubId)
                  _ <- detailsResult match {
                    case Right(club) =>
                      IO.println(s"  Name: ${club.name.getOrElse("Unknown")}") >>
                      IO.println(s"  Members: ${club.member_count.getOrElse(0)}") >>
                      IO.println(s"  Sport type: ${club.sport_type.getOrElse("Unknown")}") >>
                      IO.println(s"  City: ${club.city.getOrElse("Unknown")}") >>
                      IO.println(s"  State: ${club.state.getOrElse("Unknown")}") >>
                      IO.println(s"  Country: ${club.country.getOrElse("Unknown")}") >>
                      IO.println("")
                      
                    case Left(error) =>
                      IO.println(s"  Error getting details: ${error.message}\n")
                  }
                  
                  // Get recent club activities
                  _ <- IO.println("Recent Club Activities:\n")
                  activitiesResult <- client.clubs.getClubActivitiesById(clubId, perPage = 20)
                  
                  _ <- activitiesResult match {
                    case Right(activities) if activities.nonEmpty =>
                      activities.take(15).zipWithIndex.traverse_ { case (activity, idx) =>
                        val athleteName = activity.athlete
                          .flatMap(a => a.firstname.map(f => s"${f} ${a.lastname.getOrElse("")}"))
                          .getOrElse("Unknown")
                        val name = activity.name.getOrElse("Unnamed")
                        val distance = f"${activity.distance.getOrElse(0f) / 1000}%.1f km"
                        val actType = activity.`type`.map(_.toString).getOrElse("?")
                        
                        IO.println(f"${idx + 1}%2d. $athleteName: $name") >>
                        IO.println(f"    $distance ($actType)")
                      } >>
                      IO.println("")
                      
                    case Right(_) =>
                      IO.println("  No recent activities\n")
                      
                    case Left(error) =>
                      IO.println(s"  Error: ${error.message}\n")
                  }
                  
                  // Get club members
                  _ <- IO.println("👥 Club Members:\n")
                  membersResult <- client.clubs.getClubMembersById(clubId, perPage = 30)
                  
                  _ <- membersResult match {
                    case Right(members) if members.nonEmpty =>
                      IO.println(f"  Total members shown: ${members.size}\n") >>
                      members.take(10).traverse_ { member =>
                        val name = s"${member.firstname.getOrElse("Unknown")} ${member.lastname.getOrElse("")}"
                        val city = member.city.getOrElse("Unknown")
                        IO.println(f"  • $name from $city")
                      } >>
                      (if (members.size > 10) IO.println(s"\n  ... and ${members.size - 10} more") else IO.unit) >>
                      IO.println("")
                      
                    case Right(_) =>
                      IO.println("  No members found\n")
                      
                    case Left(error) =>
                      IO.println(s"  Error: ${error.message}\n")
                  }
                  
                  // Get club admins
                  adminsResult <- client.clubs.getClubAdminsById(clubId, perPage = 10)
                  _ <- adminsResult match {
                    case Right(admins) if admins.nonEmpty =>
                      IO.println("👑 Club Admins:\n") >>
                      admins.traverse_ { admin =>
                        val name = s"${admin.firstname.getOrElse("Unknown")} ${admin.lastname.getOrElse("")}"
                        IO.println(f"  • $name")
                      } >>
                      IO.println("")
                      
                    case _ => IO.unit
                  }
                  
                } yield ()
              }
            } yield ()
            
          case Right(_) =>
            IO.println("  You're not a member of any clubs yet.\n") >>
            IO.println("  Tip: Join a club on Strava to see club activities!")
            
          case Left(error) =>
            IO.println(s"  Error: ${error.message}")
        }
      } yield ()
    }.as(ExitCode.Success)
  }
}

