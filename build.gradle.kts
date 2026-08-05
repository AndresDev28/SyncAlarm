// SyncAlarm root build script — bootstrap-android-scaffold, PR 1 (scaffold-build).
//
// Every plugin the project uses is declared here with `apply false` so its classpath
// is on the buildscript, then each module re-declares the same alias with `apply true`
// (no version — the catalog is the single source of truth). Plugins live in the version
// catalog under [plugins]; this file references them via `alias(libs.plugins.…)`.
//
// No `subprojects { … }` block is used: per-module build.gradle.kts files own their own
// plugin application, dependency set, and (in Android modules) namespace/SDK config.

plugins {
    // Android Gradle Plugin — application + library flavors.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // Kotlin — JVM (for :domain) + Android (for :app, :data); Compose Compiler plugin
    // ships as a separate Kotlin 2.0+ plugin whose version tracks Kotlin exactly.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // KSP — annotation processing for Hilt (Hilt 2.52+ ships a first-class KSP processor).
    alias(libs.plugins.ksp) apply false

    // Hilt — compile-time DI graph; uses KSP, never KAPT (KAPT is on the deprecation path
    // and is incompatible with Kotlin 2.0+).
    alias(libs.plugins.hilt.android) apply false
}

// No `allprojects {}` / `subprojects {}` blocks: per-module Gradle files own their config.