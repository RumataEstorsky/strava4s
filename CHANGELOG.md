# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Code quality tools configuration (scalafmt, scalafix, wartremover)
- Code coverage reporting with scoverage
- Publishing configuration for Maven Central
- Pagination helper utilities

### Changed
- Enhanced examples with better error handling
- Improved documentation structure and consistency
- Consolidated documentation files for better organization

### Fixed
- Documentation inconsistencies

### Known Issues
- Test suite only runs on Scala 2.13.x; Scala 3.x has test compatibility issues (library itself works fine)

## [1.0.0] - 2025-10-11

### Added
- Initial release of Strava Client for Scala
- Comprehensive API coverage for Activities, Athletes, Segments, Clubs, and Streams
- OAuth authentication with automatic token refresh
- Built-in rate limiting (100 requests per 15 minutes, 1000 per day)
- Automatic retry logic for failed requests
- Type-safe API with explicit error handling using `Either[StravaError, T]`
- Resource-based API with automatic cleanup
- Functional programming with Cats Effect
- Comprehensive test suite with real Strava API response examples
- File-based and in-memory token storage options
- Configurable HTTP client settings (timeouts, retries, rate limiting)

### Features
- **ActivitiesApi**: List, get, create, update activities; retrieve comments, kudos, laps, zones
- **AthletesApi**: Get athlete profile, stats, update profile
- **SegmentsApi**: Get segments, star/unstar, explore, get efforts
- **ClubsApi**: Get club details, activities, members, admins
- **StreamsApi**: Get activity/segment/route streams (GPS, heart rate, power, etc.)

### Documentation
- Complete API reference in README
- Architecture documentation with diagrams
- Contributing guidelines
- User guide with usage patterns
- Working examples for common use cases

---

## Release Guidelines

### Version Numbers
- **Major (X.0.0)**: Breaking API changes
- **Minor (1.X.0)**: New features, backward compatible
- **Patch (1.0.X)**: Bug fixes, backward compatible

### Release Process
1. Update version in `build.sbt`
2. Update this CHANGELOG.md
3. Run full test suite: `sbt test`
4. Create git tag: `git tag v1.0.0`
5. Push tag: `git push origin v1.0.0`
6. GitHub Actions will create the release

### Commit Message Format
- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation changes
- `style:` Code style/formatting changes
- `refactor:` Code refactoring
- `test:` Test additions/modifications
- `chore:` Build/maintenance tasks

[1.0.0]: https://github.com/vsvechikhin/strava4s/releases/tag/v1.0.0
