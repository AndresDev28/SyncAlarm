# app-shell Specification

## Purpose

The `:app` module shell — `AndroidManifest.xml`, a Hilt `Application` class, a Compose `MainActivity`, a Material 3 theme entry, and a "SyncAlarm" smoke screen that proves Compose, Hilt, and the Activity lifecycle are wired end-to-end on a fresh install. (Source: PRD §6 NFRs — Android Native Kotlin, Compose, Hilt.)

## Requirements

### Requirement: Hilt Application Entry Point

The system SHALL provide an `Application` subclass annotated `@HiltAndroidApp` and registered as `android:name` in `app/src/main/AndroidManifest.xml`.

#### Scenario: Hilt application class is registered

- GIVEN `:app` builds successfully
- WHEN `app/src/main/AndroidManifest.xml` is read
- THEN `android:name=".SyncAlarmApp"` SHALL be present on `<application>`
- AND `app/src/main/kotlin/com/syncalarm/app/SyncAlarmApp.kt` SHALL declare `@HiltAndroidApp` on a class extending `android.app.Application`

#### Scenario: Hilt graph is generated at build time

- GIVEN the project compiles
- WHEN `./gradlew :app:assembleDebug` runs
- THEN Hilt SHALL generate `Hilt_SyncAlarmApp` (visible in build output or `app/build/generated/.../hilt/`)
- AND the build SHALL exit 0

### Requirement: Compose MainActivity Entry Point

The system SHALL provide a `MainActivity` annotated `@AndroidEntryPoint` that calls `setContent { MaterialTheme { ... } }`.

#### Scenario: MainActivity wires Compose content

- GIVEN the app launches on a fresh install
- WHEN `MainActivity.onCreate()` runs
- THEN `setContent` SHALL be invoked with a `@Composable` lambda
- AND the lambda SHALL wrap its content in `androidx.compose.material3.MaterialTheme`

### Requirement: AndroidManifest Declares Critical Permissions

The system SHALL declare five `<uses-permission>` entries in `app/src/main/AndroidManifest.xml`: `READ_CALENDAR`, `SCHEDULE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `INTERNET`, and `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

#### Scenario: All five permissions are declared

- GIVEN the APK is built
- WHEN `app/src/main/AndroidManifest.xml` is parsed
- THEN the file SHALL contain exactly five `<uses-permission>` elements matching the names above

#### Scenario: Forward-compat — exact-alarm runtime flow is deferred

- GIVEN `SCHEDULE_EXACT_ALARM` is declared as `<uses-permission>` only (no `<uses-permission-sdk-23>` override)
- WHEN the change is archived
- THEN a follow-up change SHALL be tracked for the `Build.VERSION.SDK_INT >= 31` runtime permission flow (`SCHEDULE_EXACT_ALARM` API 31, `USE_EXACT_ALARM` API 33) — minSdk 26 does NOT unlock these

### Requirement: Compose Material 3 Theme Entry

The system SHALL render UI through a Material 3 theme; the smoke screen SHALL display the text "SyncAlarm".

#### Scenario: Smoke screen is visible on first launch

- GIVEN the user launches the app on a fresh install
- WHEN the Compose tree composes
- THEN a `Text("SyncAlarm")` node SHALL be present
- AND it SHALL be discoverable via `composeTestRule.onNodeWithText("SyncAlarm")` once Compose test rule is wired (see `testing-infrastructure`)
- AND runtime verification via `connectedAndroidTest` is deferred to the follow-up change `bootstrap-android-ci` (the local dev environment has no Android emulator; the connectedAndroidTest task requires a device target which lands in `bootstrap-android-ci`)

#### Scenario: Material 3 is the active theme

- GIVEN `app/src/main/kotlin/com/syncalarm/app/MainActivity.kt` is read
- WHEN the imports are inspected
- THEN `androidx.compose.material3.MaterialTheme` SHALL be imported
- AND `androidx.compose.material.MaterialTheme` (Material 2) and `androidx.compose.material.MaterialTheme` (M2 alias) SHALL NOT be imported