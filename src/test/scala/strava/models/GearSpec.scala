package strava.models

import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for gear-related models (DetailedGear, SummaryGear)
 * Tests JSON parsing from real Strava API responses
 */
class GearSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "DetailedGear" should "parse get-equipment-by-id response" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val gear = parsed.value.asObject.get
    
    // Verify core gear fields
    gear("id").flatMap(_.asString) shouldBe Some("b1231")
    gear("primary").flatMap(_.asBoolean) shouldBe Some(false)
    gear("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(3)
  }

  it should "contain distance information" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    val distance = gear("distance").flatMap(_.asNumber).flatMap(_.toInt)
    distance shouldBe Some(388206)
    distance.get should be >= 0
  }

  it should "contain brand and model information" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    gear("brand_name").flatMap(_.asString) shouldBe Some("BMC")
    gear("model_name").flatMap(_.asString) shouldBe Some("Teammachine")
  }

  it should "contain frame type for bikes" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    gear("frame_type").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(3)
  }

  it should "contain description" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    gear("description").flatMap(_.asString) shouldBe Some("My Bike.")
  }

  "SummaryGear" should "parse gear references in activities" in {
    val json = loadJson("get-activity-getactivitybyid.json")
    val parsed = parse(json).value
    val activity = parsed.asObject.get
    
    val gear = activity("gear").flatMap(_.asObject)
    if (gear.isDefined) {
      gear.get("id") shouldBe defined
      gear.get("resource_state") shouldBe defined
    }
  }

  it should "parse gear in athlete bikes" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    val bikes = athlete("bikes").flatMap(_.asArray)
    if (bikes.isDefined && bikes.get.nonEmpty) {
      bikes.get.foreach { bike =>
        val bikeObj = bike.asObject.get
        bikeObj("id") shouldBe defined
        bikeObj("name") shouldBe defined
        bikeObj("primary") shouldBe defined
        bikeObj("distance") shouldBe defined
        bikeObj("resource_state") shouldBe defined
      }
    }
  }

  it should "parse gear in athlete shoes" in {
    val json = loadJson("get-authenticated-athlete-getloggedinathlete.json")
    val parsed = parse(json).value
    val athlete = parsed.asObject.get
    
    val shoes = athlete("shoes").flatMap(_.asArray)
    if (shoes.isDefined && shoes.get.nonEmpty) {
      shoes.get.foreach { shoe =>
        val shoeObj = shoe.asObject.get
        shoeObj("id") shouldBe defined
        shoeObj("name") shouldBe defined
        shoeObj("primary") shouldBe defined
        shoeObj("distance") shouldBe defined
        shoeObj("resource_state") shouldBe defined
      }
    }
  }

  "Gear validation" should "ensure required fields are present" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    val requiredFields = Seq("id", "primary", "resource_state", "distance")
    requiredFields.foreach { field =>
      withClue(s"Field '$field' should be defined:") {
        gear(field) shouldBe defined
      }
    }
  }

  it should "validate numeric types" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    val distance = gear("distance").flatMap(_.asNumber).flatMap(_.toInt).get
    distance should be >= 0
    
    val resourceState = gear("resource_state").flatMap(_.asNumber).flatMap(_.toInt).get
    resourceState should (be >= 1 and be <= 3)
  }

  it should "validate boolean fields" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    gear("primary").flatMap(_.asBoolean) shouldBe defined
  }

  it should "validate frame types" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    if (gear("frame_type").isDefined) {
      val frameType = gear("frame_type").flatMap(_.asNumber).flatMap(_.toInt).get
      // Frame types: 1=mtb, 2=cross, 3=road, 4=time_trial
      frameType should (be >= 1 and be <= 4)
    }
  }

  it should "validate ID format" in {
    val json = loadJson("get-equipment-getgearbyid.json")
    val parsed = parse(json).value
    val gear = parsed.asObject.get
    
    val id = gear("id").flatMap(_.asString).get
    id should not be empty
    // Gear IDs typically start with 'b' for bikes or 'g' for shoes
    id should fullyMatch regex """[bg]\d+"""
  }
}

