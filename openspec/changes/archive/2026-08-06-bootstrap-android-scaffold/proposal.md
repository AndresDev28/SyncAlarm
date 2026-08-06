# Proposal: bootstrap-android-scaffold

## Intent

PRD §6 commits SyncAlarm to **Clean Architecture + MVVM with a pure-Kotlin `:domain` for TDD**. The repo today has **zero** Gradle, Kotlin, or test infrastructure (`strict_tdd: false`). Every downstream change — Room, alarm scheduling, Google Calendar API — is blocked until a buildable scaffold enforces the module-boundary contract. This change ships that foundation as the smallest PR that unblocks everything else.

## Scope

### In Scope
- **Gradle**: `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/*`, `gradlew(.bat)`
- **Modules**: `:app` (Android app, `com.syncalarm.app`, Hilt `Application`, Compose `MainActivity` stub); `:domain` (pure Kotlin JVM, no `android.*`, one `SanityTest`); `:data` (Android library, `.gitkeep`)
- **DI**: Hilt + KSP in `:app`/`:data`; `:domain` framework-free
- **UI**: Jetpack Compose + Material 3; `MainActivity` renders `Text("SyncAlarm")`
- **Tests**: JUnit 5 + MockK + Turbine + AssertJ in `:domain`
- **Manifest**: 5 `<uses-permission>` entries
- **Tooling**: `.gitignore`, `.editorconfig`, `AGENTS.md`, `commitlint.config.js` + Husky
- **`openspec/config.yaml`**: flip `strict_tdd: true`, wire `./gradlew test` and `./gradlew assembleDebug`

### Out of Scope
- Persistence (Room/DataStore/EncryptedSharedPreferences)
- Real `:data` repos, alarm scheduler, calendar adapters
- Real `:domain` models/usecases/rule engine
- Real UI screens, navigation, theming
- OAuth2 PKCE, Google Calendar API client
- CI/GitHub Actions (→ `bootstrap-android-ci`)
- `buildSrc`/convention plugins (defer until ≥4 Android modules)
- detekt/ktlint

## Capabilities

`openspec/specs/` is empty today. All capabilities are **NEW**.

### New Capabilities
- `app-shell`: Hilt `Application`, Compose `MainActivity`, AndroidManifest (5 permissions), Material 3 theme entry
- `module-boundaries`: `:app`/`:domain`/`:data` Clean Architecture contract — `:domain` zero `android.*` imports; dependency direction `app → domain ← data`, `app → data`
- `build-system`: Kotlin DSL + `libs.versions.toml`, JDK 17 toolchain, AGP 8.7+, Kotlin 2.0.x, Gradle 8.10+, KSP for annotation processing
- `testing-infrastructure`: JUnit 5 + MockK + Turbine + AssertJ; `SanityTest` makes `./gradlew :domain:test` exit 0; flips `strict_tdd: true`
- `tooling-conventions`: `.gitignore`, `.editorconfig`, `AGENTS.md`, commitlint + Husky (Conventional Commits)

### Modified Capabilities
None.

## Approach

Adopt orchestrator-locked decisions: Kotlin DSL, Compose + Material 3, Hilt + KSP, JUnit 5 + MockK + Turbine + AssertJ, **minSdk 26 / compileSdk 35 / targetSdk 35**, JDK 17, package `com.syncalarm.app`, version catalog from day 1, `buildSrc` deferred. Kotlin 2.0+ mandates KSP — KAPT is not viable. Single PR; foundation every future change stacks on.

## Tradeoffs

- **minSdk 26 vs 24**: 26 unlocks `EncryptedSharedPreferences`, modern `WorkManager`/`POST_NOTIFICATIONS` semantics. Cost: ~5% reach lost. Worth it for the API surface.
- **Hilt vs Koin**: Hilt's compile-time graph + native `HiltWorkerFactory` (WorkManager) outweigh Koin's lighter runtime. Calendar-sync workers will need this.
- **KSP vs KAPT for Hilt**: Kotlin 2.0+ effectively requires KSP (KAPT slow, being deprecated). KSP.
- **Strict TDD flip now vs later**: Flip in this PR — proves the runner works end-to-end. Deferring risks forgetting.

