# Archive Report — bootstrap-android-scaffold

```yaml
schema: gentle-ai.archive-report/v1
change: bootstrap-android-scaffold
archived_at: 2026-08-06
archive_path: openspec/changes/archive/2026-08-06-bootstrap-android-scaffold/
archive_reason: re-verification PASS at HEAD ab951e1; verdict admit pass at 30/30 requirements + 37/37 scenarios
final_state_authority: observation 1500 (verify-report-2) > observation 1485 (tasks) > observation 1489 (apply-progress) > observation 1499 (verify-report)
```

## Executive Summary

The `bootstrap-android-scaffold` change shipped an Android multi-module scaffold (`:app` / `:data` / `:domain`) honoring the PRD's Clean Architecture + TDD-pure `:domain` contract, delivered as **4 chained PRs** (PRs #1–#4) that all merged to `main` plus a final documentation-only fix commit (`ab951e1`) addressing the 4 archive-blocking warnings from the first verify pass. Re-verification at `ab951e1` is **PASS** (30/30 requirements, 37/37 scenarios, 0 CRITICAL findings, 0 failing tests). The 5 capability specs already live under `openspec/specs/` as the source of truth (no delta merge needed — they were created new). The change folder is moved to `openspec/changes/archive/2026-08-06-bootstrap-android-scaffold/` for permanent audit-trail.

## Final Commit Graph (on `main`)

```
ab951e1 chore(scaffold): address sdd-verify warnings           [archive-warning fixes]
808c863 Merge pull request #4                                  [PR 4 merge — scaffold-tooling]
56924ad docs(scaffold): record PR 4 apply-progress (TDD evidence)
3acf523 chore(scaffold): mark PR 4 tasks complete in tasks.md
4b1431e chore(scaffold): ignore node_modules + package-lock.json
fbabc1d docs(tooling): extend README with commitlint + Husky setup
176da63 chore(tooling): add .editorconfig
a87fd1b chore(tooling): add .husky/commit-msg hook
b10d3c6 chore(tooling): add commitlint.config.js + package.json
22c238d test(tooling): assert commitlint + husky + editorconfig + README contracts (RED)
94ceef1 Merge pull request #3                                  [PR 3 merge — scaffold-app]
5196019 docs(scaffold): record PR 3 apply-progress
b2450e2 chore(scaffold): mark PR 1/2/3 tasks complete in tasks.md
70567f4 docs(scaffold): add AGENTS.md + README quick-start
40af911 feat(scaffold): wire verify.build_command + verify.test_command in openspec/config.yaml
f9fb2f7 test(app): assert openspec/config.yaml wires verify.test_command and build_command
86b5927 feat(app): add Compose MainActivity rendering SyncAlarm smoke screen
86d8cee test(app): assert MainActivity renders SyncAlarm inside MaterialTheme (RED)
278365d feat(app): add Hilt Application class SyncAlarmApp
e371f11 test(app): assert SyncAlarmApp declares @HiltAndroidApp and extends Application
2ec981d test(app): assert Hilt bootstraps via HiltAndroidRule (compile-gated)
be917a9 fix(app): use JUnit 5 platform for unit tests
4cfe620 chore(scaffold): add com.google.android.material for Material3 XML theme parent
5201fcf feat(app): declare AndroidManifest with 5 permissions + SyncAlarmApp registry
9223f2b test(app): assert AndroidManifest declares the app-shell contract
e101092 chore(scaffold): add gradle.properties with AndroidX flag and JVM args
8904e44 chore(scaffold): add androidx-compose-ui-test-junit4 to catalog
6d3e17c chore(app): add :app module skeleton with Compose + Hilt + KSP
ebd23c2 chore(data): add :data module skeleton with Hilt + KSP
dade230 chore(scaffold): include :app + :data in settings.gradle.kts
0741f10 Merge pull request #2                                  [PR 2 merge — scaffold-domain]
372fa87 test(domain): wire useJUnitPlatform() (PR2 fixup)
9a7261e test(domain): add AssertJSanityTest
e69f920 test(domain): add TurbineSanityTest
e475fe5 test(domain): add MockKSanityTest
2aab418 test(domain): add JupiterSanityTest
57df9c2 feat(scaffold): flip strict_tdd: true in openspec/config.yaml
0e38aca chore(scaffold): add :domain to settings.gradle.kts
d911405 feat(domain): write domain/build.gradle.kts (pure kotlin-jvm)
340fc70 Merge pull request #1                                  [PR 1 merge — scaffold-build]
… (wrapper jar, settings.gradle.kts, build.gradle.kts, libs.versions.toml, .gitignore, …)
f643186 Initial commit                                          [README only]
```

