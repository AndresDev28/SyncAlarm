# Tasks: bootstrap-android-scaffold

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~700 across 4 PRs |
| 400-line budget risk | Low (per PR ≤250) |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 → PR 2 → PR 3 → PR 4 (stacked-to-main) |
| Delivery strategy | ask-on-risk |
| Chain strategy | stacked-to-main |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: Low

### Suggested Work Units

| Unit | PR | Goal | Test cmd | Rollback |
|------|----|------|----------|----------|
| 1 | 1 | Wrapper + version catalog | `./gradlew --version` → 0 | revert 8 wrapper/config files |
| 2 | 2 | `:domain` JVM + test runner | `./gradlew :domain:test` → 0 | revert `:domain/` + config.yaml |
| 3 | 3 | `:app`+`:data` Hilt+Compose | `./gradlew :app:assembleDebug` → 0 | revert `:app/`,`:data/`,`AGENTS.md` |
| 4 | 4 | commitlint + Husky | `commitlint` → 0 | revert 4 tooling files |

## Phase 1: PR 1 — scaffold-build

- [x] 1.1 Write `gradle/wrapper/gradle-wrapper.properties` pinned to Gradle 8.10.2
- [x] 1.2 Add `gradle-wrapper.jar` (binary, budget-excluded) + `gradlew` launcher
- [x] 1.3 Write `gradle.properties` (jvmargs, useAndroidX, kotlin.code.style)
- [x] 1.4 Write `gradle/libs.versions.toml` — pinned triplet (full versions in design.md §Version Triplet)
- [x] 1.5 Write `settings.gradle.kts` (`pluginManagement` + `dependencyResolutionManagement`; no module includes yet)
- [x] 1.6 Write root `build.gradle.kts` (`plugins { id("...") apply false }`)
- [x] 1.7 Write `.gitignore` (`.gradle/`, `build/`, `local.properties`, `*.iml`, `.idea/`, `.kotlin/`, `captures/`)
- [x] 1.8 Write `local.properties.example` placeholder (no real SDK path)

## Phase 2: PR 2 — scaffold-domain

- [x] 2.1 Write `domain/build.gradle.kts` (`kotlin("jvm")`; NO Android plugin; stdlib)
- [x] 2.2 Wire test deps via catalog `testImplementation` (JUnit 5 BOM, MockK, Turbine, AssertJ)
- [x] 2.3 Add `:domain` to `settings.gradle.kts` `include(...)`
- [x] 2.4 Write `domain/src/test/kotlin/com/syncalarm/domain/JupiterSanityTest.kt` (one `@Test`)
- [x] 2.5 Write `domain/src/test/kotlin/com/syncalarm/domain/MockKSanityTest.kt` (`mockk` + `every` smoke)
- [x] 2.6 Write `domain/src/test/kotlin/com/syncalarm/domain/TurbineSanityTest.kt` (`flow.test { }` over `flowOf(1)`)
- [x] 2.7 Write `domain/src/test/kotlin/com/syncalarm/domain/AssertJSanityTest.kt` (`assertThat(1).isEqualTo(1)`)
- [x] 2.8 Modify `openspec/config.yaml`: flip `testing.strict_tdd: true`; wire `apply.tdd_command`/`apply.test_command`/`verify.test_command` to `./gradlew test`; set `verify.build_command: ./gradlew :app:assembleDebug`

## Phase 3: PR 3 — scaffold-app

