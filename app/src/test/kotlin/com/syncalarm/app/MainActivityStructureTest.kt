package com.syncalarm.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

/**
 * JVM unit test asserting that `MainActivity` satisfies the `app-shell` spec
 * requirements:
 *
 *   * Annotated with `@AndroidEntryPoint` (Hilt's hook for the Activity
 *     component).
 *   * Contains `setContent { ... }` inside its `onCreate` (Compose
 *     activation).
 *   * Wraps its content in `androidx.compose.material3.MaterialTheme` (M3
 *     theme; the `app-shell` spec explicitly forbids Material 2 imports).
 *   * Renders `Text("SyncAlarm")` (the smoke-screen contract).
 *
 * The test reads the production source file from the module working directory
 * (`src/main/kotlin/com/syncalarm/app/MainActivity.kt`) — Gradle's `Test` task
 * runs from the module root, so the path is stable across machines and CI.
 *
 * This is the **RED** step of the TDD cycle for T3.6: the production file
 * does not exist yet, so `readText()` throws `FileNotFoundException`. The
 * companion production commit (`MainActivity.kt`) is what makes the test go
 * GREEN.
 *
 * Run with: `./gradlew :app:testDebugUnitTest`
 */
@DisplayName("MainActivity satisfies the app-shell contract")
class MainActivityStructureTest {

    @Test
    fun `MainActivity is annotated with AndroidEntryPoint and renders SyncAlarm inside MaterialTheme`() {
        val source = readMainActivitySource()

        assertThat(source)
            .describedAs("MainActivity must be annotated with @AndroidEntryPoint")
            .contains("@AndroidEntryPoint")
            .describedAs("MainActivity must call setContent to activate Compose")
            .contains("setContent")
            .describedAs("MainActivity must wrap content in Material3 MaterialTheme")
            .contains("androidx.compose.material3.MaterialTheme")
            .describedAs("MainActivity must render the Text(\"SyncAlarm\") smoke screen")
            .contains("SyncAlarm")
    }

    @Test
    fun `MainActivity does not import Material 2 themes`() {
        val source = readMainActivitySource()

        // AndroidX Compose Material 2 lives under `androidx.compose.material.*` (NOT
        // `androidx.compose.material3.*`). The `app-shell` spec forbids M2 imports.
        assertThat(source)
            .describedAs("MainActivity must not import Material 2's MaterialTheme")
            .doesNotContain("androidx.compose.material.MaterialTheme")
            .doesNotContain("androidx.compose.material.Text")
    }

    private fun readMainActivitySource(): String {
        val sourceFile = File("src/main/kotlin/com/syncalarm/app/MainActivity.kt")
        check(sourceFile.exists()) {
            "MainActivity.kt not found at ${sourceFile.absolutePath} — " +
                "module working directory is expected to be the :app module root."
        }
        return sourceFile.readText()
    }
}