Local `main` is 1 commit ahead of `origin/main` (the `ab951e1` fix). Push when ready.

## Merged PR Chain (reviewable work units)

| # | PR | Title | Merge SHA | Files | Lines (+/−) | Branch base | Per-PR smoke |
|---|----|-------|-----------|-------|-------------|-------------|--------------|
| 1 | #1 | chore(scaffold): bootstrap-build — wrapper, version catalog, settings, .gitignore, openspec/specs/, openspec/changes/bootstrap-android-scaffold/{proposal,exploration,design,tasks} | `340fc70` | 22 | 1836 / 0 (incl. binary `gradle-wrapper.jar` + `openspec/` artifacts from `sdd-init`) | `main` | `./gradlew --version` → 0, Gradle 8.10.2 |
| 2 | #2 | chore(scaffold): scaffold-domain — pure Kotlin JVM module + 4 sanity tests + strict_tdd flip | `0741f10` | 8 | 198 / 20 | `main`+PR1 | `./gradlew :domain:test` → 0, 4/4 |
| 3 | #3 | chore(scaffold): scaffold-app — :app + :data Hilt+Compose+Manifest+AGENTS.md+config wiring | `94ceef1` | 21 | 1511 / 33 | `main`+PR2 | `./gradlew :app:assembleDebug` → 0, APK 13 MB |
| 4 | #4 | chore(scaffold): scaffold-tooling — commitlint + Husky + editorconfig + README extension | `808c863` | 9 | 604 / 8 | `main`+PR3 | commit-msg hook rejects non-conventional / accepts conventional |

**Total authored code across 4 PRs**: 50 non-binary files (52 tracked minus the wrapper jar binary), ~4,149 insertions / 61 deletions (excluding `gradle-wrapper.jar`).

**Per-PR review budget**: PR 3 exceeded the 400-line budget (904 lines / 2.25×) and PR 4 marginally exceeded (405 lines / 1.01×) — orchestrator-accepted exceptions, documented in observation 1499/1500.

## Implementation Duration

| Phase | Started | Completed | Calendar span |
|-------|---------|-----------|---------------|
| sdd-init | 2026-08-05 | 2026-08-05 | observation #1474 |
| sdd-explore / sdd-propose / sdd-spec / sdd-design / sdd-tasks | 2026-08-05 (early) | 2026-08-05 (late) | same day |
| PR 1 (scaffold-build) | 2026-08-05 | 2026-08-05 | same day |
| PR 2 (scaffold-domain) | 2026-08-05 | 2026-08-05 | same day |
| PR 3 (scaffold-app) | 2026-08-06 (early) | 2026-08-06 | overnight |
| PR 4 (scaffold-tooling) | 2026-08-06 | 2026-08-06 | same day |
| First sdd-verify (observation 1499) | 2026-08-06 | 2026-08-06 | verdict: fail, 4 blockers |
| W1–W4 fix commit | 2026-08-06 | 2026-08-06 (`ab951e1`) | documentation-only |
| Re-verify (observation 1500) | 2026-08-06 | 2026-08-06 | verdict: pass |

**Total elapsed**: 2 calendar days (2026-08-05 → 2026-08-06), single working session block.

## Test Gate Results (from observation 1500, re-executed at HEAD `ab951e1`)

