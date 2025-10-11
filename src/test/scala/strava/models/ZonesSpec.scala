package strava.models

import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for zone-related models (Zones, HeartRateZoneRanges, PowerZoneRanges)
 * Tests JSON parsing from real Strava API responses
 */
class ZonesSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "Zones" should "parse get-logged-in-athlete-zones response" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val zones = parsed.value.asArray.get
    zones should not be empty
  }

  it should "contain valid zone types" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    val validZoneTypes = Seq("heartrate", "power")
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      val zoneType = obj("type").flatMap(_.asString).get
      
      withClue(s"Zone type '$zoneType' should be valid:") {
        validZoneTypes should contain(zoneType)
      }
    }
  }

  it should "contain distribution buckets" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      val buckets = obj("distribution_buckets").flatMap(_.asArray)
      
      buckets shouldBe defined
      buckets.get should not be empty
    }
  }

  it should "contain resource_state" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      obj("resource_state").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(3)
    }
  }

  it should "contain sensor_based flag" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      obj("sensor_based") shouldBe defined
    }
  }

  "Distribution buckets" should "have valid structure" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      val buckets = obj("distribution_buckets").flatMap(_.asArray).get
      
      buckets.foreach { bucket =>
        val bucketObj = bucket.asObject.get
        bucketObj("min") shouldBe defined
        bucketObj("max") shouldBe defined
        bucketObj("time") shouldBe defined
      }
    }
  }

  it should "have valid min/max ranges" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      val buckets = obj("distribution_buckets").flatMap(_.asArray).get
      
      buckets.foreach { bucket =>
        val bucketObj = bucket.asObject.get
        val min = bucketObj("min").flatMap(_.asNumber).flatMap(_.toInt).get
        val max = bucketObj("max").flatMap(_.asNumber).flatMap(_.toInt).get
        
        // Min should be less than max (except when they're equal for first bucket or max is -1 for last)
        if (max != -1 && min != max) {
          min should be < max
        }
      }
    }
  }

  it should "have non-negative time values" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      val buckets = obj("distribution_buckets").flatMap(_.asArray).get
      
      buckets.foreach { bucket =>
        val bucketObj = bucket.asObject.get
        val time = bucketObj("time").flatMap(_.asNumber).flatMap(_.toInt).get
        time should be >= 0
      }
    }
  }

  it should "be ordered by min value" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      val buckets = obj("distribution_buckets").flatMap(_.asArray).get
      
      val minValues = buckets.map { bucket =>
        bucket.asObject.get("min").flatMap(_.asNumber).flatMap(_.toInt).get
      }
      
      // Should be sorted (except possibly for the first bucket which might be 0)
      minValues should equal(minValues.sorted)
    }
  }

  "Activity zones" should "parse get-zones-by-activity-id response" in {
    val json = loadJson("get-activity-zones-getzonesbyactivityid.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val zones = parsed.value.asArray.get
    zones.foreach { zone =>
      val obj = zone.asObject.get
      obj("type") shouldBe defined
      obj("distribution_buckets") shouldBe defined
    }
  }

  "Zone validation" should "ensure required fields are present" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    zones.foreach { zone =>
      val obj = zone.asObject.get
      
      val requiredFields = Seq("type", "distribution_buckets", "resource_state")
      requiredFields.foreach { field =>
        withClue(s"Field '$field' should be defined:") {
          obj(field) shouldBe defined
        }
      }
    }
  }

  it should "validate power zone ranges" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    val powerZone = zones.find { zone =>
      zone.asObject.get("type").flatMap(_.asString).contains("power")
    }
    
    if (powerZone.isDefined) {
      val obj = powerZone.get.asObject.get
      val buckets = obj("distribution_buckets").flatMap(_.asArray).get
      
      buckets.foreach { bucket =>
        val bucketObj = bucket.asObject.get
        val min = bucketObj("min").flatMap(_.asNumber).flatMap(_.toInt).get
        val max = bucketObj("max").flatMap(_.asNumber).flatMap(_.toInt).get
        
        // Power values should be reasonable
        min should be >= 0
        if (max != -1) {
          max should be <= 2000 // reasonable upper bound for power zones
        }
      }
    }
  }

  it should "validate heartrate zone ranges" in {
    val json = loadJson("get-zones-getloggedinathletezones.json")
    val parsed = parse(json).value
    val zones = parsed.asArray.get
    
    val hrZone = zones.find { zone =>
      zone.asObject.get("type").flatMap(_.asString).contains("heartrate")
    }
    
    if (hrZone.isDefined) {
      val obj = hrZone.get.asObject.get
      val buckets = obj("distribution_buckets").flatMap(_.asArray).get
      
      buckets.foreach { bucket =>
        val bucketObj = bucket.asObject.get
        val min = bucketObj("min").flatMap(_.asNumber).flatMap(_.toInt).get
        val max = bucketObj("max").flatMap(_.asNumber).flatMap(_.toInt).get
        
        // Heart rate values should be reasonable
        min should be >= 0
        if (max != -1) {
          max should be <= 250 // reasonable upper bound for heart rate zones
        }
      }
    }
  }
}