## Forward-Compat Constraint

`minSdk 26` does **NOT** unlock `SCHEDULE_EXACT_ALARM` (API 31) or `USE_EXACT_ALARM` (API 33). The `:app` module will need `Build.VERSION.SDK_INT >= 31` conditional handling and a runtime permission flow. This change only **declares** the permissions; the flow is a later change.

## Affected Areas

| Area | Impact | Description |
|---|---|---|
| `settings.gradle.kts` | New | Root settings; module includes |
| `build.gradle.kts` (root) | New | Plugin DSL (`apply false`) |
| `gradle.properties` | New | `useAndroidX`, JVM args, Kotlin style |
| `gradle/libs.versions.toml` | New | Version catalog (single source of truth) |
| `gradle/wrapper/*` | New | Pinned wrapper (binary jar excluded from review budget) |
| `.gitignore`, `.editorconfig`, `AGENTS.md` | New | Tooling / multi-agent conventions |
| `commitlint.config.js`, `.husky/commit-msg` | New | Conventional Commits enforcement |
| `app/build.gradle.kts` + `AndroidManifest.xml` | New | App module + 5 permissions |
| `app/src/main/kotlin/.../{SyncAlarmApp,MainActivity}.kt` | New | Hilt app + Compose entry |
| `app/src/main/res/values/*` | New | Material 3 theme, strings, colors |
| `domain/build.gradle.kts` | New | Pure Kotlin JVM (`java-library`) |
| `domain/src/test/.../SanityTest.kt` | New | Trivial `assertEquals` |
| `data/build.gradle.kts` + empty manifest | New | Stub library |
| `openspec/config.yaml` | Modified | `strict_tdd: true`; wired commands |
| `README.md` | Modified | "build / test" section |

## Impact (Downstream Unblocks)

| Future change | Unlocked by this PR |
|---|---|
| `add-room-persistence` | Gradle, KSP, test runner, version catalog |
| `add-datastore-prefs` | Gradle, Hilt modules, `:domain` repo interfaces |
| `add-oauth-google-calendar` | `:app` module + manifest pattern, Hilt `Application`, Compose entry |
| `add-alarm-scheduler` | `:data` Android library stub, permission declarations |
| `add-workmanager-calendar-scan` | Hilt (`HiltWorkerFactory`), permissions, manifest pattern |
| `add-rule-engine` | `:domain` pure-Kotlin module ready for TDD |

## Risks

| Risk | Like | Mitigation |
|---|---|---|
| Hilt / Kotlin 2.0 / KSP version skew | Med | Pin in catalog; Hilt 2.52+; verify `:app:assembleDebug` |
| Compose Compiler drift | Med | Use Kotlin 2.0 Compose Compiler Gradle plugin (no manual alignment) |
| Strict TDD flip blocks on module lacking tests | Med | Only `:domain` runs tests this PR; `:app` smoke test added in `:domain` SanityTest scope |
| Gradle wrapper download fails on first build | Low | Pin wrapper hash; smoke check in `sdd-verify` |
| `:domain` accidentally gains `android.*` import | Low | `:domain` has no Android plugin; CI-level rule deferred |
| `minSdk 26` ≠ exact-alarm permission unlock | Med | Document conditional `SDK_INT >= 31` for later change |

## Rollback Plan

Single-PR revert. No data, no remote state, no schema. Repo returns to `f643186 Initial commit` + `openspec/` + `exploration.md`. No follow-up needed.

## Dependencies

None external. Internal: `sdd-init` (observation #1474, complete).

## Success Criteria

- [ ] `./gradlew assembleDebug` succeeds on a fresh clone (JDK 17 only)
- [ ] `./gradlew test` exits 0; `SanityTest` passes
- [ ] `:domain` has zero `android.*` imports (CodeGraph-verified)
- [ ] Hilt `Application` wired; Compose UI renders `Text("SyncAlarm")`
- [ ] AndroidManifest declares all 5 permissions
- [ ] `openspec/config.yaml` shows `testing.strict_tdd: true` + wired runner/build commands
- [ ] `commitlint` rejects a non-conventional commit message locally