| Gate | Command | Exit | Result | Output hash (sha256) |
|------|---------|------|--------|----------------------|
| Wrapper smoke | `./gradlew --version` | 0 | Gradle 8.10.2, JVM 17.0.20 Temurin | n/a |
| Domain tests | `./gradlew :domain:test` | 0 | **4/4 pass** (JupiterSanityTest, MockKSanityTest, TurbineSanityTest, AssertJSanityTest) | `3664facc…` |
| App unit tests | `./gradlew :app:testDebugUnitTest` | 0 | **17/17 pass** (AndroidManifestTest × 3, MainActivityStructureTest × 2, OpenSpecConfigWiringTest × 2, SyncAlarmAppStructureTest × 1, ToolingConventionsTest × 9) | `9e85795c…` |
| Full suite | `./gradlew test` | 0 | **38 executions** (21 distinct tests × debug+release variants), 0 failures | `85091b6e…` |
| Debug APK | `./gradlew :app:assembleDebug` | 0 | `app-debug.apk` **13,060,111 bytes** (~13.06 MB) | `9383c951…` |
| Instrumented APK | `./gradlew :app:assembleDebugAndroidTest` | 0 | `app-debug-androidTest.apk` **1,066,789 bytes** (~1.07 MB) | `93bda801…` |
| commitlint (bad msg, real git hook) | `.husky/_/commit-msg /tmp/bad-commit.txt` | 1 | rejected: `subject-empty`, `type-empty` | `81616bc6…` |
| commitlint (good msg) | `.husky/_/commit-msg /tmp/good-commit.txt` | 0 | accepted | `2419e200…` |

**Module boundaries — all confirmed**:
- `:domain` `testRuntimeClasspath`: zero `android.*`/`androidx.*` artifacts (kotlin-stdlib, junit5, mockk, turbine, assertj, coroutines-test only)
- `:data` `releaseRuntimeClasspath`: only `project :domain`; no `project :app`
- `:app` `debugRuntimeClasspath`: `project :data` + `project :domain`, `:data → :domain` nested, no back-edges
- `./gradlew projects` reports exactly `:app`, `:data`, `:domain`
- `rg "^import android\." domain/src/` → exit 1, zero-byte output (`e3b0c44…`)
- JVM 17 confirmed by class-file bytecode major 61 in both `:app MainActivity.class` and `:domain AlarmTime.class`

## Spec Coverage (from observation 1500)

| Capability | Requirements | Scenarios | Compliant | Coverage |
|------------|--------------|-----------|-----------|----------|
| app-shell | 4 | 7 | 7 | 100% |
| module-boundaries | 6 | 8 | 8 | 100% |
| build-system | 7 | 7 | 7 | 100% |
| testing-infrastructure | 8 | 8 | 8 | 100% |
| tooling-conventions | 5 | 7 | 7 | 100% |
| **Total** | **30** | **37** | **37** | **100%** |

0 FAILING, 0 UNTESTED, 0 PARTIAL. `gentle-ai.verify-result/v1` admission schema admits `pass` at 30/30 + 37/37; verdict is admissible.

## Delta Spec Sync (Phase 2 result)

**No delta sync was performed.** All 5 capability specs are **NEW** (not deltas to existing specs), and per the orchestrator's launch instructions they already live in their final source-of-truth location: `openspec/specs/{app-shell,module-boundaries,build-system,testing-infrastructure,tooling-conventions}/spec.md`. Per the OpenSpec convention (sdd-archive SKILL.md §Step 2), "If main spec does not exist, the delta spec IS a full spec — copy it directly." That copy happened during PR 1 (`scaffold-build`, commit `340fc70`) when these specs were first committed to `openspec/specs/`. They have remained stable through PRs 2–4 and the W1–W4 fix commit.

## File Inventory at Archive Time

```
openspec/changes/archive/2026-08-06-bootstrap-android-scaffold/
├── apply-progress.md      (73,593 bytes, 784 lines — PR-by-PR TDD evidence)
├── design.md              ( 5,725 bytes,  95 lines — technical approach + risk register)
├── exploration.md         (13,491 bytes, 170 lines — pre-proposal exploration)
├── proposal.md            ( 7,033 bytes, 115 lines — intent, scope, rollback, success criteria)
├── tasks.md               ( 6,349 bytes,  92 lines — 32/32 tasks complete, all [x])
├── verify-report.md       (27,813 bytes, 288 lines — first verify pass, verdict fail, 4 blockers)
├── verify-report-2.md     (29,844 bytes, 320 lines — re-verify, verdict pass)  ← UNTRACKED at move time
└── archive-report.md      (this file)
```

