package strava.models

import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for segment-related models (DetailedSegment, SummarySegment, ExplorerSegment)
 * Tests JSON parsing from real Strava API responses
 */
class SegmentSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "DetailedSegment" should "parse get-segment-by-id response" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val segment = parsed.value.asObject.get
    
    // Verify core segment fields
    segment("id").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(229781)
    segment("name").flatMap(_.asString) shouldBe Some("Hawk Hill")
    segment("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(3)
    segment("activity_type").flatMap(_.asString) shouldBe Some("Ride")
  }

  it should "contain distance and elevation data" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    segment("distance").flatMap(_.asNumber).map(_.toFloat) shouldBe Some(2684.82f)
    segment("average_grade").flatMap(_.asNumber).map(_.toFloat) shouldBe Some(5.7f)
    segment("maximum_grade").flatMap(_.asNumber).map(_.toFloat) shouldBe Some(14.2f)
    segment("elevation_high").flatMap(_.asNumber).map(_.toFloat) shouldBe Some(245.3f)
    segment("elevation_low").flatMap(_.asNumber).map(_.toFloat) shouldBe Some(92.4f)
    segment("total_elevation_gain").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
  }

  it should "contain location coordinates" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    val startLatLng = segment("start_latlng").flatMap(_.asArray)
    startLatLng shouldBe defined
    startLatLng.get should have size 2
    
    val endLatLng = segment("end_latlng").flatMap(_.asArray)
    endLatLng shouldBe defined
    endLatLng.get should have size 2
  }

  it should "contain location information" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    segment("city").flatMap(_.asString) shouldBe Some("San Francisco")
    segment("state").flatMap(_.asString) shouldBe Some("CA")
    segment("country").flatMap(_.asString) shouldBe Some("United States")
  }

  it should "contain climb category" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    segment("climb_category").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1)
  }

  it should "contain boolean flags" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    segment("private").flatMap(_.asBoolean) shouldBe Some(false)
    segment("hazardous").flatMap(_.asBoolean) shouldBe Some(false)
    segment("starred").flatMap(_.asBoolean) shouldBe Some(false)
  }

  it should "contain timestamps" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    val createdAt = segment("created_at").flatMap(_.asString).get
    createdAt should fullyMatch regex """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z"""
    
    val updatedAt = segment("updated_at").flatMap(_.asString).get
    updatedAt should fullyMatch regex """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z"""
  }

  it should "contain map data" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    val map = segment("map").flatMap(_.asObject)
    map shouldBe defined
    map.get("id") shouldBe defined
    map.get("polyline") shouldBe defined
    map.get("resource_state") shouldBe defined
  }

  it should "contain effort counts" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    segment("effort_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(309974)
    segment("athlete_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(30623)
    segment("star_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(2428)
  }

  it should "contain athlete segment stats" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    val stats = segment("athlete_segment_stats").flatMap(_.asObject)
    stats shouldBe defined
    stats.get("pr_elapsed_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(553)
    stats.get("pr_date").flatMap(_.asString) shouldBe Some("1993-04-03")
    stats.get("effort_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(2)
  }

  "SegmentEffort" should "parse get-segment-effort-by-id response" in {
    val json = loadJson("get-segment-effort-getsegmenteffortbyid.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val effort = parsed.value.asObject.get
    effort("id") shouldBe defined
    effort("elapsed_time") shouldBe defined
    effort("moving_time") shouldBe defined
    effort("start_date") shouldBe defined
    effort("activity") shouldBe defined
    effort("athlete") shouldBe defined
    effort("segment") shouldBe defined
  }

  "List segment efforts" should "parse list-segment-efforts response" in {
    val json = loadJson("list-segment-efforts-geteffortsbysegmentid.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val efforts = parsed.value.asArray.get
    efforts.foreach { effort =>
      val obj = effort.asObject.get
      obj("id") shouldBe defined
      obj("elapsed_time") shouldBe defined
      obj("start_date") shouldBe defined
      obj("athlete") shouldBe defined
    }
  }

  "Explore segments" should "parse explore-segments response" in {
    val json = loadJson("explore-segments-exploresegments.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val response = parsed.value.asObject.get
    val segments = response("segments").flatMap(_.asArray)
    
    segments shouldBe defined
    segments.get.foreach { segment =>
      val obj = segment.asObject.get
      obj("id") shouldBe defined
      obj("name") shouldBe defined
      obj("climb_category") shouldBe defined
      obj("avg_grade") shouldBe defined
      obj("distance") shouldBe defined
    }
  }

  "Star segment" should "parse star-segment response" in {
    val json = loadJson("star-segment-starsegment.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val segment = parsed.value.asObject.get
    segment("id") shouldBe defined
    segment("starred") shouldBe defined
  }

  "Starred segments" should "parse list-starred-segments response" in {
    val json = loadJson("list-starred-segments-getloggedinathletestarredsegments.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    // This endpoint returns a single segment object, not an array
    val segment = parsed.value.asObject.get
    segment("id") shouldBe defined
    segment("name") shouldBe defined
    segment("starred") shouldBe defined
  }

  "Segment validation" should "ensure required fields are present" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    val requiredFields = Seq("id", "name", "resource_state", "activity_type", "distance")
    requiredFields.foreach { field =>
      withClue(s"Field '$field' should be defined:") {
        segment(field) shouldBe defined
      }
    }
  }

  it should "validate numeric ranges" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    val distance = segment("distance").flatMap(_.asNumber).map(_.toFloat).get
    distance should be > 0f
    
    val avgGrade = segment("average_grade").flatMap(_.asNumber).map(_.toFloat).get
    avgGrade should (be >= -100f and be <= 100f)
    
    val maxGrade = segment("maximum_grade").flatMap(_.asNumber).map(_.toFloat).get
    maxGrade should (be >= -100f and be <= 100f)
    
    val elevHigh = segment("elevation_high").flatMap(_.asNumber).map(_.toFloat).get
    val elevLow = segment("elevation_low").flatMap(_.asNumber).map(_.toFloat).get
    elevHigh should be >= elevLow
  }

  it should "validate climb categories" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    val climbCategory = segment("climb_category").flatMap(_.asNumber).flatMap(_.toInt).get
    climbCategory should (be >= 0 and be <= 5)
  }

  it should "validate coordinate arrays" in {
    val json = loadJson("get-segment-getsegmentbyid.json")
    val parsed = parse(json).value
    val segment = parsed.asObject.get
    
    val startLatLng = segment("start_latlng").flatMap(_.asArray).get
    val lat = startLatLng.head.asNumber.map(_.toFloat).get
    val lng = startLatLng(1).asNumber.map(_.toFloat).get
    
    lat should (be >= -90f and be <= 90f)
    lng should (be >= -180f and be <= 180f)
  }
}

