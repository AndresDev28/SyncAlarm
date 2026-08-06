# build-system Specification

## Purpose

The Gradle build machinery — wrapper, version catalog, JDK 17 toolchain, KSP for annotation processing, and the standard `.gitignore` / `local.properties.example` artifacts. Every other capability depends on this being buildable on a fresh clone with only JDK 17 installed. (Source: PRD §6 NFRs — Android Native Kotlin.)

## Requirements

### Requirement: Gradle Wrapper Pinned

The build SHALL be driven by a Gradle wrapper pinned to a distribution version 8.10 or newer. The wrapper SHALL be runnable with JDK 17 alone.

#### Scenario: `./gradlew --version` reports the pinned distribution

- GIVEN a clean clone with only JDK 17 on `PATH`
- WHEN `./gradlew --version` is invoked
- THEN the printed `Gradle` line SHALL show a version >= 8.10
- AND exit code SHALL be 0

### Requirement: Version Catalog Pins Critical Versions

`gradle/libs.versions.toml` SHALL pin AGP, Kotlin, Hilt, and KSP versions meeting the project minimums (AGP 8.7+, Kotlin 2.0.x, Hilt 2.52+).

#### Scenario: Catalog declares minimum versions

- GIVEN `gradle/libs.versions.toml` is read
- WHEN the `[versions]` block is inspected
- THEN `agp = "8.7+"`, `kotlin = "2.0.x"`, `hilt = "2.52+"` SHALL each appear with a pinned string meeting the minimum

### Requirement: JVM 17 Toolchain

The Kotlin compiler SHALL target JVM 17 across all modules.

#### Scenario: Kotlin compiler targets JVM 17

- GIVEN `./gradlew :app:compileDebugKotlin` runs
- WHEN the compiler arguments are inspected (visible via `--info` or `compileDebugKotlin*.log`)
- THEN `-jvm-target 17` SHALL be present
- AND `:domain` source SHALL also compile with `-jvm-target 17`

### Requirement: KSP for Hilt Annotation Processing

Annotation processing for Hilt SHALL use KSP, NOT KAPT. KAPT is incompatible with Kotlin 2.0+ and is being deprecated.

#### Scenario: Hilt processor is KSP, not KAPT

- GIVEN `:app/build.gradle.kts` and `:data/build.gradle.kts` are read
- WHEN `plugins {}` and `dependencies {}` blocks are inspected
- THEN `id("com.google.devtools.ksp")` SHALL be applied in both modules
- AND `kapt("com.google.dagger:hilt-android-compiler:...")` SHALL NOT be present in either module

### Requirement: `.gitignore` Excludes Build Artifacts

`.gitignore` at the repo root SHALL exclude Android/Gradle/IDE artifacts.

#### Scenario: Required gitignore patterns are present

- GIVEN `.gitignore` is read line-by-line
- WHEN each pattern is searched
- THEN `.gradle/`, `build/`, `local.properties`, `*.iml`, `.idea/`, `.kotlin/`, and `captures/` SHALL each appear as a separate entry

### Requirement: `local.properties.example` Exists

A `local.properties.example` placeholder SHALL exist at the repo root for the SDK path.

#### Scenario: Placeholder exists without leaking a real path

- GIVEN the repo root is listed
- WHEN `local.properties.example` is read
- THEN the file SHALL exist
- AND SHALL NOT contain a real absolute SDK path (e.g. no `/Users/...` or `/home/...`)

### Requirement: Build Command Wired in `openspec/config.yaml`

`openspec/config.yaml` SHALL declare `./gradlew :app:assembleDebug` as `verify.build_command` so `sdd-verify` can run a smoke build.

#### Scenario: Verify build command is set

- GIVEN `openspec/config.yaml` is read
- WHEN the `verify.build_command` key is inspected
- THEN its value SHALL equal `./gradlew :app:assembleDebug`

### Requirement: CI Workflow Exists

The build system SHALL provide `.github/workflows/ci.yml` and MUST keep `./gradlew test` green. The workflow SHALL run on pushes and pull requests targeting `main`, provision JDK 17 and Android SDK 35, then run tests, `:app:assembleDebug`, and `:data:assembleDebug` in that order.