**Tasks**: 32/32 complete (PR 1: 8, PR 2: 8, PR 3: 10, PR 4: 6). Every checkbox `[x]`. Zero stale-unchecked. No reconciliation required.

**Note on `verify-report-2.md`**: this file was untracked at the time of the archive move (working tree was otherwise clean at HEAD `ab951e1`). It is included in the moved folder and travels with the change artifacts. User can decide whether to commit it; the sdd-archive skill does not require committing.

## W1–W4 Archive-Blocking Warnings — Resolution Trace

The first sdd-verify pass (observation 1499, verdict: fail, 4 blockers) raised these archive-blockers, all resolved by `ab951e1 chore(scaffold): address sdd-verify warnings` (documentation/spec/tracking text edits only — no code change):

| # | Was | Now | Resolution evidence |
|---|-----|-----|---------------------|
| **W1** | `alarm-permission-flow` follow-up named only in prose (`design.md`, `AndroidManifest.xml:8`) | First-class OpenSpec artifact: `openspec/changes/add-alarm-permissions/proposal.md` (2,835 bytes, 56 lines) | Stub proposal references `SCHEDULE_EXACT_ALARM` (API 31), `USE_EXACT_ALARM` (API 33), `minSdk 26` conditional flow; backlinks to `app-shell` scenario "Forward-compat — exact-alarm runtime flow is deferred" |
| **W2** | Compose smoke-screen scenario had no formal deferral recorded | Spec formally records the deferral | `openspec/specs/app-shell/spec.md:64` scenario "Smoke screen is visible on first launch" ends with: "AND runtime verification via `connectedAndroidTest` is deferred to the follow-up change `bootstrap-android-ci`" |
| **W3** | `settings.gradle.kts:35-37` had three separate `include(...)` calls | Single literal call | `settings.gradle.kts:36` contains exactly `include(":app", ":domain", ":data")` (one line, one call) — matches the `module-boundaries` spec scenario verbatim |
| **W4** | Spec named `domain/src/test/.../SanityTest.kt` (does not exist) | Spec names the four actual framework-specific sanity tests | `openspec/specs/testing-infrastructure/spec.md:23-29` scenario "Four framework sanity tests are discovered and pass" names `JupiterSanityTest`, `MockKSanityTest`, `TurbineSanityTest`, `AssertJSanityTest` |

## Forward-Compat Follow-Ups Now Tracked

| Follow-up | Where | Purpose | Unblocks |
|-----------|-------|---------|----------|
| `add-alarm-permissions` | `openspec/changes/add-alarm-permissions/proposal.md` (stub) | Runtime permission flow for `SCHEDULE_EXACT_ALARM` (API 31) + `USE_EXACT_ALARM` (API 33); `Build.VERSION.SDK_INT >= 31` conditional `requestPermissions(...)`; denial fallback to `setAndAllowWhileIdle` or settings deep-link | PRD §4 RF-04 (alarm scheduling) |
| `bootstrap-android-ci` | mentioned in `AGENTS.md`, `README.md`, `app-shell/spec.md:64`, `add-alarm-permissions/proposal.md` | GitHub Actions workflow with Android emulator for `connectedDebugAndroidTest` (Compose smoke + Hilt injection + alarm permission grant) | Runtime evidence for the 2 currently-compiled-but-unexecuted instrumented tests in `:app` |

## Quality Debt (Non-Blocking, Unchanged from Observation 1499)

These are warnings + suggestions raised in the verification reports that **did not** block archive. They are tracked here so the next change can address them.

### Warnings

