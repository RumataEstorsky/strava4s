package strava.models.api

enum SportType:
  case AlpineSki, BackcountrySki, Badminton, Canoeing, Crossfit, EBikeRide, Elliptical
  case EMountainBikeRide, Golf, GravelRide, Handcycle, HighIntensityIntervalTraining
  case Hike, IceSkate, InlineSkate, Kayaking, Kitesurf, MountainBikeRide, NordicSki
  case Pickleball, Pilates, Racquetball, Ride, RockClimbing, RollerSki, Rowing, Run
  case Sail, Skateboard, Snowboard, Snowshoe, Soccer, Squash, StairStepper
  case StandUpPaddling, Surfing, Swim, TableTennis, Tennis, TrailRun, Velomobile
  case VirtualRide, VirtualRow, VirtualRun, Walk, WeightTraining, Wheelchair
  case Windsurf, Workout, Yoga

object SportType:
  def withName(name: String): SportType =
    SportType.values.find(_.toString == name)
      .getOrElse(throw new NoSuchElementException(s"Unknown SportType: $name"))

  def withNameOpt(name: String): Option[SportType] =
    SportType.values.find(_.toString == name)
