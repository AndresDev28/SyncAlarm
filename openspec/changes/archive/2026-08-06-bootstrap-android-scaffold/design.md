# Design: bootstrap-android-scaffold

## Technical Approach

Minimal-but-real Android multi-module scaffold honoring PRD §6 (Clean Architecture + MVVM, TDD-pure `:domain`). Three modules (`:app`, `:domain`, `:data`); Kotlin DSL + `libs.versions.toml`; Hilt via KSP; Compose + Material 3 in `:app`; JUnit 5 + MockK + Turbine + AssertJ in `:domain`/`:data`. Delivered as **4 chained PRs** — single PR ≈ 700 LOC blows the 400-line budget.

## Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| DSL + catalog | Kotlin DSL + `libs.versions.toml` | Type-safe; buildSrc deferred |
| DI | Hilt + KSP | Compile-time graph + `HiltWorkerFactory` |
| UI + tests | Compose + Material 3 / JUnit 5 + MockK + Turbine + AssertJ | Declarative MVVM; Turbine = Flow test lib |
| Module types | `:domain` = `kotlin("jvm")`; `:data`/`:app` = `com.android.*` | Structural: zero `android.*` in `:domain` |
| Annotation proc | KSP (not KAPT) | KAPT broken on Kotlin 2.0+ (#1483 rule 3) |
| Compose Compiler | `org.jetbrains.kotlin.plugin.compose` plugin | Tracks Kotlin — no manual alignment (#1483 rule 1 mirror) |
| SDK targets | minSdk 26 / compileSdk 35 / targetSdk 35 | Unlocks NotificationChannels + ~95% reach |
| Delivery + resources | 4 chained PRs; no XML theme files | ~22 files; single PR blows budget |

## Module Dependency Architecture

```
:app (com.android.application, Hilt @HiltAndroidApp, Compose MainActivity)
 ├── implementation(project(":domain"))
 └── implementation(project(":data"))
      └── implementation(project(":domain"))
:domain (org.jetbrains.kotlin.jvm, NO android.*, JUnit5/MockK/Turbine/AssertJ)
```

`:app` and `:data` apply `com.google.devtools.ksp`. `:domain` has no DI plugin.

## File Inventory (22 new + 2 modified; binary `gradle-wrapper.jar` excluded)

- **PR 1 `scaffold-build` (8/~150):** `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `gradlew`, `.gitignore`, `local.properties.example`.
- **PR 2 `scaffold-domain` (5/~200):** `domain/build.gradle.kts` + 4 sanity tests (`JupiterSanityTest`, `MockKSanityTest`, `TurbineSanityTest`, `AssertJSanityTest`).
- **PR 3 `scaffold-app` (8/~250):** `app/build.gradle.kts`, `AndroidManifest.xml`, `SyncAlarmApp.kt`, `MainActivity.kt`, `data/build.gradle.kts`, `AGENTS.md`, `openspec/config.yaml` (modify), `README.md` (modify).
- **PR 4 `scaffold-tooling` (5/~100):** `commitlint.config.js`, `package.json`, `.husky/commit-msg`, `.editorconfig`, README update.

Dropped: `gradlew.bat`, themes/strings XML, data manifest (AGP synthesizes).

## Version Triplet (`gradle/libs.versions.toml`)

- **Build:** Kotlin `2.0.21` · KSP `2.0.21-1.0.28` · Compose Compiler plugin `2.0.21` · AGP `8.7.3` · Gradle `8.10.2` · JDK `17`. KSP + Compose Compiler MUST match Kotlin patch (#1483 rule 1); JDK 17 non-negotiable for AGP 8.7+ (rule 4).
- **AndroidX:** Compose BOM `2024.12.01` · lifecycle `2.8.7` · activity-compose `1.9.3` · Hilt `2.52` (first stable KSP for Kotlin 2.0+, rule 3) · nav-compose `1.2.0`.
- **Tests:** JUnit 5 BOM `5.11.0` · MockK `1.13.13` · Turbine `1.2.0` · AssertJ `3.26.3` · coroutines `1.9.0`.

## Test Architecture

- **`:domain` (JVM):** `JupiterSanityTest`, `MockKSanityTest`, `TurbineSanityTest`, `AssertJSanityTest` (one per framework).
- **`:data` (JVM stub):** same stack declared; no tests required.
- **`:app` (JVM):** `ComposeSanityTest` asserts `Text("SyncAlarm")` via `createComposeRule`.
- **`:app` (instrumented):** `HiltSanityTest` uses `HiltAndroidRule`.

## Build & Verification Commands

| Command | Purpose | Cold cache |
|---|---|---|
| `./gradlew --version` | Wrapper smoke (PR 1) | < 5s |
| `./gradlew :domain:test` | `:domain` tests (PR 2) | ~10s |
| `./gradlew :app:assembleDebug` | APK build (PR 3+) | ~30s incl. plugin download (#1483 rule 5) |
| `./gradlew test` | All unit tests | ~15s warm |

## Rollout — Chained PR Slices

| Slice | Base | Files | LOC | Per-PR smoke |
|---|---|---|---|---|
| PR 1 `scaffold-build` | `main` | 8 | ~150 | `./gradlew --version` → 0 |
| PR 2 `scaffold-domain` | main + PR 1 | 5 | ~200 | `./gradlew :domain:test` → 0 |
| PR 3 `scaffold-app` | main + PR 2 | 8 | ~250 | `./gradlew :app:assembleDebug` → 0 |
| PR 4 `scaffold-tooling` | main + PR 3 | 5 | ~100 | commit-msg hook rejects non-conventional |

`./gradlew :app:assembleDebug` is only valid from PR 3 onward. Each PR's branch produces a green `./gradlew` end state.

**Chain strategy: stacked-to-main.** Each PR targets `main`; merge PR 1 → main, rebase PR 2 onto updated main, merge, etc. Rationale: small blast radius; disjoint file sets = trivial rebases.

## Threat Matrix

`N/A — no routing/shell/subprocess/VCS-PR/process boundaries. Build infrastructure only.`

## Forward-Compat Constraint

`SCHEDULE_EXACT_ALARM` (API 31) / `USE_EXACT_ALARM` (API 33) runtime flows are **not** in this scaffold. This change only **declares** the permissions. The runtime flow lands in a future change named **`alarm-permission-flow`** (findable in `openspec/changes/`).

## Risk Register

| Risk | Mitigation |
|---|---|
| Kotlin/KSP/Compose Compiler skew | Pinned triplet (#1483 rule 1); sdd-apply verifies Maven Central |
| Cold cache build ~30s | sdd-verify forecast (#1483 rule 5); README notes `first-build-30s` |
| Hilt KSP vs KAPT | `id("com.google.devtools.ksp")` per module; spec scenario validates |
| Gradle wrapper in CI | Wrapper jar + `distributionUrl` committed; smoke check |
| BuildSrc deferral | Tolerated until ≥ 4 Android modules; revisit in `bootstrap-convention-plugins` |

## Open Questions

None blocking. Decisions inherited from #1480 and #1483.