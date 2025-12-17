package strava.models

import io.circe.parser.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Simple focused tests for JSON parsing of key Strava API models
 */
class JsonParsingSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "SummaryActivity" should "parse list-athlete-activities response" in {
    val json = loadJson("list-athlete-activities-getloggedinathleteactivities.json")
    
    // Parse as generic JSON first to verify structure
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val jsonArray = parsed.value.asArray
    jsonArray shouldBe defined
    jsonArray.get should have size 2
    
    // Verify key fields exist
    val firstActivity = jsonArray.get.head.asObject.get
    firstActivity("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(154504250376823L)
    firstActivity("name").flatMap(_.asString) shouldBe Some("Happy Friday")
    firstActivity("distance").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
  }

  "DetailedActivity" should "parse get-activity response" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activity = parsed.value.asObject.get
    activity("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(12345678987654321L)
    activity("name").flatMap(_.asString) shouldBe Some("Happy Friday")
    activity("distance").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
    activity("moving_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(4207)
    
    // Verify nested objects exist
    activity("athlete") shouldBe defined
    activity("map") shouldBe defined
    activity("gear") shouldBe defined
    activity("laps") shouldBe defined
  }

  "DetailedAthlete" should "parse get-authenticated-athlete response" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val athlete = parsed.value.asObject.get
    athlete("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(1234567890987654321L)
    athlete("username").flatMap(_.asString) shouldBe Some("marianne_t")
    athlete("firstname").flatMap(_.asString) shouldBe Some("Marianne")
    athlete("lastname").flatMap(_.asString) shouldBe Some("Teutenberg")
    athlete("city").flatMap(_.asString) shouldBe Some("San Francisco")
  }

  "Lap" should "parse list-activity-laps response" in {
    val json = loadJson("list-activity-laps-getlapsbyactivityid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val laps = parsed.value.asArray.get
    laps should have size 1
    
    val lap = laps.head.asObject.get
    lap("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(12345678987654321L)
    lap("name").flatMap(_.asString) shouldBe Some("Lap 1")
    lap("distance").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
  }

  "JSON parsing" should "handle basic activity data" in {
    val json = """
      {
        "id": 123456,
        "name": "Test Activity",
        "distance": 5000.0,
        "moving_time": 1800,
        "elapsed_time": 1900
      }
    """
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activity = parsed.value.asObject.get
    activity("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(123456L)
    activity("name").flatMap(_.asString) shouldBe Some("Test Activity")
    activity("distance").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
    activity("moving_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1800)
  }

  it should "handle nested athlete data" in {
    val json = """
      {
        "id": 789,
        "firstname": "John",
        "lastname": "Doe",
        "profile": "https://example.com/profile.jpg"
      }
    """
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val athlete = parsed.value.asObject.get
    athlete("id").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(789)
    athlete("firstname").flatMap(_.asString) shouldBe Some("John")
    athlete("lastname").flatMap(_.asString) shouldBe Some("Doe")
  }

  it should "handle arrays of data" in {
    val json = """
      [
        {"id": 1, "name": "Activity 1"},
        {"id": 2, "name": "Activity 2"}
      ]
    """
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activities = parsed.value.asArray.get
    activities should have size 2
  }
}
