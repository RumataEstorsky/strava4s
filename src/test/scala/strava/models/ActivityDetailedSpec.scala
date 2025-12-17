package strava.models

import io.circe.parser.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for DetailedActivity model
 * Tests complex activity JSON parsing from real Strava API responses
 */
class ActivityDetailedSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "DetailedActivity" should "parse get-activity-by-id response" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activity = parsed.value.asObject.get
    
    // Verify core activity fields
    activity("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(12345678987654321L)
    activity("name").flatMap(_.asString) shouldBe Some("Happy Friday")
    activity("distance").flatMap(_.asNumber) shouldBe defined
    activity("moving_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(4207)
    activity("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(4410)
    
    // Verify activity type
    activity("type").flatMap(_.asString) shouldBe Some("Ride")
    activity("sport_type").flatMap(_.asString) shouldBe Some("MountainBikeRide")
    
    // Verify resource state
    activity("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(3)
  }

  it should "contain athlete reference" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val athlete = activity("athlete").flatMap(_.asObject)
    athlete shouldBe defined
    athlete.get("id") shouldBe defined
  }

  it should "contain map data" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val map = activity("map").flatMap(_.asObject)
    map shouldBe defined
    map.get("id") shouldBe defined
    map.get("summary_polyline") shouldBe defined
    map.get("resource_state") shouldBe defined
  }

  it should "contain gear information" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val gear = activity("gear").flatMap(_.asObject)
    gear shouldBe defined
    gear.get("id") shouldBe defined
  }

  it should "contain laps array" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val laps = activity("laps").flatMap(_.asArray)
    laps shouldBe defined
    
    laps.get.foreach { lap =>
      val lapObj = lap.asObject.get
      lapObj("id") shouldBe defined
      lapObj("name") shouldBe defined
      lapObj("elapsed_time") shouldBe defined
      lapObj("moving_time") shouldBe defined
      lapObj("distance") shouldBe defined
    }
  }

  it should "contain speed metrics" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    activity("average_speed") shouldBe defined
    activity("max_speed") shouldBe defined
  }

  it should "contain elevation data" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    activity("total_elevation_gain") shouldBe defined
    activity("elev_high") shouldBe defined
    activity("elev_low") shouldBe defined
  }

  it should "contain timestamps" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    activity("start_date") shouldBe defined
    activity("start_date_local") shouldBe defined
    activity("timezone") shouldBe defined
    activity("utc_offset") shouldBe defined
  }

  it should "contain achievement and count fields" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    activity("achievement_count") shouldBe defined
    activity("kudos_count") shouldBe defined
    activity("comment_count") shouldBe defined
    activity("athlete_count") shouldBe defined
    activity("photo_count") shouldBe defined
  }

  it should "contain boolean flags" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    activity("trainer") shouldBe defined
    activity("commute") shouldBe defined
    activity("manual") shouldBe defined
    activity("private") shouldBe defined
    activity("flagged") shouldBe defined
    activity("has_kudoed") shouldBe defined
    activity("from_accepted_tag") shouldBe defined
  }

  it should "contain heartrate and temperature fields" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get

    activity("has_heartrate") shouldBe defined
    activity("average_temp") shouldBe defined
  }

  it should "contain pr_count and suffer_score fields" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get

    activity("pr_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(0)
    activity("suffer_score") shouldBe defined
  }

  it should "contain leaderboard opt-out flags" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get

    activity("segment_leaderboard_opt_out") shouldBe defined
    activity("leaderboard_opt_out") shouldBe defined
  }

  it should "contain location information" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    activity("start_latlng") shouldBe defined
    activity("end_latlng") shouldBe defined
  }

  "Activity creation" should "parse create-activity response" in {
    val json = loadJson("create-an-activity-createactivity.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activity = parsed.value.asObject.get
    activity("id") shouldBe defined
    activity("name") shouldBe defined
    activity("distance") shouldBe defined
    activity("moving_time") shouldBe defined
    activity("elapsed_time") shouldBe defined
  }

  "Activity update" should "parse update-activity response" in {
    val json = loadJson("update-activity-updateactivitybyid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activity = parsed.value.asObject.get
    activity("id") shouldBe defined
    activity("name") shouldBe defined
  }

  "SummaryActivity" should "parse list-athlete-activities response" in {
    val json = loadJson("list-athlete-activities-getloggedinathleteactivities.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activities = parsed.value.asArray.get
    activities should have size 2
    
    // Verify first activity
    val firstActivity = activities.head.asObject.get
    firstActivity("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(154504250376823L)
    firstActivity("name").flatMap(_.asString) shouldBe Some("Happy Friday")
    firstActivity("distance").flatMap(_.asNumber) shouldBe defined
    firstActivity("moving_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(4500)
    firstActivity("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(4500)
    firstActivity("type").flatMap(_.asString) shouldBe Some("Ride")
    
    // Verify second activity
    val secondActivity = activities(1).asObject.get
    secondActivity("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(1234567809L)
    secondActivity("name").flatMap(_.asString) shouldBe Some("Bondcliff")
  }

  it should "contain all activities have required fields" in {
    val json = loadJson("list-athlete-activities-getloggedinathleteactivities.json")
    val parsed = parse(json).value
    val activities = parsed.asArray.get
    
    activities.foreach { activity =>
      val obj = activity.asObject.get
      
      // Verify essential fields
      obj("id") shouldBe defined
      obj("name") shouldBe defined
      obj("distance") shouldBe defined
      obj("moving_time") shouldBe defined
      obj("elapsed_time") shouldBe defined
      obj("type") shouldBe defined
      obj("start_date") shouldBe defined
      obj("athlete") shouldBe defined
    }
  }

  it should "contain new SummaryActivity fields" in {
    val json = loadJson("list-athlete-activities-getloggedinathleteactivities.json")
    val parsed = parse(json).value
    val activities = parsed.asArray.get

    activities.foreach { activity =>
      val obj = activity.asObject.get

      // New fields added in v1.1.0
      obj("resource_state") shouldBe defined
      obj("sport_type") shouldBe defined
      obj("utc_offset") shouldBe defined
      obj("location_country") shouldBe defined
      obj("from_accepted_tag") shouldBe defined
      obj("average_cadence") shouldBe defined
      obj("has_heartrate") shouldBe defined
      obj("average_heartrate") shouldBe defined
      obj("max_heartrate") shouldBe defined
      obj("pr_count") shouldBe defined
      obj("suffer_score") shouldBe defined
    }
  }

  "Activity validation" should "ensure numeric fields are valid" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    // Distance should be non-negative
    val distance = activity("distance").flatMap(_.asNumber).map(_.toFloat).get
    distance should be >= 0f
    
    // Times should be positive
    val movingTime = activity("moving_time").flatMap(_.asNumber).flatMap(_.toInt).get
    movingTime should be > 0
    
    val elapsedTime = activity("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt).get
    elapsedTime should be > 0
    
    // Elapsed time should be >= moving time
    elapsedTime should be >= movingTime
  }

  it should "validate speed calculations" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val avgSpeed = activity("average_speed").flatMap(_.asNumber).map(_.toFloat).get
    val maxSpeed = activity("max_speed").flatMap(_.asNumber).map(_.toFloat).get
    
    // Speeds should be non-negative
    avgSpeed should be >= 0f
    maxSpeed should be >= 0f
    
    // Max speed should be >= average speed
    maxSpeed should be >= avgSpeed
  }

  it should "validate date format" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val startDate = activity("start_date").flatMap(_.asString).get
    startDate should fullyMatch regex """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z"""
    
    val startDateLocal = activity("start_date_local").flatMap(_.asString).get
    startDateLocal should fullyMatch regex """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z"""
  }

  it should "validate coordinate arrays" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val startLatLng = activity("start_latlng").flatMap(_.asArray)
    if (startLatLng.isDefined && startLatLng.get.nonEmpty) {
      startLatLng.get should have size 2
      val lat = startLatLng.get.head.asNumber.map(_.toFloat).get
      val lng = startLatLng.get(1).asNumber.map(_.toFloat).get
      
      // Latitude range: -90 to 90
      lat should (be >= -90f and be <= 90f)
      // Longitude range: -180 to 180
      lng should (be >= -180f and be <= 180f)
    }
  }

  "Activity comments" should "parse list-activity-comments response" in {
    val json = loadJson("list-activity-comments-getcommentsbyactivityid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val comments = parsed.value.asArray.get
    comments.foreach { comment =>
      val obj = comment.asObject.get
      obj("id") shouldBe defined
      obj("activity_id") shouldBe defined
      obj("text") shouldBe defined
      obj("athlete") shouldBe defined
      obj("created_at") shouldBe defined
    }
  }

  "Activity kudoers" should "parse list-activity-kudoers response" in {
    val json = loadJson("list-activity-kudoers-getkudoersbyactivityid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val kudoers = parsed.value.asArray.get
    kudoers.foreach { athlete =>
      val obj = athlete.asObject.get
      obj("firstname") shouldBe defined
      obj("lastname") shouldBe defined
    }
  }

  "Activity laps" should "parse list-activity-laps response" in {
    val json = loadJson("list-activity-laps-getlapsbyactivityid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val laps = parsed.value.asArray.get
    laps should have size 1
    
    val lap = laps.head.asObject.get
    lap("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(12345678987654321L)
    lap("name").flatMap(_.asString) shouldBe Some("Lap 1")
    lap("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1691)
    lap("moving_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1587)
    lap("distance") shouldBe defined
    lap("average_speed") shouldBe defined
    lap("max_speed") shouldBe defined
    lap("lap_index").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1)
  }

  "Activity zones" should "parse get-zones-by-activity-id response" in {
    val json = loadJson("get-activity-zones-getzonesbyactivityid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val zones = parsed.value.asArray.get
    zones.foreach { zone =>
      val obj = zone.asObject.get
      obj("type") shouldBe defined
      obj("distribution_buckets") shouldBe defined
    }
  }
}

