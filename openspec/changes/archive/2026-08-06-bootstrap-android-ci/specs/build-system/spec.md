# build-system — MODIFIED Requirements

## ADDED Requirements

### Requirement: CI Workflow Exists

<!-- trace: PRD §6 Clean Architecture/TDD; synthetic-NFR-01-pending: reproducible builds and automated verification. -->
The build system SHALL provide `.github/workflows/ci.yml` and MUST keep `./gradlew test` green. It SHALL run on pushes and pull requests targeting `main`, provision JDK 17 and Android SDK 35, then run tests, `:app:assembleDebug`, and `:data:assembleDebug` in that order.

#### Scenario: Push to main runs CI
- **GIVEN** a commit is pushed to `main`
- **WHEN** GitHub evaluates workflow triggers
- **THEN** the `build-and-test` job SHALL run

#### Scenario: Pull request to main runs CI
- **GIVEN** a non-docs-only pull request targets `main`
- **WHEN** the pull request is opened or updated
- **THEN** the `build-and-test` job SHALL run

#### Scenario: Build steps follow dependency order
- **GIVEN** the CI job starts on a clean runner
- **WHEN** its steps execute
- **THEN** checkout, Temurin JDK 17, Gradle setup, Android SDK 35 setup, and `sdkmanager` SHALL precede every Gradle command
- **AND** `./gradlew test --continue`, `:app:assembleDebug`, and `:data:assembleDebug` SHALL run in that order

#### Scenario: CI status is eligible as a required check
- **GIVEN** branch protection is configured for `main`
- **WHEN** a pull request has not completed `build-and-test` successfully
- **THEN** the pull request SHALL NOT be mergeable

### Requirement: Main Branch Requires Green CI

<!-- trace: PRD §6 Clean Architecture/TDD; synthetic-NFR-01-pending. Cross-reference: tooling-conventions/Main Branch Requires Pull Requests and Green CI. -->
The `main` branch SHALL require pull requests with green CI, and branch protection SHALL block direct pushes.

#### Scenario: Protected main rejects direct pushes
- **GIVEN** branch protection applies to `main`
- **WHEN** a contributor attempts a direct push
- **THEN** GitHub SHALL reject it

#### Scenario: Protected main permits a green pull request
- **GIVEN** a pull request targets `main`
- **WHEN** all required CI checks succeed
- **THEN** branch protection MAY permit merge according to review policy

### Requirement: CodeQL Java and Kotlin Analysis

<!-- trace: PRD §6 third-party security; synthetic-NFR-03-pending: source and dependency security scanning. -->
The security workflow SHOULD analyze `java-kotlin` with CodeQL manual build mode and SHALL expose a required `codeql` status.

#### Scenario: Manual CodeQL build completes in order
- **GIVEN** CodeQL runs for `main` or a pull request to `main`
- **WHEN** the `codeql` job executes
- **THEN** init SHALL precede setup-gradle, the manual Android build, and analyze
- **AND** analysis SHALL upload SARIF without a custom `category`

#### Scenario: CodeQL severity policy blocks merge
- **GIVEN** repository code-scanning protection defines a blocking severity
- **WHEN** CodeQL reports an alert at or above that severity
- **THEN** the `codeql` merge check SHOULD remain non-green until resolved or dismissed

### Requirement: Weekly Trivy Filesystem Scan

<!-- trace: PRD §6 third-party security; synthetic-NFR-03-pending. -->
The security workflow SHOULD scan the repository filesystem for `HIGH,CRITICAL` vulnerabilities every Monday at 06:00 UTC and MAY run manually.

#### Scenario: Weekly schedule starts Trivy
- **GIVEN** cron schedule `0 6 * * 1`
- **WHEN** Monday 06:00 UTC occurs
- **THEN** the Trivy filesystem scan SHALL run

#### Scenario: Initial scan is warning-only
- **GIVEN** Trivy finds a vulnerability
- **WHEN** it runs with `ignore-unfixed: true` and `exit-code: "0"`
- **THEN** findings SHALL be uploaded as SARIF
- **AND** the scan SHALL NOT fail CI

### Requirement: Emulator Runner Forward Compatibility

<!-- trace: PRD RF-06 and §6 Android background reliability. -->
The CI workflow MAY reserve an additive instrumented-test job hook for `reactivecircus/android-emulator-runner@v2` without restructuring `build-and-test`.

#### Scenario: Emulator job is added later
- **GIVEN** `add-alarm-permissions` adds instrumented tests
- **WHEN** an emulator job is introduced
- **THEN** it SHALL reuse the Ubuntu/Gradle setup pattern without changing existing build steps
