package com.syncalarm.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Host `Application` for the SyncAlarm Android app.
 *
 * The `@HiltAndroidApp` annotation triggers Hilt's KSP processor to generate
 * `Hilt_SyncAlarmApp` (visible in `app/build/generated/ksp/debug/.../hilt/`).
 * That generated class is the actual class loaded by the OS at startup; the
 * user-authored class is the KSP input. Hilt's Gradle plugin swaps the
 * generated class into the manifest via the `transformAndroidLibraries` API
 * so the AndroidManifest's `android:name=".SyncAlarmApp"` still resolves.
 *
 * The class body is intentionally empty today: Hilt's compile-time graph
 * registers `@HiltAndroidApp` and binds the singleton component. Real
 * per-app initialization (logging, BuildConfig logging, early WorkManager
 * registration, etc.) lands in a follow-up change.
 *
 * Contract under test: `SyncAlarmAppStructureTest` (JVM unit test) asserts
 * this file contains `@HiltAndroidApp` and extends `Application`.
 */
@HiltAndroidApp
class SyncAlarmApp : Application()
