package com.syncalarm.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

/**
 * JVM unit test asserting `openspec/config.yaml` satisfies the `build-system`
 * spec requirement "Build Command Wired in `openspec/config.yaml`".
 *
 * The test reads the config file from the repo root (relative to the module
 * working directory: `../../openspec/config.yaml` from the `:app` module).
 * Two assertions back this test:
 *
 *   1. `verify.test_command` equals `./gradlew test` (the wired test runner).
 *   2. `verify.build_command` equals `./gradlew :app:assembleDebug` (the wired
 *      build command per design.md §Build & Verification Commands).
 *
 * **Important**: this test is module-relative. The `:app` module's working
 * directory is `app/`; the repo root is `../..`. The path is stable across
 * machines and CI.
 *
 * This is the **RED** step of the TDD cycle for the `openspec/config.yaml`
 * portion of T3.10: the config still has `verify.build_command:
 * "./gradlew :domain:test"` from PR 2, so the second assertion fails. The
 * companion production commit (modifying `openspec/config.yaml`) is what makes
 * the test go GREEN.
 *
 * Run with: `./gradlew :app:testDebugUnitTest`
 */
@DisplayName("openspec/config.yaml has the wired build and test commands")
class OpenSpecConfigWiringTest {

    @Test
    fun `verify commands are wired to gradle test and assembleDebug`() {
        val config = readConfig()

        assertThat(config)
            .describedAs("verify.test_command must be './gradlew test'")
            .contains("""test_command: "./gradlew test"""")
            .describedAs("verify.build_command must be './gradlew :app:assembleDebug'")
            .contains("""build_command: "./gradlew :app:assembleDebug"""")
    }

    @Test
    fun `apply commands are wired to gradle test`() {
        val config = readConfig()

        assertThat(config)
            .describedAs("apply.tdd_command must be './gradlew test'")
            .contains("""tdd_command: "./gradlew test"""")
            .describedAs("apply.test_command must be './gradlew test'")
            .contains("""test_command: "./gradlew test"""")
    }

    private fun readConfig(): String {
        val configFile = File("../../openspec/config.yaml")
        check(configFile.exists()) {
            "openspec/config.yaml not found at ${configFile.absolutePath} — " +
                "module working directory is expected to be the :app module root."
        }
        return configFile.readText()
    }
}
