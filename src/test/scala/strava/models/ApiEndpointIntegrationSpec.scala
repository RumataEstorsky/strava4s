package strava.models

import io.circe.parser.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive integration tests for all API endpoint responses
 * This suite verifies that all JSON responses from Strava API endpoints can be parsed correctly
 */
class ApiEndpointIntegrationSpec extends AnyFlatSpec with Matchers with EitherValues:

  private def loadJson(filename: String): String =
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()

  "Activity endpoints" should "parse all activity-related responses" in {
    // List athlete activities
    val listActivities = loadJson("list-athlete-activities-getloggedinathleteactivities.json")
    val parsedList = parse(listActivities)
    parsedList.isRight shouldBe true
    parsedList.value.asArray shouldBe defined
    
    // Get activity by ID
    val getActivity = loadJson("get-activity-getactivitybyid.json")
    val parsedGet = parse(getActivity)
    parsedGet.isRight shouldBe true
    parsedGet.value.asObject shouldBe defined
    
    // Create activity
    val createActivity = loadJson("create-an-activity-createactivity.json")
    val parsedCreate = parse(createActivity)
    parsedCreate.isRight shouldBe true
    
    // Update activity
    val updateActivity = loadJson("update-activity-updateactivitybyid.json")
    val parsedUpdate = parse(updateActivity)
    parsedUpdate.isRight shouldBe true
  }

  "Athlete endpoints" should "parse all athlete-related responses" in {
    // Get authenticated athlete
    val getAthlete = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsedGet = parse(getAthlete)
    parsedGet.isRight shouldBe true
    parsedGet.value.asObject shouldBe defined
    
    // Update athlete
    val updateAthlete = loadJson("update-athlete-updateloggedinathlete.json")
    val parsedUpdate = parse(updateAthlete)
    parsedUpdate.isRight shouldBe true
    
    // Get athlete stats
    val getStats = loadJson("get-athlete-stats-getstats.json")
    val parsedStats = parse(getStats)
    parsedStats.isRight shouldBe true
    
    // List athlete clubs
    val listClubs = loadJson("list-athlete-clubs-getloggedinathleteclubs.json")
    val parsedClubs = parse(listClubs)
    parsedClubs.isRight shouldBe true
    parsedClubs.value.asArray shouldBe defined
    
    // List athlete routes
    val listRoutes = loadJson("list-athlete-routes-getroutesbyathleteid.json")
    val parsedRoutes = parse(listRoutes)
    parsedRoutes.isRight shouldBe true
    parsedRoutes.value.asArray shouldBe defined
  }

  "Club endpoints" should "parse all club-related responses" in {
    // Get club by ID
    val getClub = loadJson("get-club-getclubbyid.json")
    val parsedGet = parse(getClub)
    parsedGet.isRight shouldBe true
    parsedGet.value.asObject shouldBe defined
    
    // List club members
    val listMembers = loadJson("list-club-members-getclubmembersbyid.json")
    val parsedMembers = parse(listMembers)
    parsedMembers.isRight shouldBe true
    parsedMembers.value.asArray shouldBe defined
    
    // List club administrators
    val listAdmins = loadJson("list-club-administrators-getclubadminsbyid.json")
    val parsedAdmins = parse(listAdmins)
    parsedAdmins.isRight shouldBe true
    parsedAdmins.value.asArray shouldBe defined
    
    // List club activities
    val listActivities = loadJson("list-club-activities-getclubactivitiesbyid.json")
    val parsedActivities = parse(listActivities)
    parsedActivities.isRight shouldBe true
    parsedActivities.value.asArray shouldBe defined
  }

  "Segment endpoints" should "parse all segment-related responses" in {
    // Get segment by ID
    val getSegment = loadJson("get-segment-getsegmentbyid.json")
    val parsedGet = parse(getSegment)
    parsedGet.isRight shouldBe true
    parsedGet.value.asObject shouldBe defined
    
    // Explore segments
    val exploreSegments = loadJson("explore-segments-exploresegments.json")
    val parsedExplore = parse(exploreSegments)
    parsedExplore.isRight shouldBe true
    
    // List starred segments (returns object, not array)
    val listStarred = loadJson("list-starred-segments-getloggedinathletestarredsegments.json")
    val parsedStarred = parse(listStarred)
    parsedStarred.isRight shouldBe true
    parsedStarred.value.asObject shouldBe defined
    
    // Star segment
    val starSegment = loadJson("star-segment-starsegment.json")
    val parsedStar = parse(starSegment)
    parsedStar.isRight shouldBe true
  }

  "Segment effort endpoints" should "parse all segment effort responses" in {
    // Get segment effort by ID
    val getEffort = loadJson("get-segment-effort-getsegmenteffortbyid.json")
    val parsedGet = parse(getEffort)
    parsedGet.isRight shouldBe true
    parsedGet.value.asObject shouldBe defined
    
    // List segment efforts
    val listEfforts = loadJson("list-segment-efforts-geteffortsbysegmentid.json")
    val parsedList = parse(listEfforts)
    parsedList.isRight shouldBe true
    parsedList.value.asArray shouldBe defined
  }

  "Route endpoints" should "parse all route-related responses" in {
    // Get route by ID
    val getRoute = loadJson("get-route-getroutebyid.json")
    val parsedGet = parse(getRoute)
    parsedGet.isRight shouldBe true
    parsedGet.value.asObject shouldBe defined
  }

  "Stream endpoints" should "parse all stream responses" in {
    // Get activity streams
    val activityStreams = loadJson("get-activity-streams-getactivitystreams.json")
    val parsedActivity = parse(activityStreams)
    parsedActivity.isRight shouldBe true
    parsedActivity.value.asArray shouldBe defined
    
    // Get segment streams
    val segmentStreams = loadJson("get-segment-streams-getsegmentstreams.json")
    val parsedSegment = parse(segmentStreams)
    parsedSegment.isRight shouldBe true
    parsedSegment.value.asArray shouldBe defined
    
    // Get segment effort streams
    val effortStreams = loadJson("get-segment-effort-streams-getsegmenteffortstreams.json")
    val parsedEffort = parse(effortStreams)
    parsedEffort.isRight shouldBe true
    parsedEffort.value.asArray shouldBe defined
    
    // Get route streams
    val routeStreams = loadJson("get-route-streams-getroutestreams.json")
    val parsedRoute = parse(routeStreams)
    parsedRoute.isRight shouldBe true
    parsedRoute.value.asArray shouldBe defined
  }

  "Upload endpoints" should "parse all upload responses" in {
    // Create upload
    val createUpload = loadJson("upload-activity-createupload.json")
    val parsedCreate = parse(createUpload)
    parsedCreate.isRight shouldBe true
    
    // Get upload by ID
    val getUpload = loadJson("get-upload-getuploadbyid.json")
    val parsedGet = parse(getUpload)
    parsedGet.isRight shouldBe true
  }

  "Zone endpoints" should "parse all zone responses" in {
    // Get logged in athlete zones
    val getZones = loadJson("get-zones-getloggedinathletezones.json")
    val parsedZones = parse(getZones)
    parsedZones.isRight shouldBe true
    parsedZones.value.asArray shouldBe defined
    
    // Get activity zones
    val activityZones = loadJson("get-activity-zones-getzonesbyactivityid.json")
    val parsedActivity = parse(activityZones)
    parsedActivity.isRight shouldBe true
    parsedActivity.value.asArray shouldBe defined
  }

  "Gear endpoints" should "parse all gear responses" in {
    // Get equipment by ID
    val getGear = loadJson("get-equipment-getgearbyid.json")
    val parsedGet = parse(getGear)
    parsedGet.isRight shouldBe true
    parsedGet.value.asObject shouldBe defined
  }

  "Comment and kudos endpoints" should "parse social interaction responses" in {
    // List activity comments
    val listComments = loadJson("list-activity-comments-getcommentsbyactivityid.json")
    val parsedComments = parse(listComments)
    parsedComments.isRight shouldBe true
    parsedComments.value.asArray shouldBe defined
    
    // List activity kudoers
    val listKudoers = loadJson("list-activity-kudoers-getkudoersbyactivityid.json")
    val parsedKudoers = parse(listKudoers)
    parsedKudoers.isRight shouldBe true
    parsedKudoers.value.asArray shouldBe defined
  }

  "Lap endpoints" should "parse lap responses" in {
    // List activity laps
    val listLaps = loadJson("list-activity-laps-getlapsbyactivityid.json")
    val parsedLaps = parse(listLaps)
    parsedLaps.isRight shouldBe true
    parsedLaps.value.asArray shouldBe defined
  }

  "All JSON responses" should "be valid JSON" in {
    val allFiles = Seq(
      "create-an-activity-createactivity.json",
      "explore-segments-exploresegments.json",
      "get-activity-getactivitybyid.json",
      "get-activity-streams-getactivitystreams.json",
      "get-activity-zones-getzonesbyactivityid.json",
      "get-athlete-stats-getstats.json",
      "get-authenticated-athlete-getloggedinathlete.json",
      "get-club-getclubbyid.json",
      "get-equipment-getgearbyid.json",
      "get-route-getroutebyid.json",
      "get-route-streams-getroutestreams.json",
      "get-segment-effort-getsegmenteffortbyid.json",
      "get-segment-effort-streams-getsegmenteffortstreams.json",
      "get-segment-getsegmentbyid.json",
      "get-segment-streams-getsegmentstreams.json",
      "get-upload-getuploadbyid.json",
      "get-zones-getloggedinathletezones.json",
      "list-activity-comments-getcommentsbyactivityid.json",
      "list-activity-kudoers-getkudoersbyactivityid.json",
      "list-activity-laps-getlapsbyactivityid.json",
      "list-athlete-activities-getloggedinathleteactivities.json",
      "list-athlete-clubs-getloggedinathleteclubs.json",
      "list-athlete-routes-getroutesbyathleteid.json",
      "list-club-activities-getclubactivitiesbyid.json",
      "list-club-administrators-getclubadminsbyid.json",
      "list-club-members-getclubmembersbyid.json",
      "list-segment-efforts-geteffortsbysegmentid.json",
      "list-starred-segments-getloggedinathletestarredsegments.json",
      "star-segment-starsegment.json",
      "update-activity-updateactivitybyid.json",
      "update-athlete-updateloggedinathlete.json",
      "upload-activity-createupload.json"
    )
    
    allFiles.foreach { filename =>
      withClue(s"Parsing $filename: ") {
        val json = loadJson(filename)
        val parsed = parse(json)
        parsed.isRight shouldBe true
      }
    }
  }

  "Integration test" should "verify all JSON resources can be loaded" in {
    val allFiles = Seq(
      "create-an-activity-createactivity.json",
      "explore-segments-exploresegments.json",
      "get-activity-getactivitybyid.json",
      "get-activity-streams-getactivitystreams.json",
      "get-activity-zones-getzonesbyactivityid.json",
      "get-athlete-stats-getstats.json",
      "get-authenticated-athlete-getloggedinathlete.json",
      "get-club-getclubbyid.json",
      "get-equipment-getgearbyid.json",
      "get-route-getroutebyid.json",
      "get-route-streams-getroutestreams.json",
      "get-segment-effort-getsegmenteffortbyid.json",
      "get-segment-effort-streams-getsegmenteffortstreams.json",
      "get-segment-getsegmentbyid.json",
      "get-segment-streams-getsegmentstreams.json",
      "get-upload-getuploadbyid.json",
      "get-zones-getloggedinathletezones.json",
      "list-activity-comments-getcommentsbyactivityid.json",
      "list-activity-kudoers-getkudoersbyactivityid.json",
      "list-activity-laps-getlapsbyactivityid.json",
      "list-athlete-activities-getloggedinathleteactivities.json",
      "list-athlete-clubs-getloggedinathleteclubs.json",
      "list-athlete-routes-getroutesbyathleteid.json",
      "list-club-activities-getclubactivitiesbyid.json",
      "list-club-administrators-getclubadminsbyid.json",
      "list-club-members-getclubmembersbyid.json",
      "list-segment-efforts-geteffortsbysegmentid.json",
      "list-starred-segments-getloggedinathletestarredsegments.json",
      "star-segment-starsegment.json",
      "update-activity-updateactivitybyid.json",
      "update-athlete-updateloggedinathlete.json",
      "upload-activity-createupload.json"
    )
    
    allFiles.foreach { filename =>
      withClue(s"Loading $filename: ") {
        noException should be thrownBy {
          loadJson(filename)
        }
      }
    }
  }

  it should "verify consistent structure across list responses" in {
    // Most list endpoints should return arrays (except list-starred-segments which returns object)
    val listEndpoints = Map(
      "list-athlete-activities" -> "list-athlete-activities-getloggedinathleteactivities.json",
      "list-athlete-clubs" -> "list-athlete-clubs-getloggedinathleteclubs.json",
      "list-athlete-routes" -> "list-athlete-routes-getroutesbyathleteid.json",
      "list-club-activities" -> "list-club-activities-getclubactivitiesbyid.json",
      "list-club-administrators" -> "list-club-administrators-getclubadminsbyid.json",
      "list-club-members" -> "list-club-members-getclubmembersbyid.json",
      "list-segment-efforts" -> "list-segment-efforts-geteffortsbysegmentid.json",
      "list-activity-comments" -> "list-activity-comments-getcommentsbyactivityid.json",
      "list-activity-kudoers" -> "list-activity-kudoers-getkudoersbyactivityid.json",
      "list-activity-laps" -> "list-activity-laps-getlapsbyactivityid.json"
    )
    
    listEndpoints.foreach { case (name, filename) =>
      withClue(s"Checking $name: ") {
        val json = loadJson(filename)
        val parsed = parse(json).value
        parsed.isArray shouldBe true
      }
    }
  }

  it should "verify consistent structure across get responses" in {
    // All get endpoints should return objects
    val getEndpoints = Map(
      "get-activity" -> "get-activity-getactivitybyid.json",
      "get-authenticated-athlete" -> "get-authenticated-athlete-getloggedinathlete.json",
      "get-club" -> "get-club-getclubbyid.json",
      "get-equipment" -> "get-equipment-getgearbyid.json",
      "get-route" -> "get-route-getroutebyid.json",
      "get-segment" -> "get-segment-getsegmentbyid.json",
      "get-segment-effort" -> "get-segment-effort-getsegmenteffortbyid.json",
      "get-upload" -> "get-upload-getuploadbyid.json",
      "get-athlete-stats" -> "get-athlete-stats-getstats.json"
    )
    
    getEndpoints.foreach { case (name, filename) =>
      withClue(s"Checking $name: ") {
        val json = loadJson(filename)
        val parsed = parse(json).value
        parsed.isObject shouldBe true
      }
    }
  }

  it should "verify all resources have ID fields" in {
    val resourcesWithIds = Seq(
      ("activity", "get-activity-getactivitybyid.json"),
      ("athlete", "get-authenticated-athlete-getloggedinathlete.json"),
      ("club", "get-club-getclubbyid.json"),
      ("gear", "get-equipment-getgearbyid.json"),
      ("segment", "get-segment-getsegmentbyid.json"),
      ("segment-effort", "get-segment-effort-getsegmenteffortbyid.json"),
      ("upload", "get-upload-getuploadbyid.json")
    )
    
    resourcesWithIds.foreach { case (name, filename) =>
      withClue(s"Checking $name has ID: ") {
        val json = loadJson(filename)
        val parsed = parse(json).value
        val obj = parsed.asObject.get
        obj("id") shouldBe defined
      }
    }
  }
