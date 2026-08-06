package com.syncalarm.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

/**
 * JVM unit test asserting `SyncAlarmApp` satisfies the `app-shell` contract
 * defined by the `app-shell` spec requirement "Hilt Application Entry Point".
 *
 * The test reads the production source file from the module working directory
 * (`src/main/kotlin/com/syncalarm/app/SyncAlarmApp.kt`) — Gradle's `Test` task
 * runs from the module root, so the path is stable across machines and CI.
 *
 * Two assertions back this test:
 *
 *   1. The class is annotated with `@HiltAndroidApp` (the contract that
 *      activates Hilt's compile-time graph for the host application).
 *   2. The class extends `android.app.Application` (the contract the OS
 *      requires to instantiate the class via reflection on app launch).
 *
 * This is the **RED** step of the TDD cycle for T3.5: the production file
 * does not exist yet, so `readText()` throws `FileNotFoundException`. The
 * companion production commit (`SyncAlarmApp.kt`) is what makes the test go
 * GREEN.
 *
 * Run with: `./gradlew :app:testDebugUnitTest`
 */
@DisplayName("SyncAlarmApp satisfies the app-shell contract")
class SyncAlarmAppStructureTest {

    @Test
    fun `SyncAlarmApp is annotated with HiltAndroidApp and extends Application`() {
        val source = readSyncAlarmAppSource()

        assertThat(source)
            .describedAs("SyncAlarmApp.kt must declare @HiltAndroidApp on the class")
            .contains("@HiltAndroidApp")
            .describedAs("SyncAlarmApp.kt must extend android.app.Application")
            .contains("Application")
    }

    private fun readSyncAlarmAppSource(): String {
        val sourceFile = File("src/main/kotlin/com/syncalarm/app/SyncAlarmApp.kt")
        check(sourceFile.exists()) {
            "SyncAlarmApp.kt not found at ${sourceFile.absolutePath} — " +
                "module working directory is expected to be the :app module root."
        }
        return sourceFile.readText()
    }
}
