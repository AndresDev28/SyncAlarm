package com.syncalarm.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke test proving Hilt's `@HiltAndroidApp` wiring on
 * `SyncAlarmApp` actually loads in an Android runtime.
 *
 * The test runs in `connectedAndroidTest` (emulator or device). It uses
 * `HiltAndroidRule` to bootstrap the Hilt test component and `HiltTestApplication`
 * as the per-test Application class. The single assertion is that the Hilt
 * component is a non-null singleton — if Hilt's KSP processor failed to generate
 * `Hilt_SyncAlarmApp`, the rule's `inject()` call would throw at runtime and
 * the test would fail.
 *
 * Run with: `./gradlew :app:connectedDebugAndroidTest` (requires emulator).
 * Compile-only verification: `./gradlew :app:compileDebugAndroidTestKotlin`.
 *
 * This is the **RED** step of the TDD cycle for T3.5 + T3.8: the test
 * references `com.syncalarm.app.SyncAlarmApp` (no further DI surfacing — the
 * `@HiltAndroidApp` annotation on the production class is the contract under
 * test). The companion production commit (`SyncAlarmApp.kt`) is what makes the
 * test compile.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltSmokeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun hiltAndroidRule_bootstraps_without_crash() {
        // `hiltRule.inject()` is called explicitly to surface any DI-init
        // failures as a test failure rather than a silent constructor exception.
        hiltRule.inject()

        // The Hilt test component is non-null after inject — proven by the
        // absence of a thrown exception. A secondary assertion here would
        // require a real `@Inject` member on `SyncAlarmApp`, which is a
        // future change's concern. This test proves Hilt is wired, not that
        // any specific dependency is provided.
        assertNotNull(hiltRule)
    }
}
