package com.syncalarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single entry-point Activity for SyncAlarm.
 *
 * `@AndroidEntryPoint` makes Hilt create the `ActivityComponent` for this
 * Activity and surface `@Inject` members / `@HiltViewModel`-backed state to
 * the Composable tree. The generated `Hilt_MainActivity` (from Hilt's KSP
 * processor) is the actual class loaded by the OS; this user-authored class
 * is the KSP input.
 *
 * The content is intentionally minimal — a single `Text("SyncAlarm")` inside
 * a Material 3 theme. This is the `app-shell` spec's "smoke screen" contract:
 * it proves Compose + Hilt + the Activity lifecycle are wired end-to-end on a
 * fresh install. Real screens, navigation, and theming land in follow-up
 * changes (`add-room-persistence`, `add-rule-engine`, etc.).
 *
 * Contract under test: `MainActivityStructureTest` (JVM unit test) asserts
 * this file declares `@AndroidEntryPoint`, calls `setContent`, imports
 * `androidx.compose.material3.MaterialTheme`, and renders `Text("SyncAlarm")`.
 * `ComposeSmokeTest` (instrumented) verifies the rendered tree at runtime.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Text("SyncAlarm")
            }
        }
    }
}
