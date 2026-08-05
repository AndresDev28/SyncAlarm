# module-boundaries Specification

## Purpose

Gradle multi-module layout that enforces the Clean Architecture dependency direction (`app → domain ← data`, `app → data`) via Gradle's module type system, not by convention. `:domain` carries zero Android imports so it stays TDD-friendly per PRD §6. Boundary enforcement is structural, not aspirational.

## Requirements

### Requirement: Three-Module Layout

The build SHALL include exactly three modules declared in `settings.gradle.kts`: `:app`, `:domain`, `:data`.

#### Scenario: Settings file lists all three modules

- GIVEN a clean clone
- WHEN `settings.gradle.kts` is read
- THEN `include(":app", ":domain", ":data")` SHALL be present
- AND no other modules SHALL be included

### Requirement: `:domain` is Pure Kotlin JVM

`:domain` SHALL apply the `org.jetbrains.kotlin.jvm` Gradle plugin and SHALL NOT apply any Android Gradle plugin.

#### Scenario: `:domain` build script uses Kotlin JVM, not Android Library

- GIVEN `:domain/build.gradle.kts` is read
- WHEN the `plugins {}` block is inspected
- THEN `kotlin("jvm")` SHALL be present
- AND `com.android.library` SHALL NOT be present
- AND no `id("com.android.*")` SHALL be present

### Requirement: `:domain` Has Zero Android Imports

`:domain` source files SHALL NOT contain `import android.*` statements.

#### Scenario: CodeGraph confirms zero Android imports in `:domain`

- GIVEN `:domain/src/` is indexed by CodeGraph
- WHEN `codegraph_explore(projectPath: ".../SyncAlarm", query: "import android")` is scoped to `:domain` sources
- THEN zero references SHALL be returned

#### Scenario: Ripgrep confirms zero Android imports in `:domain`

- GIVEN a clean clone
- WHEN `rg "^import android\." domain/src/` is run
- THEN zero matches SHALL be returned and exit code SHALL be 1 (rg convention for no matches)

### Requirement: `:data` Depends on `:domain` Only

`:data` SHALL depend on `:domain`; `:domain` SHALL NOT depend on `:data`. The dependency direction SHALL be enforced by Gradle's resolution, not by comment.

#### Scenario: `:data` references `:domain`

- GIVEN `:data/build.gradle.kts` is read
- WHEN the `dependencies {}` block is inspected
- THEN `implementation(project(":domain"))` SHALL be present
- AND `project(":app")` SHALL NOT be referenced

#### Scenario: `:domain` does not reference `:data` or `:app`

- GIVEN `:domain/build.gradle.kts` is read
- WHEN the `dependencies {}` block is inspected
- THEN no `project(":data")` and no `project(":app")` reference SHALL be present

### Requirement: `:app` Depends on Both `:domain` and `:data`

`:app` SHALL depend on both lower-layer modules.

#### Scenario: `:app` references both modules

- GIVEN `:app/build.gradle.kts` is read
- WHEN the `dependencies {}` block is inspected
- THEN both `implementation(project(":domain"))` and `implementation(project(":data"))` SHALL be present

### Requirement: No Circular Dependencies

The Gradle dependency graph SHALL be acyclic. Introducing a cycle SHALL cause a resolution failure.

#### Scenario: Clean build resolves with no cycles

- GIVEN a clean clone
- WHEN `./gradlew :app:dependencies --configuration debugRuntimeClasspath` runs
- THEN the build SHALL exit 0
- AND the printed graph SHALL show `:app` → `:data` → `:domain` with no back-edges