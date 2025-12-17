package strava.models

import io.circe.{Decoder, Encoder}

// Helper for Scala 2 Enumeration codecs
private def enumerationDecoder[E <: Enumeration](`enum`: E): Decoder[`enum`.Value] =
  Decoder.decodeString.emap: str =>
    `enum`.values.find(_.toString == str).toRight(s"Unknown value: $str")

private def enumerationEncoder[E <: Enumeration](`enum`: E): Encoder[`enum`.Value] =
  Encoder.encodeString.contramap[`enum`.Value](_.toString)

// Decoders for ActivityType and SportType (Scala 3 enums)
given activityTypeDecoder: Decoder[api.ActivityType] =
  Decoder.decodeString.emap(s => api.ActivityType.withNameOpt(s).toRight(s"Unknown ActivityType: $s"))
given activityTypeEncoder: Encoder[api.ActivityType] =
  Encoder.encodeString.contramap(_.toString)
  
given sportTypeDecoder: Decoder[api.SportType] =
  Decoder.decodeString.emap(s => api.SportType.withNameOpt(s).toRight(s"Unknown SportType: $s"))
given sportTypeEncoder: Encoder[api.SportType] =
  Encoder.encodeString.contramap(_.toString)
  
// Decoders for DetailedAthlete enums
given detailedAthleteSexDecoder: Decoder[api.DetailedAthleteEnums.Sex] = 
  enumerationDecoder(api.DetailedAthleteEnums.Sex)
given detailedAthleteSexEncoder: Encoder[api.DetailedAthleteEnums.Sex] = 
  enumerationEncoder(api.DetailedAthleteEnums.Sex)
  
given detailedAthleteMeasurementPreferenceDecoder: Decoder[api.DetailedAthleteEnums.MeasurementPreference] = 
  enumerationDecoder(api.DetailedAthleteEnums.MeasurementPreference)
given detailedAthleteMeasurementPreferenceEncoder: Encoder[api.DetailedAthleteEnums.MeasurementPreference] = 
  enumerationEncoder(api.DetailedAthleteEnums.MeasurementPreference)
  
// Decoders for SummaryAthlete enums
given summaryAthleteSexDecoder: Decoder[api.SummaryAthleteEnums.Sex] = 
  enumerationDecoder(api.SummaryAthleteEnums.Sex)
given summaryAthleteSexEncoder: Encoder[api.SummaryAthleteEnums.Sex] = 
  enumerationEncoder(api.SummaryAthleteEnums.Sex)
  
// Decoders for DetailedSegment enums
given detailedSegmentActivityTypeDecoder: Decoder[api.DetailedSegmentEnums.ActivityType] = 
  enumerationDecoder(api.DetailedSegmentEnums.ActivityType)
given detailedSegmentActivityTypeEncoder: Encoder[api.DetailedSegmentEnums.ActivityType] = 
  enumerationEncoder(api.DetailedSegmentEnums.ActivityType)
  
// Decoders for SummarySegment enums
given summarySegmentActivityTypeDecoder: Decoder[api.SummarySegmentEnums.ActivityType] = 
  enumerationDecoder(api.SummarySegmentEnums.ActivityType)
given summarySegmentActivityTypeEncoder: Encoder[api.SummarySegmentEnums.ActivityType] = 
  enumerationEncoder(api.SummarySegmentEnums.ActivityType)
  
// Decoders for SummaryClub enums (Scala 3)
given summaryClubSportTypeDecoder: Decoder[api.SummaryClubEnums.SportType] =
  Decoder.decodeString.emap: s =>
    api.SummaryClubEnums.SportType.values.find(_.value == s).toRight(s"Unknown SportType: $s")
given summaryClubSportTypeEncoder: Encoder[api.SummaryClubEnums.SportType] =
  Encoder.encodeString.contramap(_.value)
  
// Decoders for DetailedClub enums (Scala 3)
given detailedClubSportTypeDecoder: Decoder[api.DetailedClubEnums.SportType] =
  Decoder.decodeString.emap: s =>
    api.DetailedClubEnums.SportType.values.find(_.value == s).toRight(s"Unknown SportType: $s")
given detailedClubSportTypeEncoder: Encoder[api.DetailedClubEnums.SportType] =
  Encoder.encodeString.contramap(_.value)
  
given detailedClubMembershipDecoder: Decoder[api.DetailedClubEnums.Membership] =
  Decoder.decodeString.emap: s =>
    api.DetailedClubEnums.Membership.values.find(_.value == s).toRight(s"Unknown Membership: $s")
given detailedClubMembershipEncoder: Encoder[api.DetailedClubEnums.Membership] =
  Encoder.encodeString.contramap(_.value)
  
// Decoders for ExplorerSegment enums
given explorerSegmentClimbCategoryDescDecoder: Decoder[api.ExplorerSegmentEnums.ClimbCategoryDesc] = 
  enumerationDecoder(api.ExplorerSegmentEnums.ClimbCategoryDesc)
