package strava.models

import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.io.Source

/**
 * Comprehensive tests for upload-related models
 * Tests JSON parsing from real Strava API responses
 */
class UploadSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def loadJson(filename: String): String = {
    val source = Source.fromResource(s"strava/$filename")
    try source.mkString finally source.close()
  }

  "Upload" should "parse create-upload response" in {
    val json = loadJson("upload-activity-createupload.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val upload = parsed.value.asObject.get
    
    upload("id") shouldBe defined
    upload("id_str") shouldBe defined
    upload("external_id") shouldBe defined
    upload("status") shouldBe defined
  }

  it should "contain upload identifiers" in {
    val json = loadJson("upload-activity-createupload.json")
    val parsed = parse(json).value
    val upload = parsed.asObject.get
    
    upload("id").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(0)
    upload("id_str").flatMap(_.asString) shouldBe Some("aeiou")
    upload("external_id").flatMap(_.asString) shouldBe Some("aeiou")
  }

  it should "contain status field" in {
    val json = loadJson("upload-activity-createupload.json")
    val parsed = parse(json).value
    val upload = parsed.asObject.get
    
    val status = upload("status").flatMap(_.asString).get
    status should not be empty
  }

  it should "contain error field" in {
    val json = loadJson("upload-activity-createupload.json")
    val parsed = parse(json).value
    val upload = parsed.asObject.get
    
    upload("error") shouldBe defined
  }

  it should "contain activity_id when processed" in {
    val json = loadJson("upload-activity-createupload.json")
    val parsed = parse(json).value
    val upload = parsed.asObject.get
    
    upload("activity_id") shouldBe defined
    upload("activity_id").flatMap(_.asNumber).flatMap(_.toInt) shouldBe Some(6)
  }

  "Get upload by id" should "parse get-upload-by-id response" in {
    val json = loadJson("get-upload-getuploadbyid.json")
    
    val parsed = parse(json)
    parsed shouldBe a[Right[_, _]]
    
    val upload = parsed.value.asObject.get
    upload("id") shouldBe defined
    upload("status") shouldBe defined
  }

  "Upload validation" should "ensure required fields are present" in {
    val json = loadJson("upload-activity-createupload.json")
    val parsed = parse(json).value
    val upload = parsed.asObject.get
    
    val requiredFields = Seq("id", "external_id", "status")
    requiredFields.foreach { field =>
      withClue(s"Field '$field' should be defined:") {
        upload(field) shouldBe defined
      }
    }
  }

  it should "validate status values" in {
    val json = loadJson("upload-activity-createupload.json")
    val parsed = parse(json).value
    val upload = parsed.asObject.get
    
    val status = upload("status").flatMap(_.asString).get
    // Valid status values: "Your activity is still being processed.",
    // "Your activity is ready.", "There was an error processing your activity."
    status should not be empty
  }

  it should "validate numeric types" in {
    val json = loadJson("upload-activity-createupload.json")
    val parsed = parse(json).value
    val upload = parsed.asObject.get
    
    val id = upload("id").flatMap(_.asNumber).flatMap(_.toInt).get
    id should be >= 0
    
    if (upload("activity_id").isDefined && !upload("activity_id").get.isNull) {
      val activityId = upload("activity_id").flatMap(_.asNumber).flatMap(_.toInt).get
      activityId should be > 0
    }
  }
}

