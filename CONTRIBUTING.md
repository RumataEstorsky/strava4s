# Contributing

Thank you for your interest in contributing to the Strava Client library!

## Quick Start

```bash
# Clone the repository and go inside
cd strava4s

# Compile
sbt compile

# Run tests (note: tests only run on Scala 2.13.x)
sbt test

# To explicitly test with Scala 2.13
sbt ++2.13.18 test

# Format code
sbt scalafmt

# Run specific test
sbt "testOnly strava.models.JsonParsingSpec"
```

**Note:** Tests are currently only compatible with Scala 2.13.x. There are known issues with the test suite on Scala 3.x. The library itself compiles and works with both versions, but testing is limited to 2.13.x.

## Ways to Contribute

### Reporting Bugs

1. Check [existing issues](https://github.com/vsvechikhin/strava4s/issues)
2. Create a new issue with:
   - Clear, descriptive title
   - Steps to reproduce
   - Expected vs actual behavior
   - Your environment (Scala version, OS)
   - Code sample if applicable

### Suggesting Features

1. Check existing feature requests
2. Create an issue describing:
   - Problem you're solving
   - Proposed solution
   - Alternative solutions considered
   - Links to relevant Strava API docs

### Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes following our standards
4. Add tests for new functionality
5. Ensure all tests pass: `sbt ++2.13.18 test` (tests only run on Scala 2.13.x)
6. Format code: `sbt scalafmt`
7. Update documentation
8. Commit with clear messages
9. Push and create a Pull Request

## Coding Standards

### Scala Style

- Follow standard Scala style guidelines
- Use meaningful names
- Keep functions small and focused
- Prefer immutability
- Use pattern matching over conditionals
- Document public APIs with ScalaDoc

### Example

```scala
/**
 * Retrieves detailed information about an activity.
 *
 * @param id The activity identifier
 * @param includeAllEfforts Include all segment efforts
 * @return Either an error or the detailed activity
 */
def getActivityById(
  id: Long,
  includeAllEfforts: Boolean = false
): F[Either[StravaError, DetailedActivity]]
```

### Testing

- Write tests for all new features
- Test both success and error cases
- Use real JSON fixtures from `src/test/resources/strava/`
- Follow the AAA pattern (Arrange, Act, Assert)

```scala
"ActivitiesApi" should "parse activity details" in {
  // Arrange
  val json = loadJson("get-activity-getactivitybyid.json")
  
  // Act
  val result = decode[DetailedActivity](json)
  
  // Assert
  result shouldBe a[Right[_, _]]
  result.value.id shouldBe Some(12345)
}
```

### Documentation

- Update README.md for user-facing changes
- Add ScalaDoc for public APIs
- Include code examples
- Update GUIDE.md for new patterns
- Update CHANGELOG.md

## Project Structure

```
strava4s/
├── src/
│   ├── main/scala/strava/
│   │   ├── StravaClient.scala
│   │   ├── api/         # API implementations
│   │   ├── auth/        # Authentication
│   │   ├── core/        # Core types
│   │   ├── http/        # HTTP layer
│   │   └── models/      # Data models
│   └── test/
│       ├── scala/strava/ # Tests
│       └── resources/strava/ # JSON fixtures
├── examples/            # Usage examples
└── scripts/             # Build scripts
```

## Adding New Features

### New API Endpoint

1. Check [Strava API docs](https://developers.strava.com/docs/reference/)
2. Add method to appropriate API class (e.g., `ActivitiesApi`)
3. Add models if needed
4. Add JSON fixture to `src/test/resources/strava/`
5. Add tests using the fixture
6. Update GUIDE.md with usage example

Example:

```scala
// In ActivitiesApi
def getNewEndpoint(id: Long): F[Either[StravaError, NewModel]] = {
  httpClient.get[NewModel](s"activities/$id/new-endpoint")
}

// In test suite
"ActivitiesApi" should "get new endpoint data" in {
  val json = loadJson("get-new-endpoint.json")
  val result = decode[NewModel](json)
  result shouldBe a[Right[_, _]]
}
```

### New Model

1. Add case class to appropriate file in `strava.models.api`
2. Add Circe encoder/decoder
3. Add JSON fixture
4. Add parsing test

## Testing with Real API

To test with actual Strava API:

```bash
# 1. Create Strava app at https://www.strava.com/settings/api
# 2. Set environment variables
export STRAVA_CLIENT_ID="your-client-id"
export STRAVA_CLIENT_SECRET="your-client-secret"

# 3. Authenticate
sbt "runMain examples.AuthenticationExample"

# 4. Run examples
sbt "runMain examples.BasicExample"
```

## Code Quality Tools

### Scalafmt

Format code automatically:

```bash
sbt scalafmt
```

Configuration in `.scalafmt.conf`

### Scalafix

Lint and refactor:

```bash
sbt "scalafix RemoveUnused"
```

Configuration in `.scalafix.conf`

### Coverage

Check test coverage:

```bash
sbt clean coverage test coverageReport
```

View report: `target/scala-x.x/scoverage-report/index.html`

## Git Commit Messages

Follow conventional commit format:

- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation only
- `style:` Formatting changes
- `refactor:` Code restructuring
- `test:` Adding tests
- `chore:` Build/tooling changes

Example:

```
feat: add pagination helper for activities

Add fetchAll and paginate methods to simplify fetching
multiple pages of activities.

Closes #42
```

## Pull Request Process

1. **Update documentation** if needed
2. **Add tests** for new functionality
3. **Ensure tests pass**: `sbt ++2.13.18 test` (Scala 2.13 only)
4. **Format code**: `sbt scalafmt`
5. **Update CHANGELOG.md**
6. **Create PR** with clear description
7. **Respond to feedback** from reviewers

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
How was this tested?

## Checklist
- [ ] Tests added/updated (Scala 2.13.x)
- [ ] Documentation updated
- [ ] All tests pass (`sbt ++2.13.18 test`)
- [ ] Code formatted
- [ ] CHANGELOG.md updated
```

## Release Process

(For maintainers)

1. Update version in `build.sbt`
2. Update `CHANGELOG.md`
3. Create git tag: `git tag vX.Y.Z`
4. Push tag: `git push origin vX.Y.Z`
5. GitHub Actions will handle the release

## Code of Conduct

- Be respectful and professional
- Welcome newcomers
- Provide constructive feedback
- Focus on what is best for the community
- Show empathy towards others

## Questions?

- Create an issue with the `question` label
- Check existing documentation:
  - [README.md](README.md)
  - [GUIDE.md](GUIDE.md)
  - [ARCHITECTURE.md](ARCHITECTURE.md)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
