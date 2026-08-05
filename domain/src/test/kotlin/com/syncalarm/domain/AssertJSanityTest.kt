package com.syncalarm.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Smoke test proving AssertJ is on the classpath AND functional in `:domain`.
 *
 * AssertJ's fluent API (`assertThat(...).hasSize(...).contains(...)`) is the
 * project-wide assertion style for `:domain` and `:data`. A collection-based
 * assertion exercises the chained-call path without depending on any Android type.
 */
class AssertJSanityTest {
    @Test
    fun `assertj fluent assertions work on collections`() {
        assertThat(listOf(1, 2, 3))
            .hasSize(3)
            .contains(2)
            .doesNotContain(4)
    }
}
