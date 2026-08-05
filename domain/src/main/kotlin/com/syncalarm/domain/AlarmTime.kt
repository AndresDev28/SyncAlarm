package com.syncalarm.domain

/**
 * Placeholder domain value class for an alarm time-of-day (24-hour clock).
 *
 * This is intentionally tiny — its job in PR 2 is to give `:domain` real production code
 * so the module isn't tests-only at the end of scaffold-domain. Real validation rules
 * (e.g. "the alarm must be at least N minutes in the future"), formatting
 * (`"HH:mm"` parsing), and rule-engine integration land in a follow-up change.
 *
 * Validation lives in the value class itself so any consumer (`:data` repo, `:app` UI)
 * gets the guard for free. Keeping the type in `:domain` (JVM-pure) means no Android
 * imports leak in and the class stays unit-testable under strict TDD.
 */
data class AlarmTime(val hour: Int, val minute: Int) {
    init {
        require(hour in 0..23) { "hour must be 0..23, got $hour" }
        require(minute in 0..59) { "minute must be 0..59, got $minute" }
    }

    /** Total minutes since midnight, useful for arithmetic and comparison. */
    val minutesSinceMidnight: Int
        get() = hour * 60 + minute
}
