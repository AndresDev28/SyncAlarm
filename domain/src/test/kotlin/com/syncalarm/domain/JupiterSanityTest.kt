package com.syncalarm.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Smoke test proving JUnit 5 (Jupiter) is on the classpath AND functional in `:domain`.
 *
 * This test deliberately stays trivial — its only job is to prove the runner discovers
 * a `@Test`-annotated method and that `assertEquals` reports PASSED. Real domain
 * scenarios (alarm validation, rule resolution, etc.) land in follow-up changes.
 *
 * Run with: `./gradlew :domain:test`
 */
class JupiterSanityTest {
    @Test
    @DisplayName("JUnit 5 Jupiter runs and asserts correctly")
    fun `arithmetic works`() {
        assertEquals(2, 1 + 1)
    }
}