#### Scenario: Push to main runs CI

- GIVEN a commit is pushed to `main`
- WHEN GitHub evaluates workflow triggers
- THEN the `build-and-test` job SHALL run

#### Scenario: Pull request to main runs CI

- GIVEN a non-docs-only pull request targets `main`
- WHEN the pull request is opened or updated
- THEN the `build-and-test` job SHALL run

#### Scenario: Build steps follow dependency order

- GIVEN the CI job starts on a clean runner
- WHEN its steps execute
- THEN checkout, Temurin JDK 17, Gradle setup, Android SDK 35 setup, and `sdkmanager` SHALL precede every Gradle command
- AND `./gradlew test --continue`, `:app:assembleDebug`, and `:data:assembleDebug` SHALL run in that order

#### Scenario: CI status is eligible as a required check

- GIVEN branch protection is configured for `main`
- WHEN a pull request has not completed `build-and-test` successfully
- THEN the pull request SHALL NOT be mergeable

### Requirement: Main Branch Requires Green CI

The `main` branch SHALL require pull requests with green CI, and branch protection SHALL block direct pushes.

#### Scenario: Protected main rejects direct pushes

- GIVEN branch protection applies to `main`
- WHEN a contributor attempts a direct push
- THEN GitHub SHALL reject it

#### Scenario: Protected main permits a green pull request

- GIVEN a pull request targets `main`
- WHEN all required CI checks succeed
- THEN branch protection MAY permit merge according to review policy

### Requirement: CodeQL Java and Kotlin Analysis

The security workflow SHALL analyze `java-kotlin` with CodeQL in manual build mode and SHALL expose a required `codeql` status. The `init` step SHALL NOT define a `category:` (free-form string, not a query-suite selector); the default security suite covers Java + Kotlin.

#### Scenario: Manual CodeQL build completes in order

- GIVEN CodeQL runs for `main` or a pull request to `main`
- WHEN the `codeql` job executes
- THEN init with `build-mode: manual` SHALL precede setup-gradle, the manual Android build, and analyze
- AND analysis SHALL upload SARIF without a custom `category` on either init or analyze

#### Scenario: CodeQL severity policy blocks merge

- GIVEN repository code-scanning protection defines a blocking severity
- WHEN CodeQL reports an alert at or above that severity
- THEN the `codeql` merge check SHALL remain non-green until resolved or dismissed

### Requirement: Weekly Trivy Filesystem Scan

The security workflow SHALL scan the repository filesystem for `HIGH,CRITICAL` vulnerabilities weekly (cron `0 6 * * 1`) and on every push to `main`. The scan SHALL emit SARIF and upload to the GitHub Security tab. The scan SHALL be skipped on pull requests.

#### Scenario: Weekly schedule starts Trivy

- GIVEN cron schedule `0 6 * * 1`
- WHEN Monday 06:00 UTC occurs
- THEN the Trivy filesystem scan SHALL run

#### Scenario: Initial scan is warning-only

- GIVEN Trivy finds a vulnerability
- WHEN it runs with `ignore-unfixed: true`, `exit-code: "0"`, and `format: sarif`
- THEN findings SHALL be uploaded as SARIF to the GitHub Security tab
- AND the scan SHALL NOT fail CI

#### Scenario: Trivy is skipped on pull requests

- GIVEN a pull request is opened or updated
- WHEN the workflow triggers evaluate
- THEN the Trivy job SHALL be skipped via `if: github.event_name != 'pull_request'`
- AND the CodeQL job SHALL still run

### Requirement: Emulator Runner Forward Compatibility

The CI workflow SHALL reserve an additive instrumented-test job hook for `reactivecircus/android-emulator-runner@v2` without restructuring `build-and-test`. The placeholder is documented in the workflow YAML and consumed by the `add-alarm-permissions` change.

#### Scenario: Emulator job is added later

- GIVEN `add-alarm-permissions` adds instrumented tests
- WHEN an emulator job is introduced
- THEN it SHALL reuse the Ubuntu/Gradle setup pattern without changing existing build steps