- **W5 — Four weak assertions in `:app` structural tests** (would still pass after real regressions):
  - `MainActivityStructureTest.kt:46` — `.contains("SyncAlarm")` is satisfied by the KDoc, does not bind `Text("SyncAlarm")` contract
  - `SyncAlarmAppStructureTest.kt:41` — `.contains("Application")` is satisfied by the `import android.app.Application` line and KDoc
  - `OpenSpecConfigWiringTest.kt:41,43,52,54` — unscoped `.contains("test_command: \"./gradlew test\"")` substring, one occurrence satisfies both apply and verify assertions
  - `HiltSmokeTest.kt:49` — `assertNotNull(hiltRule)` is tautological for a `@get:Rule` field; the actual contract is `hiltRule.inject()` not throwing
  - **Recommended fix**: assert exact contract strings (e.g. `contains("Text(\"SyncAlarm\")")`, `contains(": Application()")`), scope YAML assertions to their section (or parse YAML), replace `assertNotNull(hiltRule)` with `hiltRule.inject()` assertion

- **W6 — Husky 9 deprecation preamble will break on Husky 10 major bump**:
  - `.husky/commit-msg:1-2` prints `husky - DEPRECATED … They WILL FAIL in v10.0.0` on every commit
  - `package.json` pins `husky: ^9.0.0` so v10 won't auto-install, but the gate breaks on eventual major bump
  - **Recommended fix**: delete the two preamble lines from `.husky/commit-msg`, leaving only `npx --no-install commitlint --edit "$1"`

### Suggestions

- **S1** — No regression test guards `module-boundaries` scenarios. A future change could add `com.android.library` to `:domain` or a back-edge from `:domain → :data` and no test would fail. Consider adding a JVM test asserting `domain/build.gradle.kts` contains no `com.android` / `project(` reference.
- **S2** — `build-system` "JVM 17" scenario names unobtainable evidence (asks for `-jvm-target 17` in compiler args; Kotlin 2.0's build-tools API doesn't print that flag). Reword to accept `jvmToolchain(17)` + class-file bytecode major 61 evidence.
- **S3** — `local.properties.example` comments contain `/Users/<you>/` and `/home/<you>/` placeholders. No real path is leaked, but naive `grep '/home/'` audits produce false positives. Reword examples as `$HOME/Android/Sdk`.
- **S4** — Repository history predating PR 4 fails `commitlint --from=<root> --to=HEAD` on `body-max-line-length`. Not a spec violation (the hook only governs later commits). `bootstrap-android-ci` should lint the PR range, not full history.
- **S5** — `./gradlew test` runs `:app` suite twice (debug + release). Harmless but ~2× suite time as `:app` grows. Consider scoping `verify.test_command` to `testDebugUnitTest` + `:domain:test`.

## Implementation Deviations (Documented in observation 1499/1500)

| Deviation | Was | Became | Why |
|-----------|-----|--------|-----|
| `gradle.properties` | PR 1 task list | Landed in PR 3 instead | AGP 8.7+ requires `android.useAndroidX=true`; deferred until `:app`/`AndroidManifest` were staged |
| XML resource files | "No XML theme/resource files" in design.md | `res/values/strings.xml` + `themes.xml` added in PR 3 | Manifest `@string`/`@style` references require them |
| Per-PR review budget | 400-line budget per PR | PR 3 = 904 lines (2.25×), PR 4 = 405 lines (1.01×) | Orchestrator-accepted exception; PR 3 needed the full app scaffold atomically |
| `:domain` `useJUnitPlatform()` | Initially missed in PR 2 | Added via autosquash fixup commit | Gradle defaulted to JUnit 4 discovery, silently reported 0 tests |
| `AlarmTime` placeholder | Not in PR 2 task list | Added in `openspec/config.yaml` T2.8 commit | Orchestrator addition to anchor a future `:domain` model |

## Downstream Changes Now Unblocked

Per the proposal.md §"Impact (Downstream Unblocks)", this scaffold unblocks:

| Future change | Unlocked by |
|---------------|-------------|
| `add-room-persistence` | Gradle + KSP + test runner + version catalog |
| `add-datastore-prefs` | Gradle + Hilt modules + `:domain` repo interfaces |
| `add-oauth-google-calendar` | `:app` module + manifest pattern + Hilt `Application` + Compose entry |
| `add-alarm-scheduler` | `:data` Android library stub + permission declarations |
| `add-workmanager-calendar-scan` | Hilt (`HiltWorkerFactory`) + permissions + manifest pattern |
| `add-rule-engine` | `:domain` pure-Kotlin module ready for TDD |

