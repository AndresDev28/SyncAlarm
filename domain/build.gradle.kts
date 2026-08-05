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
    // --- JUnit 5 stack ---
    // The BOM aligns all `org.junit.*` artifact versions to a single release; jupiter brings
    // the API + params + engine together, and `testRuntimeOnly` on the engine keeps it off
    // the production classpath. `junit-vintage-engine` is intentionally omitted: `:domain`
    // has no legacy JUnit 4 tests and the catalog has no vintage entry; PR 3 may add it
    // if a legacy test suite shows up.
    testImplementation(platform(libs.junit5.bom))
    testImplementation(libs.junit5.jupiter)
    testRuntimeOnly(libs.junit5.jupiter.engine)

    // --- Test utilities — all JVM-safe, none pull in android.* ---
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.assertj.core)

    // Turbine needs a coroutines runtime + a coroutines test runner; both are JVM-only.
    testImplementation(libs.kotlinx.coroutines.test)
}

// Tell Gradle's `test` task to use the JUnit Platform; without this, Gradle defaults to
// JUnit 4 discovery and would silently report "0 tests" even though the classes compile.
// `useJUnitPlatform()` reads its engine from `testRuntimeOnly(libs.junit5.jupiter.engine)` above.
tasks.test {
    useJUnitPlatform()
}
