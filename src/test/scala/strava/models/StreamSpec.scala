package strava.models

import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for stream-related models (Activity Streams, Segment Streams, Route Streams)
 * Tests JSON parsing from real Strava API responses
 */
class StreamSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "Activity Streams" should "parse get-activity-streams response" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val streams = parsed.value.asArray.get
    streams should not be empty
  }

  it should "contain valid stream types" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val validStreamTypes = Seq(
      "time", "latlng", "distance", "altitude", "velocity_smooth",
      "heartrate", "cadence", "watts", "temp", "moving", "grade_smooth"
    )
    
    streams.foreach { stream =>
      val obj = stream.asObject.get
      val streamType = obj("type").flatMap(_.asString).get
      
      withClue(s"Stream type '$streamType' should be valid:") {
        validStreamTypes should contain(streamType)
      }
    }
  }

  it should "have data and original_size for each stream" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    streams.foreach { stream =>
      val obj = stream.asObject.get
      obj("data") shouldBe defined
      obj("original_size") shouldBe defined
      obj("resolution") shouldBe defined
      obj("series_type") shouldBe defined
    }
  }

  it should "validate time stream" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val timeStream = streams.find { stream =>
      stream.asObject.get("type").flatMap(_.asString).contains("time")
    }
    
    if (timeStream.isDefined) {
      val obj = timeStream.get.asObject.get
      val data = obj("data").flatMap(_.asArray).get
      
      data should not be empty
      data.foreach { value =>
        val time = value.asNumber.flatMap(_.toInt).get
        time should be >= 0
      }
      
      // Time should be monotonically increasing
      val times = data.map(_.asNumber.flatMap(_.toInt).get)
      times should equal(times.sorted)
    }
  }

  it should "validate distance stream" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val distanceStream = streams.find { stream =>
      stream.asObject.get("type").flatMap(_.asString).contains("distance")
    }
    
    if (distanceStream.isDefined) {
      val obj = distanceStream.get.asObject.get
      val data = obj("data").flatMap(_.asArray).get
      
      data should not be empty
      data.foreach { value =>
        val distance = value.asNumber.map(_.toFloat).get
        distance should be >= 0f
      }
      
      // Distance should be monotonically increasing
      val distances = data.map(_.asNumber.map(_.toFloat).get)
      distances should equal(distances.sorted)
    }
  }

  it should "validate latlng stream" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val latlngStream = streams.find { stream =>
      stream.asObject.get("type").flatMap(_.asString).contains("latlng")
    }
    
    if (latlngStream.isDefined) {
      val obj = latlngStream.get.asObject.get
      val data = obj("data").flatMap(_.asArray).get
      
      data should not be empty
      data.foreach { value =>
        val coords = value.asArray.get
        coords should have size 2
        
        val lat = coords.head.asNumber.map(_.toFloat).get
        val lng = coords(1).asNumber.map(_.toFloat).get
        
        lat should (be >= -90f and be <= 90f)
        lng should (be >= -180f and be <= 180f)
      }
    }
  }

  it should "validate altitude stream" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val altitudeStream = streams.find { stream =>
      stream.asObject.get("type").flatMap(_.asString).contains("altitude")
    }
    
    if (altitudeStream.isDefined) {
      val obj = altitudeStream.get.asObject.get
      val data = obj("data").flatMap(_.asArray).get
      
      data should not be empty
      data.foreach { value =>
        val altitude = value.asNumber.map(_.toFloat).get
        altitude should (be >= -500f and be <= 9000f) // reasonable altitude range
      }
    }
  }

  it should "validate heartrate stream" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val heartrateStream = streams.find { stream =>
      stream.asObject.get("type").flatMap(_.asString).contains("heartrate")
    }
    
    if (heartrateStream.isDefined) {
      val obj = heartrateStream.get.asObject.get
      val data = obj("data").flatMap(_.asArray).get
      
      data should not be empty
      data.foreach { value =>
        val hr = value.asNumber.flatMap(_.toInt).get
        hr should (be >= 0 and be <= 250) // reasonable heart rate range
      }
    }
  }

  it should "validate cadence stream" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val cadenceStream = streams.find { stream =>
      stream.asObject.get("type").flatMap(_.asString).contains("cadence")
    }
    
    if (cadenceStream.isDefined) {
      val obj = cadenceStream.get.asObject.get
      val data = obj("data").flatMap(_.asArray).get
      
      data should not be empty
      data.foreach { value =>
        val cadence = value.asNumber.flatMap(_.toInt).get
        cadence should (be >= 0 and be <= 300) // reasonable cadence range
      }
    }
  }

  it should "validate power stream" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val powerStream = streams.find { stream =>
      stream.asObject.get("type").flatMap(_.asString).contains("watts")
    }
    
    if (powerStream.isDefined) {
      val obj = powerStream.get.asObject.get
      val data = obj("data").flatMap(_.asArray).get
      
      data should not be empty
      data.foreach { value =>
        val power = value.asNumber.flatMap(_.toInt).get
        power should be >= 0
      }
    }
  }

  "Segment Streams" should "parse get-segment-streams response" in {
    val json = loadJson("get-segment-streams-getsegmentstreams.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val streams = parsed.value.asArray.get
    streams should not be empty
  }

  it should "contain valid stream structure" in {
    val json = loadJson("get-segment-streams-getsegmentstreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    streams.foreach { stream =>
      val obj = stream.asObject.get
      obj("type") shouldBe defined
      obj("data") shouldBe defined
      obj("original_size") shouldBe defined
      obj("resolution") shouldBe defined
      obj("series_type") shouldBe defined
    }
  }

  "Segment Effort Streams" should "parse get-segment-effort-streams response" in {
    val json = loadJson("get-segment-effort-streams-getsegmenteffortstreams.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val streams = parsed.value.asArray.get
    streams should not be empty
  }

  "Route Streams" should "parse get-route-streams response" in {
    val json = loadJson("get-route-streams-getroutestreams.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val streams = parsed.value.asArray.get
    streams should not be empty
  }

  "Stream validation" should "ensure all streams have same original_size" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    if (streams.nonEmpty) {
      val sizes = streams.map { stream =>
        stream.asObject.get("original_size").flatMap(_.asNumber).flatMap(_.toInt).get
      }
      
      sizes.toSet should have size 1 // all sizes should be the same
    }
  }

  it should "ensure data array length matches original_size or is downsampled" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    streams.foreach { stream =>
      val obj = stream.asObject.get
      val data = obj("data").flatMap(_.asArray).get
      val originalSize = obj("original_size").flatMap(_.asNumber).flatMap(_.toInt).get
      val resolution = obj("resolution").flatMap(_.asString).get
      
      if (resolution == "high") {
        data.size should equal(originalSize)
      } else {
        data.size should be <= originalSize
      }
    }
  }

  it should "validate series_type values" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val validSeriesTypes = Seq("distance", "time")
    
    streams.foreach { stream =>
      val obj = stream.asObject.get
      val seriesType = obj("series_type").flatMap(_.asString).get
      
      withClue(s"Series type '$seriesType' should be valid:") {
        validSeriesTypes should contain(seriesType)
      }
    }
  }

  it should "validate resolution values" in {
    val json = loadJson("get-activity-streams-getactivitystreams.json")
    val parsed = parse(json).value
    val streams = parsed.asArray.get
    
    val validResolutions = Seq("low", "medium", "high")
    
    streams.foreach { stream =>
      val obj = stream.asObject.get
      val resolution = obj("resolution").flatMap(_.asString).get
      
      withClue(s"Resolution '$resolution' should be valid:") {
        validResolutions should contain(resolution)
      }
    }
  }
}

