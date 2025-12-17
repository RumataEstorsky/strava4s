package strava.models

import io.circe.parser.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for route-related models
 * Tests JSON parsing from real Strava API responses
 */
class RouteSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "Route" should "parse get-route-by-id response" in {
    val json = loadJson("get-route-getroutebyid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val route = parsed.value.asObject.get
    
    // Verify core route fields
    route("id") shouldBe defined
    route("name") shouldBe defined
    route("description") shouldBe defined
    route("athlete") shouldBe defined
  }

  it should "contain distance and elevation data" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    route("distance") shouldBe defined
    route("elevation_gain") shouldBe defined
    route("estimated_moving_time") shouldBe defined
  }

  it should "contain route type and sub_type" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    route("type") shouldBe defined
    route("sub_type") shouldBe defined
  }

  it should "contain boolean flags" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    route("private") shouldBe defined
    route("starred") shouldBe defined
  }

  it should "contain timestamps" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    route("created_at") shouldBe defined
    route("updated_at") shouldBe defined
    route("timestamp") shouldBe defined
  }

  it should "contain map data" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    val map = route("map").flatMap(_.asObject)
    map shouldBe defined
    map.get("id") shouldBe defined
    map.get("polyline") shouldBe defined
    map.get("summary_polyline") shouldBe defined
  }

  it should "contain segments array" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    val segments = route("segments").flatMap(_.asArray)
    segments shouldBe defined
    
    segments.get.foreach { segment =>
      val obj = segment.asObject.get
      obj("id") shouldBe defined
      obj("name") shouldBe defined
      obj("activity_type") shouldBe defined
      obj("distance") shouldBe defined
      obj("average_grade") shouldBe defined
    }
  }

  it should "contain waypoints array" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    val waypoints = route("waypoints").flatMap(_.asArray)
    waypoints shouldBe defined
    
    waypoints.get.foreach { waypoint =>
      val obj = waypoint.asObject.get
      obj("latlng") shouldBe defined
      obj("target_latlng") shouldBe defined
      obj("categories") shouldBe defined
      obj("title") shouldBe defined
      obj("description") shouldBe defined
      obj("distance_into_route") shouldBe defined
    }
  }

  "List athlete routes" should "parse list-athlete-routes response" in {
    val json = loadJson("list-athlete-routes-getroutesbyathleteid.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val routes = parsed.value.asArray.get
    routes.foreach { route =>
      val obj = route.asObject.get
      obj("id") shouldBe defined
      obj("name") shouldBe defined
    }
  }

  "Route validation" should "ensure numeric fields are valid" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    if (route("distance").isDefined) {
      val distance = route("distance").flatMap(_.asNumber).map(_.toFloat).get
      distance should be >= 0f
    }
    
    if (route("elevation_gain").isDefined) {
      val elevGain = route("elevation_gain").flatMap(_.asNumber).map(_.toFloat).get
      elevGain should be >= 0f
    }
    
    if (route("estimated_moving_time").isDefined) {
      val time = route("estimated_moving_time").flatMap(_.asNumber).flatMap(_.toInt).get
      time should be >= 0
    }
  }

  it should "validate waypoint structure" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    val waypoints = route("waypoints").flatMap(_.asArray)
    if (waypoints.isDefined) {
      waypoints.get.foreach { waypoint =>
        val obj = waypoint.asObject.get
        
        val categories = obj("categories").flatMap(_.asArray)
        categories shouldBe defined
        
        obj("distance_into_route") shouldBe defined
      }
    }
  }

  it should "validate segment structure in route" in {
    val json = loadJson("get-route-getroutebyid.json")
    val parsed = parse(json).value
    val route = parsed.asObject.get
    
    val segments = route("segments").flatMap(_.asArray)
    if (segments.isDefined) {
      segments.get.foreach { segment =>
        val obj = segment.asObject.get
        
        // Validate required segment fields
        obj("id") shouldBe defined
        obj("name") shouldBe defined
        obj("distance") shouldBe defined
        
        // Validate athlete segment stats if present
        if (obj("athlete_segment_stats").isDefined) {
          val stats = obj("athlete_segment_stats").flatMap(_.asObject).get
          stats("id") shouldBe defined
          stats("elapsed_time") shouldBe defined
          stats("distance") shouldBe defined
        }
        
        // Validate athlete PR effort if present
        if (obj("athlete_pr_effort").isDefined) {
          val prEffort = obj("athlete_pr_effort").flatMap(_.asObject).get
          prEffort("pr_elapsed_time") shouldBe defined
          prEffort("pr_date") shouldBe defined
        }
      }
    }
  }
}