- [x] 3.1 Write `data/build.gradle.kts` (com.android.library + KSP + `project(":domain")` + namespace `com.syncalarm.data`)
- [x] 3.2 Add `:data` to `settings.gradle.kts` module includes
- [x] 3.3 Write `app/build.gradle.kts` (application + Compose Compiler plugin + KSP + Hilt + projects + Compose BOM + namespace `com.syncalarm.app`)
- [x] 3.4 Write `app/src/main/AndroidManifest.xml` (5 `<uses-permission>` + `android:name=".SyncAlarmApp"` + MainActivity LAUNCHER intent-filter) — **with `AndroidManifestTest`** (3 JVM unit tests asserting the contract)
- [x] 3.5 Write `app/src/main/kotlin/com/syncalarm/app/SyncAlarmApp.kt` (`@HiltAndroidApp class SyncAlarmApp : Application()`) — **with `SyncAlarmAppStructureTest`** (1 JVM unit test)
- [x] 3.6 Write `app/src/main/kotlin/com/syncalarm/app/MainActivity.kt` (`@AndroidEntryPoint` + `setContent { MaterialTheme { Text("SyncAlarm") } }`) — **with `MainActivityStructureTest`** (2 JVM unit tests)
- [x] 3.7 Write `app/src/androidTest/kotlin/com/syncalarm/app/ComposeSmokeTest.kt` (uses `createAndroidComposeRule`)
- [x] 3.8 Write `app/src/androidTest/kotlin/com/syncalarm/app/HiltSmokeTest.kt` (`@HiltAndroidTest` + `HiltAndroidRule`)
- [x] 3.9 Write `AGENTS.md` at repo root (project-level AI guidance referencing PRD §6 Clean Architecture)
- [x] 3.10 Modify `README.md` — add `first-build-30s` cold-cache note + minimal quick-start stub (PR 4 extends) — **with `OpenSpecConfigWiringTest`** (2 JVM unit tests verifying the wiring)

## Phase 4: PR 4 — scaffold-tooling

- [x] 4.1 Write `commitlint.config.js` (extends `@commitlint/config-conventional`)
- [x] 4.2 Write `package.json` (devDeps: `@commitlint/cli@^19`, `@commitlint/config-conventional@^19`, `husky@^9`)
- [x] 4.3 Write `.husky/commit-msg` (`npx --no-install commitlint --edit "$1"`)
- [x] 4.4 Write `.editorconfig` (`[*.{kt,kts,java}]`: 4-space, LF, UTF-8, trim trailing, final newline)
- [x] 4.5 Extend `README.md` — quick-start build/test commands + commitlint note + exact-alarm forward-compat deferral
- [x] 4.6 Note in `README.md`: CI / GitHub Actions deferred to follow-up change `bootstrap-android-ci`

## Cross-PR Dependencies (stacked-to-main)

```
PR 1 → main → PR 2 → main → PR 3 → main → PR 4
```

Each PR targets `main`; rebases trivial (disjoint file sets).

## sdd-verify Test Commands per PR

| PR | Verify command |
|----|----------------|
| PR 1 | `./gradlew --version` (exit 0, Gradle 8.10.2) + `./gradlew help` (exit 0) |
| PR 2 | `./gradlew :domain:test` (exit 0, 4 sanity tests pass) + `./gradlew :domain:dependencies` (zero Android deps) |
| PR 3 | `./gradlew :app:assembleDebug` (exit 0, cold cache ≤30s) + `./gradlew :app:compileDebugAndroidTestKotlin` (exit 0; tests compile) |
| PR 4 | `npx commitlint --from=HEAD~1 --to=HEAD` (exit 0 on prior conventional commit) |

## New Risks (additions to design risk register)

1. **androidTest runtime not CI-friendly** — Compose/Hilt instrumented tests in `app/src/androidTest/` need an emulator. PR 3 verify command is test COMPILATION only (`compileDebugAndroidTestKotlin`); runtime gate deferred to first device CI run.
2. **gradle-wrapper.jar binary reviewability** — JAR is committed (Android convention), excluded from review budget. Reviewers verify via re-running `gradle wrapper --gradle-version 8.10.2` and diffing the result.
3. **strict_tdd flip ordering** — PR 2 task 2.8 must land BEFORE PR 3 apply runs. If config flip is reverted, PR 3's apply runs without TDD enforcement. Orchestrator verifies `openspec/config.yaml` diff at PR 3 apply start.