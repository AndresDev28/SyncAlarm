package com.syncalarm.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Smoke test proving MockK is on the classpath AND functional in `:domain`.
 *
 * Mocks a nested interface (rather than a concrete class or an Android type) to keep
 * this test JVM-pure and free of Android stubs. The Clock interface is the smallest
 * possible abstraction that exercises `mockk<>()`, `every { ... } returns`, and
 * `verify(exactly = ...)`.
 */
class MockKSanityTest {
    private interface Clock {
        fun now(): Long
    }

    @Test
    fun `mockk stubs an interface and verifies the interaction`() {
        val clock = mockk<Clock>()
        every { clock.now() } returns 42L

        val result = clock.now()

        assertEquals(42L, result)
        verify(exactly = 1) { clock.now() }
    }
}
