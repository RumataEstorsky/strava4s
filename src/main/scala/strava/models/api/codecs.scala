package strava.models.api

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import strava.models.given

object codecs:
  // Simple models without nested dependencies
  given Decoder[MetaActivity] = deriveDecoder
  given Encoder[MetaActivity] = deriveEncoder
  
  given Decoder[MetaAthlete] = deriveDecoder
  given Encoder[MetaAthlete] = deriveEncoder
  
  given Decoder[MetaClub] = deriveDecoder
  given Encoder[MetaClub] = deriveEncoder
  
  given Decoder[PolylineMap] = deriveDecoder
  given Encoder[PolylineMap] = deriveEncoder
  
  given Decoder[PhotosSummaryPrimary] = deriveDecoder
  given Encoder[PhotosSummaryPrimary] = deriveEncoder
  
  given Decoder[PhotosSummary] = deriveDecoder
  given Encoder[PhotosSummary] = deriveEncoder
  
  given Decoder[SummaryGear] = deriveDecoder
  given Encoder[SummaryGear] = deriveEncoder
  
  given Decoder[DetailedGear] = deriveDecoder
  given Encoder[DetailedGear] = deriveEncoder
  
  given Decoder[ZoneRange] = deriveDecoder
  given Encoder[ZoneRange] = deriveEncoder
  
  given Decoder[TimedZoneRange] = deriveDecoder
  given Encoder[TimedZoneRange] = deriveEncoder
  
  given Decoder[HeartRateZoneRanges] = deriveDecoder
  given Encoder[HeartRateZoneRanges] = deriveEncoder
  
  given Decoder[PowerZoneRanges] = deriveDecoder
  given Encoder[PowerZoneRanges] = deriveEncoder
  
  given Decoder[Zones] = deriveDecoder
  given Encoder[Zones] = deriveEncoder
  
  given Decoder[ActivityTotal] = deriveDecoder
  given Encoder[ActivityTotal] = deriveEncoder
  
  given Decoder[ActivityStats] = deriveDecoder
  given Encoder[ActivityStats] = deriveEncoder
  
  given Decoder[ActivityZone] = deriveDecoder
  given Encoder[ActivityZone] = deriveEncoder
  
  given Decoder[Split] = deriveDecoder
  given Encoder[Split] = deriveEncoder
  
  given Decoder[Waypoint] = deriveDecoder
  given Encoder[Waypoint] = deriveEncoder
  
  given Decoder[Route] = deriveDecoder
  given Encoder[Route] = deriveEncoder
  
  given Decoder[Comment] = deriveDecoder
  given Encoder[Comment] = deriveEncoder
  
  given Decoder[Fault] = deriveDecoder
  given Encoder[Fault] = deriveEncoder
  
  given Decoder[Error] = deriveDecoder
  given Encoder[Error] = deriveEncoder
  
  given Decoder[Upload] = deriveDecoder
  given Encoder[Upload] = deriveEncoder
  
  given Decoder[UpdatableActivity] = deriveDecoder
  given Encoder[UpdatableActivity] = deriveEncoder
  
  // Athlete models
  given Decoder[SummaryAthlete] = deriveDecoder
  given Encoder[SummaryAthlete] = deriveEncoder
  
  given Decoder[DetailedAthlete] = deriveDecoder
  given Encoder[DetailedAthlete] = deriveEncoder
  
  given Decoder[ClubAthlete] = deriveDecoder
  given Encoder[ClubAthlete] = deriveEncoder
  
  // Club models
  given Decoder[SummaryClub] = deriveDecoder
  given Encoder[SummaryClub] = deriveEncoder
  
  given Decoder[DetailedClub] = deriveDecoder
  given Encoder[DetailedClub] = deriveEncoder
  
  given Decoder[ClubActivity] = deriveDecoder
  given Encoder[ClubActivity] = deriveEncoder
  
  // Segment models
  given Decoder[SummarySegment] = deriveDecoder
  given Encoder[SummarySegment] = deriveEncoder
  
  given Decoder[DetailedSegment] = deriveDecoder
  given Encoder[DetailedSegment] = deriveEncoder
  
  given Decoder[ExplorerSegment] = deriveDecoder
  given Encoder[ExplorerSegment] = deriveEncoder
  
  given Decoder[ExplorerResponse] = deriveDecoder
  given Encoder[ExplorerResponse] = deriveEncoder
  
  given Decoder[SummaryPRSegmentEffort] = deriveDecoder
  given Encoder[SummaryPRSegmentEffort] = deriveEncoder
  
  given Decoder[SummarySegmentEffort] = deriveDecoder
  given Encoder[SummarySegmentEffort] = deriveEncoder
  
  given Decoder[DetailedSegmentEffort] = deriveDecoder
  given Encoder[DetailedSegmentEffort] = deriveEncoder
  
  // Lap
  given Decoder[Lap] = deriveDecoder
  given Encoder[Lap] = deriveEncoder
  
  // Activity models
  given Decoder[SummaryActivity] = deriveDecoder
  given Encoder[SummaryActivity] = deriveEncoder
  
  given Decoder[DetailedActivity] = deriveDecoder
  given Encoder[DetailedActivity] = deriveEncoder
  
  // Stream models
  given Decoder[BaseStream] = deriveDecoder
  given Encoder[BaseStream] = deriveEncoder
  
  given Decoder[TimeStream] = deriveDecoder
  given Encoder[TimeStream] = deriveEncoder
  
  given Decoder[DistanceStream] = deriveDecoder
  given Encoder[DistanceStream] = deriveEncoder
  
  given Decoder[AltitudeStream] = deriveDecoder
  given Encoder[AltitudeStream] = deriveEncoder
  
  given Decoder[HeartrateStream] = deriveDecoder
  given Encoder[HeartrateStream] = deriveEncoder
  
  given Decoder[CadenceStream] = deriveDecoder
  given Encoder[CadenceStream] = deriveEncoder
  
  given Decoder[PowerStream] = deriveDecoder
  given Encoder[PowerStream] = deriveEncoder
  
  given Decoder[TemperatureStream] = deriveDecoder
  given Encoder[TemperatureStream] = deriveEncoder
  
  given Decoder[MovingStream] = deriveDecoder
  given Encoder[MovingStream] = deriveEncoder
  
  given Decoder[SmoothGradeStream] = deriveDecoder
  given Encoder[SmoothGradeStream] = deriveEncoder
  
  given Decoder[SmoothVelocityStream] = deriveDecoder
  given Encoder[SmoothVelocityStream] = deriveEncoder
  
  given Decoder[LatLngStream] = deriveDecoder
  given Encoder[LatLngStream] = deriveEncoder
  
  given Decoder[StreamSet] = deriveDecoder
  given Encoder[StreamSet] = deriveEncoder

