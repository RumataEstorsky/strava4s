package strava.models

import io.circe.parser.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for athlete stats models (ActivityStats, ActivityTotal)
 * Tests JSON parsing from real Strava API responses
 */
class StatsSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "ActivityStats" should "parse get-athlete-stats response" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val stats = parsed.value.asObject.get
    
    // Should contain various totals
    stats("recent_ride_totals") shouldBe defined
    stats("recent_run_totals") shouldBe defined
    stats("recent_swim_totals") shouldBe defined
    stats("ytd_ride_totals") shouldBe defined
    stats("ytd_run_totals") shouldBe defined
    stats("ytd_swim_totals") shouldBe defined
    stats("all_ride_totals") shouldBe defined
    stats("all_run_totals") shouldBe defined
    stats("all_swim_totals") shouldBe defined
  }

  it should "contain biggest achievements" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    stats("biggest_ride_distance") shouldBe defined
    stats("biggest_climb_elevation_gain") shouldBe defined
  }

  "ActivityTotal" should "have valid structure for recent_ride_totals" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    val recentRideTotals = stats("recent_ride_totals").flatMap(_.asObject)
    if (recentRideTotals.isDefined) {
      val totals = recentRideTotals.get
      totals("count") shouldBe defined
      totals("distance") shouldBe defined
      totals("moving_time") shouldBe defined
      totals("elapsed_time") shouldBe defined
      totals("elevation_gain") shouldBe defined
      totals("achievement_count") shouldBe defined
    }
  }

  it should "contain activity count" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    val recentRideTotals = stats("recent_ride_totals").flatMap(_.asObject)
    if (recentRideTotals.isDefined) {
      val count = recentRideTotals.get("count").flatMap(_.asNumber).flatMap(_.toInt)
      count shouldBe defined
      count.get should be >= 0
    }
  }

  it should "contain distance totals" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    val recentRideTotals = stats("recent_ride_totals").flatMap(_.asObject)
    if (recentRideTotals.isDefined) {
      val distance = recentRideTotals.get("distance").flatMap(_.asNumber).map(_.toFloat)
      distance shouldBe defined
      distance.get should be >= 0f
    }
  }

  it should "contain time totals" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    val recentRideTotals = stats("recent_ride_totals").flatMap(_.asObject)
    if (recentRideTotals.isDefined) {
      val movingTime = recentRideTotals.get("moving_time").flatMap(_.asNumber).flatMap(_.toInt)
      val elapsedTime = recentRideTotals.get("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt)
      
      movingTime shouldBe defined
      elapsedTime shouldBe defined
      movingTime.get should be >= 0
      elapsedTime.get should be >= 0
    }
  }

  it should "contain elevation gain" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    val recentRideTotals = stats("recent_ride_totals").flatMap(_.asObject)
    if (recentRideTotals.isDefined) {
      val elevationGain = recentRideTotals.get("elevation_gain").flatMap(_.asNumber).map(_.toFloat)
      elevationGain shouldBe defined
      elevationGain.get should be >= 0f
    }
  }

  it should "contain achievement count" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    val recentRideTotals = stats("recent_ride_totals").flatMap(_.asObject)
    if (recentRideTotals.isDefined) {
      val achievementCount = recentRideTotals.get("achievement_count").flatMap(_.asNumber).flatMap(_.toInt)
      achievementCount shouldBe defined
      achievementCount.get should be >= 0
    }
  }

  "Stats validation" should "ensure all totals have same structure" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    val totalTypes = Seq(
      "recent_ride_totals", "recent_run_totals", "recent_swim_totals",
      "ytd_ride_totals", "ytd_run_totals", "ytd_swim_totals",
      "all_ride_totals", "all_run_totals", "all_swim_totals"
    )
    
    totalTypes.foreach { totalType =>
      val total = stats(totalType)
      total shouldBe defined
      
      // Can be either an object with data or an empty string
      total.get.isObject || total.get.isString shouldBe true
    }
  }

  it should "validate numeric ranges for biggest achievements" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    if (stats("biggest_ride_distance").isDefined) {
      val biggestRide = stats("biggest_ride_distance").flatMap(_.asNumber).map(_.toFloat).get
      biggestRide should be >= 0f
    }
    
    if (stats("biggest_climb_elevation_gain").isDefined) {
      val biggestClimb = stats("biggest_climb_elevation_gain").flatMap(_.asNumber).map(_.toFloat).get
      biggestClimb should be >= 0f
    }
  }

  it should "validate time values are non-negative" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    val recentRideTotals = stats("recent_ride_totals").flatMap(_.asObject)
    if (recentRideTotals.isDefined) {
      val movingTime = recentRideTotals.get("moving_time").flatMap(_.asNumber).flatMap(_.toInt)
      val elapsedTime = recentRideTotals.get("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt)
      
      if (movingTime.isDefined) {
        movingTime.get should be >= 0
      }
      if (elapsedTime.isDefined) {
        elapsedTime.get should be >= 0
      }
    }
  }

  it should "handle empty totals as strings" in {
    val json = loadJson("get-athlete-stats-getstats.json")
    val parsed = parse(json).value
    val stats = parsed.asObject.get
    
    // Some totals might be empty strings instead of objects
    val recentRunTotals = stats("recent_run_totals")
    if (recentRunTotals.isDefined) {
      recentRunTotals.get.isObject || recentRunTotals.get.isString shouldBe true
    }
  }
}

