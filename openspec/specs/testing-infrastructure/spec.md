# testing-infrastructure Specification

## Purpose

Wire JUnit 5 + MockK + Turbine + AssertJ in `:domain` so the test stack is provably functional end-to-end; mirror the `:domain` test config in `:data`; add Compose UI test rule + Hilt test rule in `:app`. This capability is the GATE that flips `openspec/config.yaml` `testing.strict_tdd` from `false` to `true` so every future `sdd-apply` run enforces RED-GREEN-REFACTOR.

## Requirements

### Requirement: `:domain` Declares the Test Stack

`:domain/build.gradle.kts` SHALL declare JUnit 5 (`junit-jupiter`), MockK, Turbine, and AssertJ as `testImplementation` dependencies.

#### Scenario: All four libraries are declared in `:domain`

- GIVEN `:domain/build.gradle.kts` is read
- WHEN the `dependencies {}` block is inspected
- THEN `testImplementation` entries SHALL exist for `org.junit.jupiter:junit-jupiter`, `io.mockk:mockk`, `app.cash.turbine:turbine`, and `org.assertj:assertj-core`

### Requirement: `:domain` Sanity Test Passes

`:domain` SHALL contain a `SanityTest` that uses JUnit 5 + AssertJ and passes when `./gradlew :domain:test` runs.

#### Scenario: SanityTest is discovered and passes

- GIVEN `domain/src/test/kotlin/com/syncalarm/domain/SanityTest.kt` is present
- WHEN `./gradlew :domain:test` runs
- THEN at least one `@Test`-annotated method SHALL be discovered
- AND its assertion SHALL report `PASSED`
- AND the task SHALL exit 0

### Requirement: MockK is Provably Functional in `:domain`

`:domain` SHALL contain at least one test that uses MockK (proves the library is on the classpath AND functional).

#### Scenario: MockK smoke test exists and passes

- GIVEN `domain/src/test/kotlin/com/syncalarm/domain/` is searched
- WHEN `rg "io\.mockk\.(mockk|every|verify)"` is run
- THEN at least one match SHALL be returned
- AND `./gradlew :domain:test` SHALL exit 0

### Requirement: Turbine is Provably Functional in `:domain`

`:domain` SHALL contain at least one test that uses Turbine to assert a `Flow` (proves Turbine is on the classpath AND functional).

#### Scenario: Turbine smoke test exists and passes

- GIVEN `domain/src/test/kotlin/com/syncalarm/domain/` is searched
- WHEN `rg "app\.cash\.turbine\."` is run
- THEN at least one match SHALL be returned
- AND `./gradlew :domain:test` SHALL exit 0

### Requirement: `:data` Mirrors the Test Config Stub

`:data` SHALL declare the same test stack as `:domain` (JUnit 5, MockK, Turbine, AssertJ) so future `:data` tests inherit the same toolkit. No tests are required in this change.

#### Scenario: `:data` declares all four libraries

- GIVEN `:data/build.gradle.kts` is read
- WHEN `testImplementation` entries are inspected
- THEN JUnit 5, MockK, Turbine, and AssertJ SHALL each be present

### Requirement: `:app` Compose UI Test Rule Configured

`:app` test config SHALL include the Compose UI test rule (`createComposeRule`) and the Hilt Android test rule (`HiltAndroidRule`) so future `sdd-apply` runs can write Compose tests without re-wiring tooling.

#### Scenario: Compose + Hilt test rules are declared

- GIVEN `:app/build.gradle.kts` is read
- WHEN `androidTestImplementation` and `testImplementation` are inspected
- THEN `androidx.compose.ui:ui-test-junit4` and `com.google.dagger:hilt-android-testing` SHALL each appear

### Requirement: `./gradlew test` is Wired

`openspec/config.yaml` SHALL declare `./gradlew test` as `apply.tdd_command`, `apply.test_command`, and `verify.test_command` so `sdd-apply` and `sdd-verify` can run the test suite.

#### Scenario: Test command wired in three locations

- GIVEN `openspec/config.yaml` is read
- WHEN the `apply.tdd_command`, `apply.test_command`, and `verify.test_command` keys are inspected
- THEN each value SHALL equal `./gradlew test`

### Requirement: Strict TDD Gate Flips to True

`openspec/config.yaml` `testing.strict_tdd` SHALL be `true` after this change is archived. This is the gate that unlocks strict TDD for every future `sdd-apply` run.

#### Scenario: Strict TDD is enabled

- GIVEN the change is archived
- WHEN `openspec/config.yaml` is read
- THEN `testing.strict_tdd: true` SHALL be present
- AND `testing.runner.command: "./gradlew test"` SHALL be present
- AND `testing.runner.available: true` SHALL be present