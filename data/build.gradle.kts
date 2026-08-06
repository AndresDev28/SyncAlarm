// SyncAlarm :data module — bootstrap-android-scaffold, PR 3 (scaffold-app).
//
// `:data` is the Clean Architecture data layer (Android library). It is a structural
// stub today: the module exists, builds, and exposes the dependency direction
// (`:data` → `:domain`) so future changes (Room, DataStore, calendar adapters) have
// a target. No production sources are added in this PR — that work lands in
// `add-room-persistence`, `add-oauth-google-calendar`, etc.
//
// Module type: `com.android.library` (NOT `com.android.application`). The
// `Namespace` is `com.syncalarm.data` and MUST be unique across the project
// (`:app` uses `com.syncalarm.app`).
//
// Hilt wiring is applied because the eventual `@Module @InstallIn(SingletonComponent::class)`
// classes will live here. KSP is the Hilt processor (KAPT is broken on Kotlin 2.0+).
//
// Compile/target SDKs come from the version catalog (`libs.versions.toml`); no
// hard-coded values per the workshop convention.

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.syncalarm.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
    // Clean Architecture: data depends on domain only. The reverse direction is
    // structurally impossible because :domain is a `kotlin("jvm")` module with
    // no Android classpath.
    implementation(project(":domain"))

    // Hilt — compile-time DI graph (Android-library-flavored). The
    // `hilt-compiler` is wired via KSP so generated code lives on the
    // debug/release classpath without ever touching KAPT.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Mirrors :domain's test stack so future :data tests inherit the same
    // toolkit. No tests are required in this PR per design.md.
    testImplementation(libs.bundles.unit.test)
}