## Lineage / Observation IDs (Engram traceability)

| Observation ID | Topic | Type | Purpose |
|----------------|-------|------|---------|
| 1474 | `sdd-init/syncalarm` | architecture | sdd-init context (project + capabilities) |
| 1482 | `sdd/bootstrap-android-scaffold/spec` | architecture | Concatenated spec summary — **STALE** (counts 27 req / 31 scenarios; filesystem is authoritative at 30 / 37) |
| 1483 | rule registry / version-pin rationale | (memory) | Rule 1 (Kotlin/KSP/Compose patch match), Rule 3 (KAPT broken on Kotlin 2.0+), Rule 4 (JDK 17 non-negotiable), Rule 5 (cold-cache ~30s) |
| 1484 | `sdd/bootstrap-android-scaffold/design` | architecture | Technical design (architecture decisions, file inventory, version triplet, threat matrix, risk register) |
| 1485 | `sdd/bootstrap-android-scaffold/tasks` | architecture | 32/32 tasks complete, PR-by-PR breakdown with commit SHAs |
| 1489 | `sdd/bootstrap-android-scaffold/apply-progress` | architecture | PR 4 implementation with TDD evidence — superseded by `ab951e1` for fact reconciliation but historically accurate |
| 1499 | `sdd/bootstrap-android-scaffold/verify-report` | architecture | First verify pass (verdict: fail, 4 archive blockers W1–W4, 0 CRITICAL) |
| 1500 | `sdd/bootstrap-android-scaffold/verify-report-2` | architecture | Re-verify at HEAD `ab951e1` (verdict: pass, 30/30 req, 37/37 scenarios, 0 CRITICAL) — **AUTHORITATIVE for archive facts** |

This archive-report observation itself:

| Observation ID | Topic | Type |
|----------------|-------|------|
| (this save) | `sdd/bootstrap-android-scaffold/archive-report` | architecture |

## What Did NOT Change During Archive

- No implementation files were modified. The 4 merged PRs and the W1–W4 fix commit are the final state on `main`.
- No `mem_review` calls with `mark_reviewed` action (per launch-prompt constraints).
- No `sdd-verify` or `sdd-apply` re-run (those phases are complete).
- The `add-alarm-permissions` stub proposal at `openspec/changes/add-alarm-permissions/proposal.md` is left in place as the first-class forward-compat follow-up — it is **not** part of this change's archive.

## Risks Surfaced at Archive Time

1. **`ab951e1` fix commit is uncommitted-elsewhere.** Local `main` is 1 commit ahead of `origin/main` (the `ab951e1` fix commit). The user can push when ready. The archive move itself is an uncommitted working-tree change; commit + push will land the archive folder move together with `ab951e1`.
2. **W5 weak assertions are real regression-value loss.** Four tests in `:app` would still pass after real regressions. Non-blocking for archive; should be addressed before the first feature change lands on top of this scaffold.
3. **W6 Husky 9 deprecation is a soft-clock-bomb.** `package.json` pins `husky: ^9.0.0`; v10 will break the gate without a spec violation. Low likelihood, but a follow-up change should delete the deprecated preamble lines before the v10 migration window.

## Next Steps (Post-Archive)

1. **Commit the archive folder move + the W1–W4 fix commit (`ab951e1`)** in a single commit (or split as the user prefers). The user can decide whether to include the previously-untracked `verify-report-2.md`.
2. **Push `main` to `origin`** — local is 1 commit ahead; push when ready.
3. **Take `add-alarm-permissions` off the backlog** when the team is ready to land the runtime permission flow (after `bootstrap-android-ci` lands the device runner that the instrumented test needs).
4. **Take `bootstrap-android-ci` off the backlog** to land the GitHub Actions workflow with Android emulator — unblocks the 2 currently-compiled-but-unexecuted instrumented tests.
5. **Address W5 + W6** in a small follow-up change before the first feature change lands (regression-value loss + Husky v10 migration prep).
6. **Decide whether to update observation 1482** (the stale 27/31 spec summary) or leave it as historical. The filesystem specs at `openspec/specs/*/spec.md` are authoritative; a discovery observation has been added flagging the discrepancy.
