// SyncAlarm :domain module — bootstrap-android-scaffold, PR 2 (scaffold-domain).
//
// `:domain` is the Clean Architecture core: pure Kotlin, zero `android.*` imports.
// Per design.md, this module MUST stay JVM-pure so it can be developed under strict TDD
// without an emulator and so the module-boundary test (`rg "^import android\." domain/src/`)
// stays green permanently.
//
// Toolchain: JDK 17 (matches the rest of the project; AGP 8.7+ requires it, see design #1483
// rule 4). `jvmToolchain(17)` lets Gradle auto-detect an installed JDK 17 rather than
// provisioning a new one.

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

// Test dependencies are added in T2.3 (a follow-up commit). Keeping this commit to the
// bare skeleton matches the orchestrator's commit-by-work-unit discipline — each commit
// tells one story, and "module exists" is a different story than "tests can run".
dependencies {
}
