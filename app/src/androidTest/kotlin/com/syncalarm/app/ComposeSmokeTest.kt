package com.syncalarm.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.syncalarm.app.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke test asserting that `MainActivity` renders the "SyncAlarm"
 * text on first compose — this is the happy-path proof that Compose + Hilt +
 * the Activity lifecycle are wired end-to-end on a fresh install.
 *
 * The test uses `createAndroidComposeRule` (the Activity-attached variant of
 * the Compose UI test rule) so it can find the `MainActivity` and exercise
 * its `setContent` block. `HiltAndroidRule` is included so the Activity can
 * resolve its `@AndroidEntryPoint` graph at runtime.
 *
 * The test asserts that the `Text("SyncAlarm")` node is present in the
 * composed tree — not just that the tree exists. That is the
 * `app-shell` spec requirement "Smoke screen is visible on first launch".
 *
 * Run with: `./gradlew :app:connectedDebugAndroidTest` (requires emulator).
 * Compile-only verification: `./gradlew :app:compileDebugAndroidTestKotlin`.
 *
 * This is the **RED** step of the TDD cycle for T3.6 + T3.7: the test
 * references `com.syncalarm.app.MainActivity`, which doesn't exist yet. The
 * companion production commit (`MainActivity.kt`) is what makes the test
 * compile.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ComposeSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun smokeScreen_displays_SyncAlarm_text() {
        composeTestRule.onNodeWithText("SyncAlarm").assertExists()
    }
}
