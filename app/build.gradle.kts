// SyncAlarm :app module — bootstrap-android-scaffold, PR 3 (scaffold-app).
//
// `:app` is the Android application module. It owns the APK that the user installs.
// Module type: `com.android.application` (NOT library). Namespace: `com.syncalarm.app`
// (MUST differ from `:data`'s `com.syncalarm.data`).
//
// Plugins layered onto this module:
//   * `com.android.application` — AGP's app module type.
//   * `org.jetbrains.kotlin.android` — Kotlin compiler for Android targets.
//   * `org.jetbrains.kotlin.plugin.compose` — Kotlin 2.0+ Compose Compiler plugin.
//     Its version equals the Kotlin version (it is the same plugin id); do NOT
//     version-align manually (see design.md #1483 rule 1 mirror).
//   * `com.google.devtools.ksp` — KSP processor for Hilt (KAPT is broken on
//     Kotlin 2.0+).
//   * `com.google.dagger.hilt.android` — Hilt's Gradle plugin auto-applies the
//     Hilt transform to `:app` so generated `Hilt_*` classes compile without
//     manual `kapt(...)` / `ksp(...)` configuration of the compiler artifact.
//
// Dependencies:
//   * `:domain` (Clean Architecture: UI talks to domain via interfaces).
//   * `:data` (repos, adapters).
//   * Compose BOM (UI framework + Material 3).
//   * Hilt (the host application carries the singleton component; `hilt-compiler`
//     is wired via KSP).
//   * Coroutines (the `:app` lifecycle harnesses suspend functions).
//   * Compose UI test rule + Hilt Android test rule — wired here so future
//     `sdd-apply` runs can write Compose + Hilt tests without re-adding tooling.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.syncalarm.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.syncalarm.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // --- Clean Architecture wiring ---
    implementation(project(":domain"))
    implementation(project(":data"))

    // --- AndroidX core / lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)

    // --- Compose stack ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Platform Material 3 components (View-system) — supplies the
    // `Theme.Material3.DayNight.NoActionBar` parent referenced from
    // `themes.xml`. The Compose tree renders Material 3 itself via
    // `MaterialTheme`; this dependency is only for the platform launch theme.
    implementation(libs.google.material)

    // The official `material-icons-core` is intentionally NOT pulled in: this
    // scaffold ships only a `Text("SyncAlarm")` smoke screen. Icons land alongside
    // the first real UI change.

    // --- Hilt (host application) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // --- Coroutines ---
    implementation(libs.bundles.coroutines)

    // --- Unit test stack (`:domain` stack mirrored for `:app` JVM tests) ---
    testImplementation(libs.bundles.unit.test)

    // --- Android instrumented test stack (Compose + Hilt UI tests) ---
    // The Compose UI test rule (`createComposeRule` / `createAndroidComposeRule`)
    // and the Hilt Android test rule (`HiltAndroidRule`) are wired here so future
    // `sdd-apply` runs can author Compose + Hilt tests without re-adding tooling.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
