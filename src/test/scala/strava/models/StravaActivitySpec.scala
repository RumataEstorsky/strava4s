package strava.models

import io.circe.parser.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Tests for parsing activity JSON data
 */
class StravaActivitySpec extends AnyFlatSpec with Matchers with EitherValues {

  "Activity JSON" should "parse from resources/activities.json" in {
    val json = Source.fromResource("activities.json").mkString
    
    // Parse as generic JSON
    val parsed = parse(json)
    parsed .isRight shouldBe true
    
    val activities = parsed.value.asArray.get
    activities should have length 2

    // First activity
    val first = activities.head.asObject.get
    first("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(4500)
    first("moving_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(4500)
    first("name").flatMap(_.asString) shouldBe Some("Happy Friday")
    first("distance").flatMap(_.asNumber).map(_.toFloat) shouldBe defined

    // Second activity
    val second = activities(1).asObject.get
    second("elapsed_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(5400)
    second("moving_time").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(5400)
    second("name").flatMap(_.asString) shouldBe Some("Bondcliff")
    second("distance").flatMap(_.asNumber).map(_.toFloat) shouldBe defined
  }

  it should "contain valid activity fields" in {
    val json = Source.fromResource("activities.json").mkString
    val parsed = parse(json).value
    val activities = parsed.asArray.get
    
    activities.foreach { activity =>
      val obj = activity.asObject.get
      
      // Essential fields should be present
      obj("id") shouldBe defined
      obj("name") shouldBe defined
      obj("distance") shouldBe defined
      obj("moving_time") shouldBe defined
      obj("elapsed_time") shouldBe defined
    }
  }

  it should "contain resource_state field" in {
    val json = Source.fromResource("activities.json").mkString
    val parsed = parse(json).value
    val activities = parsed.asArray.get

    activities.foreach { activity =>
      val obj = activity.asObject.get
      obj("resource_state") shouldBe defined
      obj("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(2)
    }
  }

  it should "contain type and sport_type fields" in {
    val json = Source.fromResource("activities.json").mkString
    val parsed = parse(json).value
    val activities = parsed.asArray.get

    activities.foreach { activity =>
      val obj = activity.asObject.get
      obj("type").flatMap(_.asString) shouldBe Some("Ride")
      obj("sport_type").flatMap(_.asString) shouldBe Some("MountainBikeRide")
    }
  }

  it should "contain location fields" in {
    val json = Source.fromResource("activities.json").mkString
    val parsed = parse(json).value
    val activities = parsed.asArray.get

    activities.foreach { activity =>
      val obj = activity.asObject.get
      obj("utc_offset") shouldBe defined
      obj("location_city") shouldBe defined
      obj("location_state") shouldBe defined
      obj("location_country").flatMap(_.asString) shouldBe Some("United States")
    }
  }

  it should "contain heartrate and cadence fields" in {
    val json = Source.fromResource("activities.json").mkString
    val parsed = parse(json).value
    val activities = parsed.asArray.get

    activities.foreach { activity =>
      val obj = activity.asObject.get
      obj("has_heartrate").flatMap(_.asBoolean) shouldBe Some(true)
      obj("average_heartrate") shouldBe defined
      obj("max_heartrate") shouldBe defined
      obj("average_cadence") shouldBe defined
    }
  }

  it should "contain pr_count and suffer_score fields" in {
    val json = Source.fromResource("activities.json").mkString
    val parsed = parse(json).value
    val activities = parsed.asArray.get

    activities.foreach { activity =>
      val obj = activity.asObject.get
      obj("pr_count").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(0)
      obj("suffer_score") shouldBe defined
    }
  }

  it should "contain from_accepted_tag field" in {
    val json = Source.fromResource("activities.json").mkString
    val parsed = parse(json).value
    val activities = parsed.asArray.get

    activities.foreach { activity =>
      val obj = activity.asObject.get
      obj("from_accepted_tag").flatMap(_.asBoolean) shouldBe Some(false)
    }
  }
}
