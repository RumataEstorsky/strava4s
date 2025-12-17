package strava.models

import io.circe.parser.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for club-related models (DetailedClub, SummaryClub, MetaClub)
 * Tests JSON parsing from real Strava API responses
 */
class ClubSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "DetailedClub" should "parse get-club-by-id response" in {
    val json = loadJson("get-club-getclubbyid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val club = parsed.value.asObject.get
    
    // Verify core club fields
    club("id").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1)
    club("name").flatMap(_.asString) shouldBe Some("Team Strava Cycling")
    club("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(3)
    
    // Verify profile images
    club("profile_medium").flatMap(_.asString) shouldBe defined
    club("profile").flatMap(_.asString) shouldBe defined
    club("cover_photo").flatMap(_.asString) shouldBe defined
    club("cover_photo_small").flatMap(_.asString) shouldBe defined
  }

  it should "contain sport and activity types" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    club("sport_type").flatMap(_.asString) shouldBe Some("cycling")
    
    val activityTypes = club("activity_types").flatMap(_.asArray)
    activityTypes shouldBe defined
    activityTypes.get should not be empty
    
    val expectedTypes = Seq("Ride", "VirtualRide", "EBikeRide", "Velomobile", "Handcycle")
    activityTypes.get.map(_.asString.get) should contain allElementsOf expectedTypes
  }

  it should "contain location information" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    club("city").flatMap(_.asString) shouldBe Some("San Francisco")
    club("state").flatMap(_.asString) shouldBe Some("California")
    club("country").flatMap(_.asString) shouldBe Some("United States")
  }

  it should "contain membership information" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    club("private").flatMap(_.asBoolean) shouldBe Some(true)
    club("member_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(116)
    club("membership").flatMap(_.asString) shouldBe Some("member")
    club("admin").flatMap(_.asBoolean) shouldBe Some(false)
    club("owner").flatMap(_.asBoolean) shouldBe Some(false)
  }

  it should "contain club metadata" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    club("featured").flatMap(_.asBoolean) shouldBe Some(false)
    club("verified").flatMap(_.asBoolean) shouldBe Some(false)
    club("url").flatMap(_.asString) shouldBe Some("team-strava-bike")
    club("description").flatMap(_.asString) shouldBe Some("Private club for Cyclists who work at Strava.")
    club("club_type").flatMap(_.asString) shouldBe Some("company")
  }

  it should "contain counts" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    club("post_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(29)
    club("owner_id").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(759)
    club("following_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(107)
  }

  "SummaryClub" should "parse list-athlete-clubs response" in {
    val json = loadJson("list-athlete-clubs-getloggedinathleteclubs.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val clubs = parsed.value.asArray.get
    clubs.foreach { club =>
      val obj = club.asObject.get
      obj("id") shouldBe defined
      obj("name") shouldBe defined
      obj("resource_state") shouldBe defined
    }
  }

  "Club activities" should "parse list-club-activities response" in {
    val json = loadJson("list-club-activities-getclubactivitiesbyid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activities = parsed.value.asArray.get
    activities.foreach { activity =>
      val obj = activity.asObject.get
      obj("name") shouldBe defined
      obj("distance") shouldBe defined
      obj("moving_time") shouldBe defined
      obj("type") shouldBe defined
      obj("athlete") shouldBe defined
    }
  }

  "Club administrators" should "parse list-club-administrators response" in {
    val json = loadJson("list-club-administrators-getclubadminsbyid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val admins = parsed.value.asArray.get
    admins.foreach { admin =>
      val obj = admin.asObject.get
      obj("resource_state") shouldBe defined
      obj("firstname") shouldBe defined
      obj("lastname") shouldBe defined
    }
  }

  "Club members" should "parse list-club-members response" in {
    val json = loadJson("list-club-members-getclubmembersbyid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val members = parsed.value.asArray.get
    members.foreach { member =>
      val obj = member.asObject.get
      obj("resource_state") shouldBe defined
      obj("firstname") shouldBe defined
      obj("lastname") shouldBe defined
    }
  }

  "Club validation" should "ensure required fields are present" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    val requiredFields = Seq("id", "name", "resource_state", "sport_type")
    requiredFields.foreach { field =>
      withClue(s"Field '$field' should be defined:") {
        club(field) shouldBe defined
      }
    }
  }

  it should "validate numeric types" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    val memberId = club("id").flatMap(_.asNumber).flatMap(_.toInt).get
    memberId should be > 0
    
    val memberCount = club("member_count").flatMap(_.asNumber).flatMap(_.toInt).get
    memberCount should be >= 0
    
    val postCount = club("post_count").flatMap(_.asNumber).flatMap(_.toInt).get
    postCount should be >= 0
  }

  it should "validate boolean fields" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    club("private").flatMap(_.asBoolean) shouldBe defined
    club("featured").flatMap(_.asBoolean) shouldBe defined
    club("verified").flatMap(_.asBoolean) shouldBe defined
    club("admin").flatMap(_.asBoolean) shouldBe defined
    club("owner").flatMap(_.asBoolean) shouldBe defined
  }

  it should "validate club types" in {
    val json = loadJson("get-club-getclubbyid.json")
    val parsed = parse(json).value
    val club = parsed.asObject.get
    
    val clubType = club("club_type").flatMap(_.asString).get
    clubType should (be("company") or be("casual_club") or be("racing_team") or be("shop") or be("other"))
  }
}

