package strava.models

import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Tests for parsing lap JSON data
 */
class LapSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Lap JSON" should "parse from resources/laps.json" in {
    val json = Source.fromResource("laps.json").mkString
    
    // Parse as generic JSON
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val laps = parsed.value.asArray.get
    laps should have size 1

    val lap = laps.head.asObject.get

    // Verify IDs
    lap("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(12345678987654321L)
    
    // Verify activity ID
    val activity = lap("activity").flatMap(_.asObject)
    activity shouldBe defined
    activity.get("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(12345678987654321L)
    
    // Verify athlete ID
    val athlete = lap("athlete").flatMap(_.asObject)
    athlete shouldBe defined
    athlete.get("id").flatMap(_.asNumber).flatMap(_.toLong) shouldBe Some(12345678987654321L)

    // Verify numeric values
    lap("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1691)
    lap("moving_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1587)
    lap("distance").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
    lap("total_elevation_gain").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
    lap("average_speed").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
    lap("max_speed").flatMap(_.asNumber).map(_.toFloat) shouldBe defined

    // Verify other fields
    lap("lap_index").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1)
    lap("split").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(1)
    lap("name").flatMap(_.asString) shouldBe Some("Lap 1")
    
    // Verify date field exists
    lap("start_date_local") shouldBe defined
  }

  it should "contain valid lap structure" in {
    val json = Source.fromResource("laps.json").mkString
    val parsed = parse(json).value
    val laps = parsed.asArray.get
    
    laps.foreach { lap =>
      val obj = lap.asObject.get
      
      // Essential fields should be present
      obj("id") shouldBe defined
      obj("name") shouldBe defined
      obj("elapsed_time") shouldBe defined
      obj("moving_time") shouldBe defined
      obj("distance") shouldBe defined
      obj("athlete") shouldBe defined
      obj("activity") shouldBe defined
    }
  }
}
