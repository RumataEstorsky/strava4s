package strava.models

import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for athlete-related models (DetailedAthlete, SummaryAthlete, MetaAthlete)
 * Tests JSON parsing from real Strava API responses
 */
class AthleteSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "DetailedAthlete" should "parse get-authenticated-athlete response" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val athlete = parsed.value.asObject.get
    
    // Verify essential athlete fields
    athlete("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(1234567890987654321L)
    athlete("username").flatMap(_.asString) shouldBe Some("marianne_t")
    athlete("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(3)
    
    // Verify personal information
    athlete("firstname").flatMap(_.asString) shouldBe Some("Marianne")
    athlete("lastname").flatMap(_.asString) shouldBe Some("Teutenberg")
    athlete("city").flatMap(_.asString) shouldBe Some("San Francisco")
    athlete("state").flatMap(_.asString) shouldBe Some("CA")
    athlete("country").flatMap(_.asString) shouldBe Some("US")
    athlete("sex").flatMap(_.asString) shouldBe Some("F")
    
    // Verify premium status
    athlete("premium").flatMap(_.asBoolean) shouldBe Some(true)
    
    // Verify timestamps
    athlete("created_at").flatMap(_.asString) shouldBe Some("2017-11-14T02:30:05Z")
    athlete("updated_at").flatMap(_.asString) shouldBe Some("2018-02-06T19:32:20Z")
    
    // Verify profile URLs
    athlete("profile_medium") shouldBe defined
    athlete("profile") shouldBe defined
    
    // Verify social counts
    athlete("follower_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(5)
    athlete("friend_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(5)
    athlete("mutual_friend_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(0)
    
    // Verify athlete type and preferences
    athlete("athlete_type").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1)
    athlete("date_preference").flatMap(_.asString) shouldBe Some("%m/%d/%Y")
    athlete("measurement_preference").flatMap(_.asString) shouldBe Some("feet")
  }

  it should "contain bikes array" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    val bikes = athlete("bikes").flatMap(_.asArray)
    bikes shouldBe defined
    bikes.get should have size 1
    
    val bike = bikes.get.head.asObject.get
    bike("id").flatMap(_.asString) shouldBe Some("b12345678987655")
    bike("primary").flatMap(_.asBoolean) shouldBe Some(true)
    bike("name").flatMap(_.asString) shouldBe Some("EMC")
    bike("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(2)
    bike("distance").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(0)
  }

  it should "contain shoes array" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    val shoes = athlete("shoes").flatMap(_.asArray)
    shoes shouldBe defined
    shoes.get should have size 1
    
    val shoe = shoes.get.head.asObject.get
    shoe("id").flatMap(_.asString) shouldBe Some("g12345678987655")
    shoe("primary").flatMap(_.asBoolean) shouldBe Some(true)
    shoe("name").flatMap(_.asString) shouldBe Some("adidas")
    shoe("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(2)
    shoe("distance").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(4904)
  }

  it should "have empty clubs array" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    val clubs = athlete("clubs").flatMap(_.asArray)
    clubs shouldBe defined
    clubs.get should have size 0
  }

  it should "handle null values correctly" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    athlete("friend").flatMap(_.asNull) shouldBe Some(())
    athlete("follower").flatMap(_.asNull) shouldBe Some(())
    athlete("ftp").flatMap(_.asNull) shouldBe Some(())
  }

  "SummaryAthlete" should "parse athlete references in activities" in {
    val json = loadJson("list-athlete-activities-getloggedinathleteactivities.json")
    val parsed = parse(json).value
    val activities = parsed.asArray.get
    
    activities.foreach { activity =>
      val obj = activity.asObject.get
      val athlete = obj("athlete").flatMap(_.asObject)
      
      athlete shouldBe defined
      athlete.get("id") shouldBe defined
      athlete.get("resource_state") shouldBe defined
    }
  }

  "MetaAthlete" should "parse athlete references in detailed activity" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val athlete = activity("athlete").flatMap(_.asObject)
    athlete shouldBe defined
    athlete.get("id") shouldBe defined
  }

  "Athlete update" should "parse update-athlete response" in {
    val json = loadJson("update-athlete-updateloggedinathlete.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val athlete = parsed.value.asObject.get
    athlete("id") shouldBe defined
    athlete("weight") shouldBe defined
  }

  "List athlete clubs" should "parse athlete clubs" in {
    val json = loadJson("list-athlete-clubs-getloggedinathleteclubs.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val clubs = parsed.value.asArray.get
    clubs.foreach { club =>
      val obj = club.asObject.get
      obj("id") shouldBe defined
      obj("name") shouldBe defined
      obj("resource_state") shouldBe defined
    }
  }

  "Athlete validation" should "ensure required fields are present" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    // Essential fields that must always be present
    val requiredFields = Seq("id", "username", "resource_state", "firstname", "lastname")
    requiredFields.foreach { field =>
      athlete(field) shouldBe defined
      withClue(s"Field '$field' should not be null:") {
        athlete(field).exists(!_.isNull) shouldBe true
      }
    }
  }

  it should "validate date formats" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    val createdAt = athlete("created_at").flatMap(_.asString).get
    createdAt should fullyMatch regex """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z"""
    
    val updatedAt = athlete("updated_at").flatMap(_.asString).get
    updatedAt should fullyMatch regex """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z"""
  }

  it should "validate numeric types" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    // ID should be a valid long
    val id = athlete("id").flatMap(_.asNumber).flatMap(_.toLong)
    id shouldBe defined
    id.get should be > 0L
    
    // Counts should be non-negative integers
    val followerCount = athlete("follower_count").flatMap(_.asNumber).flatMap(_.toInt).get
    followerCount should be >= 0
    
    val friendCount = athlete("friend_count").flatMap(_.asNumber).flatMap(_.toInt).get
    friendCount should be >= 0
  }

  it should "validate boolean fields" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    athlete("premium").flatMap(_.asBoolean) shouldBe defined
  }
}

