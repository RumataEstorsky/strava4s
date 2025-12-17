package strava.models.api

enum ActivityType:
  case AlpineSki, BackcountrySki, Canoeing, Crossfit, EBikeRide, Elliptical, Golf
  case Handcycle, Hike, IceSkate, InlineSkate, Kayaking, Kitesurf, NordicSki, Ride
  case RockClimbing, RollerSki, Rowing, Run, Sail, Skateboard, Snowboard, Snowshoe
  case Soccer, StairStepper, StandUpPaddling, Surfing, Swim, Velomobile, VirtualRide
  case VirtualRun, Walk, WeightTraining, Wheelchair, Windsurf, Workout, Yoga

object ActivityType:
  def withName(name: String): ActivityType =
    ActivityType.values.find(_.toString == name)
      .getOrElse(throw new NoSuchElementException(s"Unknown ActivityType: $name"))

  def withNameOpt(name: String): Option[ActivityType] =
    ActivityType.values.find(_.toString == name)