given explorerSegmentClimbCategoryDescEncoder: Encoder[api.ExplorerSegmentEnums.ClimbCategoryDesc] = 
  enumerationEncoder(api.ExplorerSegmentEnums.ClimbCategoryDesc)
  
// Decoders for ActivityZone enums
given activityZoneTypeDecoder: Decoder[api.ActivityZoneEnums.`Type`] = 
  enumerationDecoder(api.ActivityZoneEnums.`Type`)
given activityZoneTypeEncoder: Encoder[api.ActivityZoneEnums.`Type`] = 
  enumerationEncoder(api.ActivityZoneEnums.`Type`)
  
// Decoders for BaseStream enums
given baseStreamResolutionDecoder: Decoder[api.BaseStreamEnums.Resolution] = 
  enumerationDecoder(api.BaseStreamEnums.Resolution)
given baseStreamResolutionEncoder: Encoder[api.BaseStreamEnums.Resolution] = 
  enumerationEncoder(api.BaseStreamEnums.Resolution)
  
given baseStreamSeriesTypeDecoder: Decoder[api.BaseStreamEnums.SeriesType] = 
  enumerationDecoder(api.BaseStreamEnums.SeriesType)
given baseStreamSeriesTypeEncoder: Encoder[api.BaseStreamEnums.SeriesType] = 
  enumerationEncoder(api.BaseStreamEnums.SeriesType)
  
// Decoders for Stream types (they all have Resolution and SeriesType)
given timeStreamResolutionDecoder: Decoder[api.TimeStreamEnums.Resolution] = 
  enumerationDecoder(api.TimeStreamEnums.Resolution)
given timeStreamResolutionEncoder: Encoder[api.TimeStreamEnums.Resolution] = 
  enumerationEncoder(api.TimeStreamEnums.Resolution)
given timeStreamSeriesTypeDecoder: Decoder[api.TimeStreamEnums.SeriesType] = 
  enumerationDecoder(api.TimeStreamEnums.SeriesType)
given timeStreamSeriesTypeEncoder: Encoder[api.TimeStreamEnums.SeriesType] = 
  enumerationEncoder(api.TimeStreamEnums.SeriesType)
  
given distanceStreamResolutionDecoder: Decoder[api.DistanceStreamEnums.Resolution] = 
  enumerationDecoder(api.DistanceStreamEnums.Resolution)
given distanceStreamResolutionEncoder: Encoder[api.DistanceStreamEnums.Resolution] = 
  enumerationEncoder(api.DistanceStreamEnums.Resolution)
given distanceStreamSeriesTypeDecoder: Decoder[api.DistanceStreamEnums.SeriesType] = 
  enumerationDecoder(api.DistanceStreamEnums.SeriesType)
given distanceStreamSeriesTypeEncoder: Encoder[api.DistanceStreamEnums.SeriesType] = 
  enumerationEncoder(api.DistanceStreamEnums.SeriesType)
  
given altitudeStreamResolutionDecoder: Decoder[api.AltitudeStreamEnums.Resolution] = 
  enumerationDecoder(api.AltitudeStreamEnums.Resolution)
given altitudeStreamResolutionEncoder: Encoder[api.AltitudeStreamEnums.Resolution] = 
  enumerationEncoder(api.AltitudeStreamEnums.Resolution)
given altitudeStreamSeriesTypeDecoder: Decoder[api.AltitudeStreamEnums.SeriesType] = 
  enumerationDecoder(api.AltitudeStreamEnums.SeriesType)
given altitudeStreamSeriesTypeEncoder: Encoder[api.AltitudeStreamEnums.SeriesType] = 
  enumerationEncoder(api.AltitudeStreamEnums.SeriesType)
  
given heartrateStreamResolutionDecoder: Decoder[api.HeartrateStreamEnums.Resolution] = 
  enumerationDecoder(api.HeartrateStreamEnums.Resolution)
given heartrateStreamResolutionEncoder: Encoder[api.HeartrateStreamEnums.Resolution] = 
  enumerationEncoder(api.HeartrateStreamEnums.Resolution)
given heartrateStreamSeriesTypeDecoder: Decoder[api.HeartrateStreamEnums.SeriesType] = 
  enumerationDecoder(api.HeartrateStreamEnums.SeriesType)
given heartrateStreamSeriesTypeEncoder: Encoder[api.HeartrateStreamEnums.SeriesType] = 
  enumerationEncoder(api.HeartrateStreamEnums.SeriesType)
  
given cadenceStreamResolutionDecoder: Decoder[api.CadenceStreamEnums.Resolution] = 
  enumerationDecoder(api.CadenceStreamEnums.Resolution)
given cadenceStreamResolutionEncoder: Encoder[api.CadenceStreamEnums.Resolution] = 
  enumerationEncoder(api.CadenceStreamEnums.Resolution)
given cadenceStreamSeriesTypeDecoder: Decoder[api.CadenceStreamEnums.SeriesType] = 
  enumerationDecoder(api.CadenceStreamEnums.SeriesType)
