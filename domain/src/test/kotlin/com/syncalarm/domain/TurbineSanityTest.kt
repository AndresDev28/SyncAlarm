package com.syncalarm.domain

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Smoke test proving Turbine is on the classpath AND functional in `:domain`.
 *
 * Turbine + kotlinx-coroutines-test lets us assert on cold flows without manually
 * wiring coroutine scopes. `runTest` provides a virtual-time dispatcher so the test
 * is fast and deterministic. The flow is the simplest possible (`flowOf(1, 2, 3)`)
 * — its only job is to prove `awaitItem()` / `awaitComplete()` work end-to-end.
 */
class TurbineSanityTest {
    @Test
    fun `turbine collects items from a flow and awaits completion`() = runTest {
        flowOf(1, 2, 3).test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }
}
