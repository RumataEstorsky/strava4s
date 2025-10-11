package strava

import io.circe.{Decoder, Encoder}

package object models {
  // Helper for Scala Enumeration codecs
  private def enumerationDecoder[E <: Enumeration](`enum`: E): Decoder[`enum`.Value] =
    Decoder.decodeString.emap { str =>
      `enum`.values.find(_.toString == str).toRight(s"Unknown value: $str")
    }
  
  private def enumerationEncoder[E <: Enumeration](`enum`: E): Encoder[`enum`.Value] =
    Encoder.encodeString.contramap[`enum`.Value](_.toString)
  
  // Decoders for ActivityType and SportType (top-level enums)
  implicit val activityTypeDecoder: Decoder[api.ActivityType.ActivityType] = 
    enumerationDecoder(api.ActivityType)
  implicit val activityTypeEncoder: Encoder[api.ActivityType.ActivityType] = 
    enumerationEncoder(api.ActivityType)
    
  implicit val sportTypeDecoder: Decoder[api.SportType.SportType] = 
    enumerationDecoder(api.SportType)
  implicit val sportTypeEncoder: Encoder[api.SportType.SportType] = 
    enumerationEncoder(api.SportType)
    
  // Decoders for DetailedAthlete enums
  implicit val detailedAthleteSexDecoder: Decoder[api.DetailedAthleteEnums.Sex] = 
    enumerationDecoder(api.DetailedAthleteEnums.Sex)
  implicit val detailedAthleteSexEncoder: Encoder[api.DetailedAthleteEnums.Sex] = 
    enumerationEncoder(api.DetailedAthleteEnums.Sex)
    
  implicit val detailedAthleteMeasurementPreferenceDecoder: Decoder[api.DetailedAthleteEnums.MeasurementPreference] = 
    enumerationDecoder(api.DetailedAthleteEnums.MeasurementPreference)
  implicit val detailedAthleteMeasurementPreferenceEncoder: Encoder[api.DetailedAthleteEnums.MeasurementPreference] = 
    enumerationEncoder(api.DetailedAthleteEnums.MeasurementPreference)
    
  // Decoders for SummaryAthlete enums
  implicit val summaryAthleteSexDecoder: Decoder[api.SummaryAthleteEnums.Sex] = 
    enumerationDecoder(api.SummaryAthleteEnums.Sex)
  implicit val summaryAthleteSexEncoder: Encoder[api.SummaryAthleteEnums.Sex] = 
    enumerationEncoder(api.SummaryAthleteEnums.Sex)
    
  // Decoders for DetailedSegment enums
  implicit val detailedSegmentActivityTypeDecoder: Decoder[api.DetailedSegmentEnums.ActivityType] = 
    enumerationDecoder(api.DetailedSegmentEnums.ActivityType)
  implicit val detailedSegmentActivityTypeEncoder: Encoder[api.DetailedSegmentEnums.ActivityType] = 
    enumerationEncoder(api.DetailedSegmentEnums.ActivityType)
    
  // Decoders for SummarySegment enums
  implicit val summarySegmentActivityTypeDecoder: Decoder[api.SummarySegmentEnums.ActivityType] = 
    enumerationDecoder(api.SummarySegmentEnums.ActivityType)
  implicit val summarySegmentActivityTypeEncoder: Encoder[api.SummarySegmentEnums.ActivityType] = 
    enumerationEncoder(api.SummarySegmentEnums.ActivityType)
    
  // Decoders for SummaryClub enums
  implicit val summaryClubSportTypeDecoder: Decoder[api.SummaryClubEnums.SportType] = 
    enumerationDecoder(api.SummaryClubEnums.SportType)
  implicit val summaryClubSportTypeEncoder: Encoder[api.SummaryClubEnums.SportType] = 
    enumerationEncoder(api.SummaryClubEnums.SportType)
    
  // Decoders for DetailedClub enums
  implicit val detailedClubSportTypeDecoder: Decoder[api.DetailedClubEnums.SportType] = 
    enumerationDecoder(api.DetailedClubEnums.SportType)
  implicit val detailedClubSportTypeEncoder: Encoder[api.DetailedClubEnums.SportType] = 
    enumerationEncoder(api.DetailedClubEnums.SportType)
    
  implicit val detailedClubMembershipDecoder: Decoder[api.DetailedClubEnums.Membership] = 
    enumerationDecoder(api.DetailedClubEnums.Membership)
  implicit val detailedClubMembershipEncoder: Encoder[api.DetailedClubEnums.Membership] = 
    enumerationEncoder(api.DetailedClubEnums.Membership)
    
  // Decoders for ExplorerSegment enums
  implicit val explorerSegmentClimbCategoryDescDecoder: Decoder[api.ExplorerSegmentEnums.ClimbCategoryDesc] = 
    enumerationDecoder(api.ExplorerSegmentEnums.ClimbCategoryDesc)
  implicit val explorerSegmentClimbCategoryDescEncoder: Encoder[api.ExplorerSegmentEnums.ClimbCategoryDesc] = 
    enumerationEncoder(api.ExplorerSegmentEnums.ClimbCategoryDesc)
    
  // Decoders for ActivityZone enums
  implicit val activityZoneTypeDecoder: Decoder[api.ActivityZoneEnums.`Type`] = 
    enumerationDecoder(api.ActivityZoneEnums.`Type`)
  implicit val activityZoneTypeEncoder: Encoder[api.ActivityZoneEnums.`Type`] = 
    enumerationEncoder(api.ActivityZoneEnums.`Type`)
    
  // Decoders for BaseStream enums
  implicit val baseStreamResolutionDecoder: Decoder[api.BaseStreamEnums.Resolution] = 
    enumerationDecoder(api.BaseStreamEnums.Resolution)
  implicit val baseStreamResolutionEncoder: Encoder[api.BaseStreamEnums.Resolution] = 
    enumerationEncoder(api.BaseStreamEnums.Resolution)
    
  implicit val baseStreamSeriesTypeDecoder: Decoder[api.BaseStreamEnums.SeriesType] = 
    enumerationDecoder(api.BaseStreamEnums.SeriesType)
  implicit val baseStreamSeriesTypeEncoder: Encoder[api.BaseStreamEnums.SeriesType] = 
    enumerationEncoder(api.BaseStreamEnums.SeriesType)
    
  // Decoders for Stream types (they all have Resolution and SeriesType)
  implicit val timeStreamResolutionDecoder: Decoder[api.TimeStreamEnums.Resolution] = 
    enumerationDecoder(api.TimeStreamEnums.Resolution)
  implicit val timeStreamResolutionEncoder: Encoder[api.TimeStreamEnums.Resolution] = 
    enumerationEncoder(api.TimeStreamEnums.Resolution)
  implicit val timeStreamSeriesTypeDecoder: Decoder[api.TimeStreamEnums.SeriesType] = 
    enumerationDecoder(api.TimeStreamEnums.SeriesType)
  implicit val timeStreamSeriesTypeEncoder: Encoder[api.TimeStreamEnums.SeriesType] = 
    enumerationEncoder(api.TimeStreamEnums.SeriesType)
    
  implicit val distanceStreamResolutionDecoder: Decoder[api.DistanceStreamEnums.Resolution] = 
    enumerationDecoder(api.DistanceStreamEnums.Resolution)
  implicit val distanceStreamResolutionEncoder: Encoder[api.DistanceStreamEnums.Resolution] = 
    enumerationEncoder(api.DistanceStreamEnums.Resolution)
  implicit val distanceStreamSeriesTypeDecoder: Decoder[api.DistanceStreamEnums.SeriesType] = 
    enumerationDecoder(api.DistanceStreamEnums.SeriesType)
  implicit val distanceStreamSeriesTypeEncoder: Encoder[api.DistanceStreamEnums.SeriesType] = 
    enumerationEncoder(api.DistanceStreamEnums.SeriesType)
    
  implicit val altitudeStreamResolutionDecoder: Decoder[api.AltitudeStreamEnums.Resolution] = 
    enumerationDecoder(api.AltitudeStreamEnums.Resolution)
  implicit val altitudeStreamResolutionEncoder: Encoder[api.AltitudeStreamEnums.Resolution] = 
    enumerationEncoder(api.AltitudeStreamEnums.Resolution)
  implicit val altitudeStreamSeriesTypeDecoder: Decoder[api.AltitudeStreamEnums.SeriesType] = 
    enumerationDecoder(api.AltitudeStreamEnums.SeriesType)
  implicit val altitudeStreamSeriesTypeEncoder: Encoder[api.AltitudeStreamEnums.SeriesType] = 
    enumerationEncoder(api.AltitudeStreamEnums.SeriesType)
    
  implicit val heartrateStreamResolutionDecoder: Decoder[api.HeartrateStreamEnums.Resolution] = 
    enumerationDecoder(api.HeartrateStreamEnums.Resolution)
  implicit val heartrateStreamResolutionEncoder: Encoder[api.HeartrateStreamEnums.Resolution] = 
    enumerationEncoder(api.HeartrateStreamEnums.Resolution)
  implicit val heartrateStreamSeriesTypeDecoder: Decoder[api.HeartrateStreamEnums.SeriesType] = 
    enumerationDecoder(api.HeartrateStreamEnums.SeriesType)
  implicit val heartrateStreamSeriesTypeEncoder: Encoder[api.HeartrateStreamEnums.SeriesType] = 
    enumerationEncoder(api.HeartrateStreamEnums.SeriesType)
    
  implicit val cadenceStreamResolutionDecoder: Decoder[api.CadenceStreamEnums.Resolution] = 
    enumerationDecoder(api.CadenceStreamEnums.Resolution)
  implicit val cadenceStreamResolutionEncoder: Encoder[api.CadenceStreamEnums.Resolution] = 
    enumerationEncoder(api.CadenceStreamEnums.Resolution)
  implicit val cadenceStreamSeriesTypeDecoder: Decoder[api.CadenceStreamEnums.SeriesType] = 
    enumerationDecoder(api.CadenceStreamEnums.SeriesType)
  implicit val cadenceStreamSeriesTypeEncoder: Encoder[api.CadenceStreamEnums.SeriesType] = 
    enumerationEncoder(api.CadenceStreamEnums.SeriesType)
    
  implicit val powerStreamResolutionDecoder: Decoder[api.PowerStreamEnums.Resolution] = 
    enumerationDecoder(api.PowerStreamEnums.Resolution)
  implicit val powerStreamResolutionEncoder: Encoder[api.PowerStreamEnums.Resolution] = 
    enumerationEncoder(api.PowerStreamEnums.Resolution)
  implicit val powerStreamSeriesTypeDecoder: Decoder[api.PowerStreamEnums.SeriesType] = 
    enumerationDecoder(api.PowerStreamEnums.SeriesType)
  implicit val powerStreamSeriesTypeEncoder: Encoder[api.PowerStreamEnums.SeriesType] = 
    enumerationEncoder(api.PowerStreamEnums.SeriesType)
    
  implicit val temperatureStreamResolutionDecoder: Decoder[api.TemperatureStreamEnums.Resolution] = 
    enumerationDecoder(api.TemperatureStreamEnums.Resolution)
  implicit val temperatureStreamResolutionEncoder: Encoder[api.TemperatureStreamEnums.Resolution] = 
    enumerationEncoder(api.TemperatureStreamEnums.Resolution)
  implicit val temperatureStreamSeriesTypeDecoder: Decoder[api.TemperatureStreamEnums.SeriesType] = 
    enumerationDecoder(api.TemperatureStreamEnums.SeriesType)
  implicit val temperatureStreamSeriesTypeEncoder: Encoder[api.TemperatureStreamEnums.SeriesType] = 
    enumerationEncoder(api.TemperatureStreamEnums.SeriesType)
    
  implicit val movingStreamResolutionDecoder: Decoder[api.MovingStreamEnums.Resolution] = 
    enumerationDecoder(api.MovingStreamEnums.Resolution)
  implicit val movingStreamResolutionEncoder: Encoder[api.MovingStreamEnums.Resolution] = 
    enumerationEncoder(api.MovingStreamEnums.Resolution)
  implicit val movingStreamSeriesTypeDecoder: Decoder[api.MovingStreamEnums.SeriesType] = 
    enumerationDecoder(api.MovingStreamEnums.SeriesType)
  implicit val movingStreamSeriesTypeEncoder: Encoder[api.MovingStreamEnums.SeriesType] = 
    enumerationEncoder(api.MovingStreamEnums.SeriesType)
    
  implicit val smoothGradeStreamResolutionDecoder: Decoder[api.SmoothGradeStreamEnums.Resolution] = 
    enumerationDecoder(api.SmoothGradeStreamEnums.Resolution)
  implicit val smoothGradeStreamResolutionEncoder: Encoder[api.SmoothGradeStreamEnums.Resolution] = 
    enumerationEncoder(api.SmoothGradeStreamEnums.Resolution)
  implicit val smoothGradeStreamSeriesTypeDecoder: Decoder[api.SmoothGradeStreamEnums.SeriesType] = 
    enumerationDecoder(api.SmoothGradeStreamEnums.SeriesType)
  implicit val smoothGradeStreamSeriesTypeEncoder: Encoder[api.SmoothGradeStreamEnums.SeriesType] = 
    enumerationEncoder(api.SmoothGradeStreamEnums.SeriesType)
    
  implicit val smoothVelocityStreamResolutionDecoder: Decoder[api.SmoothVelocityStreamEnums.Resolution] = 
    enumerationDecoder(api.SmoothVelocityStreamEnums.Resolution)
  implicit val smoothVelocityStreamResolutionEncoder: Encoder[api.SmoothVelocityStreamEnums.Resolution] = 
    enumerationEncoder(api.SmoothVelocityStreamEnums.Resolution)
  implicit val smoothVelocityStreamSeriesTypeDecoder: Decoder[api.SmoothVelocityStreamEnums.SeriesType] = 
    enumerationDecoder(api.SmoothVelocityStreamEnums.SeriesType)
  implicit val smoothVelocityStreamSeriesTypeEncoder: Encoder[api.SmoothVelocityStreamEnums.SeriesType] = 
    enumerationEncoder(api.SmoothVelocityStreamEnums.SeriesType)
    
  implicit val latLngStreamResolutionDecoder: Decoder[api.LatLngStreamEnums.Resolution] = 
    enumerationDecoder(api.LatLngStreamEnums.Resolution)
  implicit val latLngStreamResolutionEncoder: Encoder[api.LatLngStreamEnums.Resolution] = 
    enumerationEncoder(api.LatLngStreamEnums.Resolution)
  implicit val latLngStreamSeriesTypeDecoder: Decoder[api.LatLngStreamEnums.SeriesType] = 
    enumerationDecoder(api.LatLngStreamEnums.SeriesType)
  implicit val latLngStreamSeriesTypeEncoder: Encoder[api.LatLngStreamEnums.SeriesType] = 
    enumerationEncoder(api.LatLngStreamEnums.SeriesType)
    
  // Decoders for SummaryActivity and DetailedActivity enums (if any)
  // These classes reference ActivityType and SportType which are already defined above
}