given cadenceStreamSeriesTypeEncoder: Encoder[api.CadenceStreamEnums.SeriesType] = 
  enumerationEncoder(api.CadenceStreamEnums.SeriesType)
  
given powerStreamResolutionDecoder: Decoder[api.PowerStreamEnums.Resolution] = 
  enumerationDecoder(api.PowerStreamEnums.Resolution)
given powerStreamResolutionEncoder: Encoder[api.PowerStreamEnums.Resolution] = 
  enumerationEncoder(api.PowerStreamEnums.Resolution)
given powerStreamSeriesTypeDecoder: Decoder[api.PowerStreamEnums.SeriesType] = 
  enumerationDecoder(api.PowerStreamEnums.SeriesType)
given powerStreamSeriesTypeEncoder: Encoder[api.PowerStreamEnums.SeriesType] = 
  enumerationEncoder(api.PowerStreamEnums.SeriesType)
  
given temperatureStreamResolutionDecoder: Decoder[api.TemperatureStreamEnums.Resolution] = 
  enumerationDecoder(api.TemperatureStreamEnums.Resolution)
given temperatureStreamResolutionEncoder: Encoder[api.TemperatureStreamEnums.Resolution] = 
  enumerationEncoder(api.TemperatureStreamEnums.Resolution)
given temperatureStreamSeriesTypeDecoder: Decoder[api.TemperatureStreamEnums.SeriesType] = 
  enumerationDecoder(api.TemperatureStreamEnums.SeriesType)
given temperatureStreamSeriesTypeEncoder: Encoder[api.TemperatureStreamEnums.SeriesType] = 
  enumerationEncoder(api.TemperatureStreamEnums.SeriesType)
  
given movingStreamResolutionDecoder: Decoder[api.MovingStreamEnums.Resolution] = 
  enumerationDecoder(api.MovingStreamEnums.Resolution)
given movingStreamResolutionEncoder: Encoder[api.MovingStreamEnums.Resolution] = 
  enumerationEncoder(api.MovingStreamEnums.Resolution)
given movingStreamSeriesTypeDecoder: Decoder[api.MovingStreamEnums.SeriesType] = 
  enumerationDecoder(api.MovingStreamEnums.SeriesType)
given movingStreamSeriesTypeEncoder: Encoder[api.MovingStreamEnums.SeriesType] = 
  enumerationEncoder(api.MovingStreamEnums.SeriesType)
  
given smoothGradeStreamResolutionDecoder: Decoder[api.SmoothGradeStreamEnums.Resolution] = 
  enumerationDecoder(api.SmoothGradeStreamEnums.Resolution)
given smoothGradeStreamResolutionEncoder: Encoder[api.SmoothGradeStreamEnums.Resolution] = 
  enumerationEncoder(api.SmoothGradeStreamEnums.Resolution)
given smoothGradeStreamSeriesTypeDecoder: Decoder[api.SmoothGradeStreamEnums.SeriesType] = 
  enumerationDecoder(api.SmoothGradeStreamEnums.SeriesType)
given smoothGradeStreamSeriesTypeEncoder: Encoder[api.SmoothGradeStreamEnums.SeriesType] = 
  enumerationEncoder(api.SmoothGradeStreamEnums.SeriesType)
  
given smoothVelocityStreamResolutionDecoder: Decoder[api.SmoothVelocityStreamEnums.Resolution] = 
  enumerationDecoder(api.SmoothVelocityStreamEnums.Resolution)
given smoothVelocityStreamResolutionEncoder: Encoder[api.SmoothVelocityStreamEnums.Resolution] = 
  enumerationEncoder(api.SmoothVelocityStreamEnums.Resolution)
given smoothVelocityStreamSeriesTypeDecoder: Decoder[api.SmoothVelocityStreamEnums.SeriesType] = 
  enumerationDecoder(api.SmoothVelocityStreamEnums.SeriesType)
given smoothVelocityStreamSeriesTypeEncoder: Encoder[api.SmoothVelocityStreamEnums.SeriesType] = 
  enumerationEncoder(api.SmoothVelocityStreamEnums.SeriesType)
  
given latLngStreamResolutionDecoder: Decoder[api.LatLngStreamEnums.Resolution] = 
  enumerationDecoder(api.LatLngStreamEnums.Resolution)
given latLngStreamResolutionEncoder: Encoder[api.LatLngStreamEnums.Resolution] = 
  enumerationEncoder(api.LatLngStreamEnums.Resolution)
given latLngStreamSeriesTypeDecoder: Decoder[api.LatLngStreamEnums.SeriesType] = 
  enumerationDecoder(api.LatLngStreamEnums.SeriesType)
given latLngStreamSeriesTypeEncoder: Encoder[api.LatLngStreamEnums.SeriesType] = 
  enumerationEncoder(api.LatLngStreamEnums.SeriesType)

// Decoders for SummaryActivity and DetailedActivity enums (if any)
// These classes reference ActivityType and SportType which are already defined above
