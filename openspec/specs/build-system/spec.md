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