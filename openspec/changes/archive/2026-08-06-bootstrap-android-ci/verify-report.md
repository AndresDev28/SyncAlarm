```yaml
schema: gentle-ai.verify-result/v1
evidence_revision: sha256:429eb2141fa90911915174d7f66a3c247a112e3792b95bd355209b1f1da3b18c
verdict: fail
blockers: 3
critical_findings: 1
requirements: 7/9
scenarios: 18/20
test_command: ./gradlew test
test_exit_code: 1
test_output_hash: sha256:1783da34ca308d3f04cf10fe4c9f11f73d095d7e6b99bd66493e0fa1436e9e13
build_command: ./gradlew :app:assembleDebug
build_exit_code: 0
build_output_hash: sha256:4b0a51bcc60c4c868ff83330a77b2e6506e04756816d30a31122b59f3874784f
```

## Verification Report

**Change**: bootstrap-android-ci
**Version**: N/A (2 MODIFIED capability specs: `build-system`, `tooling-conventions`)
**Mode**: Strict TDD (`openspec/config.yaml` `testing.strict_tdd: true`, runner `./gradlew test`)
**Commit verified**: `68c1a42` (merge of PR #5 into `origin/main`; branch `ci/bootstrap-android-ci` at `6be45db`). **PR #5 state: MERGED** (not OPEN as the verify brief assumed).
**Date**: 2026-08-06

### Summary

- Status: **failed** — one CRITICAL regression + two partial-scenario blockers.
- Specs verified: 2 capabilities (`build-system`, `tooling-conventions`).
- Requirements: 9 total (0 ADDED-as-new-capability; 7 ADDED requirements across the two deltas + 1 MODIFIED + 1 forward-compat). Authorization counts: **7/9 satisfied**, 2 partial.
- Scenarios: 20 total — **18/20 COMPLIANT**, 2 PARTIAL.
- Warnings: 6
- Suggestions: 4
- INFO: 3
- **CRITICAL findings: 1 (must be 0 for success)** — the strict-TDD gate is RED.

### Validation Evidence

| Gate | Command | Result | Exit | Output hash (sha256) |
|------|---------|--------|------|---------------------|
| 1 | `./gradlew test --continue` (up-to-date cache) | BUILD SUCCESSFUL — 99 up-to-date tasks (stale green marker) | 0 | (cached; misleading) |
| 1b | `./gradlew test --continue --rerun-tasks` (fresh) | **BUILD FAILED — `:app:testDebugUnitTest` + `:app:testReleaseUnitTest`**, 1 failure / 21 tests | **1** | `1783da34…` |
| 2 | `./gradlew :app:assembleDebug --rerun-tasks` | BUILD SUCCESSFUL in 18s, APK packaged | 0 | `4b0a51bc…` |
| 3 | `./gradlew :data:assembleDebug --rerun-tasks` | BUILD SUCCESSFUL in 1s, AAR packaged | 0 | `ceeef5e6…` |
| 4 | `gh pr view 5` | Title descriptive (`ci: bootstrap Android CI workflows …`), body has Summary/Changes/Test Plan/OpenSpec Artifacts, label `enhancement`, not a draft, **state = MERGED** | 0 | — |

> **Critical hazard — the apply-progress evidence was a stale Gradle up-to-date cache.** `apply-progress.md` reports "`./gradlew test` → BUILD SUCCESSFUL in 8s. 12 tests / 0 failures. Exit 0." That 8s result was the cached green marker from *before* `.github/workflows/` existed; Gradle's up-to-date check for `:app:testDebugUnitTest` does not track the runtime filesystem read `File("../.github/workflows")` inside the test body, so the stale green marker persisted even after the directory the test guards against was created. A forced `--rerun-tasks` (the authoritative fresh execution) fails with 1/21. The strict-TDD gate on `main` is therefore RED, and `ci.yml`'s own `./gradlew test --continue` step will be red on the next clean-runner CI execution.

### Completeness

| Metric | Value |
|--------|-------|
| Tasks total | 11 (Phases 1–5) |
| Tasks complete | 11 |
| Tasks incomplete | 0 |

All checkboxes in `openspec/changes/bootstrap-android-ci/tasks.md` are `[x]`; each maps to one of 6 commits on `ci/bootstrap-android-ci` (`2b681a6`, `6b2792a`, `1f2dfdc`, `2fccd2b`, `1e94088`, `6be45db`). Task completion is at objective level only — **correctness is NOT verified** because the strict-TDD runtime gate is red (see CRITICAL C1).

### Spec Compliance Matrix

**build-system** (5 requirements / 11 scenarios — ADDED delta)

| Requirement | Scenario | Evidence | Result |
|---|---|---|---|
| CI Workflow Exists | Push to main runs CI | `ci.yml:4-5` `on.push.branches: [main]`; job `build-and-test` | ✅ COMPLIANT |
| CI Workflow Exists | Pull request to main runs CI | `ci.yml:6-10` `on.pull_request.branches: [main]` + `paths-ignore: ['docs/**','**.md']` | ✅ COMPLIANT |
| CI Workflow Exists | Build steps follow dependency order | checkout → setup-java 17 → setup-android → sdkmanager → setup-gradle → `test --continue` → `:app:assembleDebug` → `:data:assembleDebug`. All 5 bootstrap steps precede every Gradle command; build commands run in the required order. (Bootstrap sub-order swaps setup-gradle/setup-android relative to design — see INFO I2; the spec's `SHALL precede` clause is satisfied.) | ✅ COMPLIANT |
| CI Workflow Exists | CI status is eligible as a required check | Job key `build-and-test` produces a status; `docs/branch-protection.md:27` lists it. ⚠️ see W6 (check-name vs job-key mismatch). | ✅ COMPLIANT (with W6 caveat) |
| Main Branch Requires Green CI | Protected main rejects direct pushes | Documented in `docs/branch-protection.md:13-16`; enforcement is repo settings (by design). | ✅ COMPLIANT |
| Main Branch Requires Green CI | Protected main permits a green pull request | Documented `docs/branch-protection.md` + proposal D. | ✅ COMPLIANT |
| CodeQL Java and Kotlin Analysis | Manual CodeQL build completes in order | `init` (`security.yml:42-48`) precedes `setup-gradle:50`, `:app:assembleDebug:53`, and `analyze:58` ✅. **BUT** `analyze:58-63` sets `category: "/language:java-kotlin"`** — contradicts "analysis SHALL upload SARIF without a custom `category`". | ⚠️ PARTIAL (C/W3) |
| CodeQL Java and Kotlin Analysis | CodeQL severity policy blocks merge | `analyze` uploads SARIF by default; severity policy is a repo code-scanning setting (documented). | ✅ COMPLIANT |
| Weekly Trivy Filesystem Scan | Weekly schedule starts Trivy | `security.yml:8-9` cron `0 6 * * 1`; `trivy` job runs. | ✅ COMPLIANT |
| Weekly Trivy Filesystem Scan | Initial scan is warning-only | `exit-code: "0"` ✅, `ignore-unfixed: true` ✅ (not fail CI). **BUT** no SARIF upload step — only `format: table` (`security.yml:86`); violates "findings SHALL be uploaded as SARIF". | ⚠️ PARTIAL (C/W4) |
| Emulator Runner Forward Compatibility | Emulator job is added later | `ci.yml:62-69` placeholder comment names `android-emulator-runner@v2` + `add-alarm-permissions`. | ✅ COMPLIANT |

**tooling-conventions** (4 requirements / 9 scenarios — ADDED + MODIFIED delta)

| Requirement | Scenario | Evidence | Result |
|---|---|---|---|
| Branch Naming Convention | Developer creates a feature branch | `docs/branch-protection.md:36-49` lists `feat/<descriptive-name>` + 8 more prefixes. | ✅ COMPLIANT |
| Branch Naming Convention | Protection validates branch naming | Documented as SHOULD; GitHub does not enforce natively; `branch-name-lint` follow-up noted (`docs/branch-protection.md:51-52`). SHOULD satisfied by documented guidance + planned enforcement. | ✅ COMPLIANT (S1) |
| Branch Naming Convention | Branch and commit taxonomy align | 9 prefixes mirror Conventional Commits types (commitlint-aligned). | ✅ COMPLIANT |
| Git Worktrees for Concurrent Work and Hotfixes | Context-switch without losing feature work | `docs/branch-protection.md:74-87` `git worktree add ../syncalarm-hotfix main`; original worktree preserved. | ✅ COMPLIANT |
| Git Worktrees for Concurrent Work and Hotfixes | Hotfix and feature proceed in parallel | `docs/branch-protection.md:94-97`. | ✅ COMPLIANT |
| Git Worktrees for Concurrent Work and Hotfixes | Parallel reviews remain independent | `docs/branch-protection.md:89-92`. | ✅ COMPLIANT |
| Main Branch Requires Pull Requests and Green CI | Direct push is blocked | `docs/branch-protection.md:13-16`; repo-settings enforcement by design. | ✅ COMPLIANT |
| Main Branch Requires Pull Requests and Green CI | Failed CI blocks merge | `docs/branch-protection.md:19-29` required checks table. ⚠️ W6 (check-name mismatch). | ✅ COMPLIANT (with W6 caveat) |
| CI / GitHub Actions Deferred (MODIFIED) | Workflows directory is created | `.github/workflows/ci.yml` + `.github/workflows/security.yml` both exist. | ✅ COMPLIANT |

**Compliance summary**: 18/20 scenarios COMPLIANT, 2 PARTIAL, 0 FAILING-by-static-evidence, 0 UNTESTED.

> The 2 partial scenarios are static-evidence gaps (a CodeQL `category` line and a missing Trivy SARIF upload). They are **not** the cause of the `fail` verdict — the CRITICAL test-gate regression (C1) is.

### Correctness (Static Evidence)

| Requirement | Status | Notes |
|---|---|---|
| `.github/workflows/ci.yml` exists, runs test + app + data assemblies in order | ✅ Implemented | `ci.yml:54,57,60` — order correct. |
| SDK 35 provisioned before every Gradle step | ✅ Implemented | `setup-android@v4` + `sdkmanager "platforms;android-35" "build-tools;35.0.0"` (`ci.yml:36-47`) before `test`/`assemble`. R1 mitigated. |
| CodeQL `java-kotlin`, manual build, no init `category` | ⚠️ Partial | `languages: java-kotlin` ✅, no `category` on `init` ✅ (R3 mitigated on init). But `build-mode: manual` is **missing** (W2) and `analyze` carries a custom `category` (W3). |
| Trivy warning-only, `@0.28.0`, `ignore-unfixed` | ⚠️ Partial | `@0.28.0` ✅ (R2 mitigated), `exit-code: "0"` ✅, `ignore-unfixed: true` ✅. But no SARIF upload (W4) and no PR-skip guard (W5). |
| Dependabot `github-actions` weekly, pins fresh | ✅ Implemented | `dependabot.yml:1-22`; gradle deferral documented `:24-26`. |
| `docs/branch-protection.md` required checks + branch names + worktrees | ⚠️ Partial | All sections present, but required-check names in the table do not match the actual GitHub check (display) names produced by the workflows (W6). |
| `AGENTS.md` CI out-of-scope bullet removed | ✅ Implemented | Diff shows 2 lines removed (bullet + blank); `grep "CI / GitHub Actions" AGENTS.md` returns zero matches. |
| Strict-TDD gate (`./gradlew test`) stays green | ❌ **BROKEN** | See CRITICAL C1. The pre-existing `ToolingConventionsTest$NoWorkflowsDirectory` guard now fails because this change creates `.github/workflows/`. |

### Coherence (Design)

| Decision | Followed? | Notes |
|---|---|---|
| D1 Trivy warning-only (`exit-code: "0"` + `ignore-unfixed`) | ✅ Yes | `security.yml:84-85`. |
| D2 Four artifacts in scope | ✅ Yes | `ci.yml`, `security.yml`, `dependabot.yml`, `docs/branch-protection.md` all present. |
| D3 ONE OpenSpec change | ✅ Yes | Single change folder, single PR. |
| D4 Emulator placeholder in `ci.yml` | ✅ Yes | `ci.yml:62-69`. |
| D5 Pin 3rd-party actions (no `@master`) | ✅ Yes | `trivy-action@0.28.0`; 1st-party on `@v4`. (No SHA-pinning, but tag-pinned ≥ release tag as design allowed.) |
| D6 `setup-android@v4` + `sdkmanager` before Gradle | ✅ Yes | Both workflows. |
| D7 CodeQL `manual`, no `category:`, job-level perms, drop `actions: read` | ⚠️ Partial | No init `category` ✅, job-level perms ✅, no `actions: read` ✅. BUT `build-mode: manual` missing (W2) and `analyze` has a custom `category` (W3). |
| D8 Cache via `setup-gradle@v4` only | ✅ Yes | No separate `actions/cache`. |
| D9 `:data:assembleDebug` added | ✅ Yes | `ci.yml:59-60`. |
| D10 Branch naming = Conventional Commit prefixes | ✅ Yes | 9 prefixes in `docs/branch-protection.md`. |
| D11 Worktrees `../syncalarm-<branch>` | ✅ Yes | `docs/branch-protection.md:64-70`. |
| R5 `timeout-minutes: 25` | ❌ Deviation | `ci.yml:24` = 20 (apply-progress Deviation #1). |
| R9 top-level `permissions: {}` + job-level minimums | ⚠️ Partial | `security.yml` ✅. `ci.yml:17-18` uses top-level `contents: read` with **no job-level** permissions — defense-in-depth not followed for `ci.yml` (W7). |
| R12 Trivy skips PR events | ❌ Deviation | No `if: github.event_name != 'pull_request'` on the `trivy` job; Trivy runs on PRs (W5). |
| R19 `--continue` on test | ✅ Yes | `ci.yml:54`. |

### Deviation Assessment

| Deviation | Severity | Notes |
|---|---|---|
| `timeout-minutes: 20` vs design's 25 (R5) | WARNING | Cold first-run risk; apply-progress #1. Bump to 25. |
| `build-mode: manual` missing on `codeql-action/init` | WARNING | Default is `autobuild` for `java-kotlin`; the explicit `:app:assembleDebug` step is present but CodeQL may attempt its own autobuild and stumble on Hilt KSP (R7). One-line fix. |
| Trivy PR-skip guard absent (R12) | WARNING | Trivy runs on every PR (`exit-code: "0"`, so non-blocking) but wastes ~90s/PR. Add `if: github.event_name != 'pull_request'` to the `trivy` job. |
| **Trivy SARIF upload step missing** | WARNING (blocker) | Spec scenario "Initial scan is warning-only" requires "findings SHALL be uploaded as SARIF". `trivy` job uses `format: table` only — no `upload-sarif` step. Add `format: sarif` output + `github/codeql-action/upload-sarif@v3`. |
| **`analyze` step carries a custom `category`** | WARNING (blocker) | Spec scenario "Manual CodeQL build completes in order" requires "analysis SHALL upload SARIF without a custom `category`". `security.yml:61` sets `category: "/language:java-kotlin"`. Remove the line (or amend the spec). The init-level R3 risk is still mitigated — this `category` is on `analyze`, a valid free-form label, but it contradicts the literal spec text. |
| `ci.yml` top-level `permissions: contents: read` instead of `{}` + job-level | WARNING | Design R9 wanted empty top-level + job-level minimums. `ci.yml` grants workflow-level `contents: read` and declares no job-level perms. |
| Bootstrap sub-order swap (setup-android before setup-gradle) | INFO | Design listed setup-gradle before setup-android; implementation swaps them. Functionally equivalent — all precede Gradle commands. Spec `SHALL precede` clause satisfied. |
| Dependabot `update-types: [minor, patch]` excludes major | INFO | Enhancement beyond design; reduces major-bump noise. Acceptable. |
| `concurrency.group` uses `github.ref` only (design: `pull_request.number || ref`) | INFO | Per-PR cancellation still works for PRs (ref is the PR merge ref); minor deviation. |

### Risk Mitigation Table (20 risks from `exploration.md`)

| Risk | Severity | Mitigation in code | Status |
|---|---|---|---|
| R1 No Android SDK on runner → AGP fails | CRITICAL | `ci.yml:36-47`, `security.yml:34-40` — `setup-android@v4` + `sdkmanager` before Gradle | ✅ Mitigated |
| R2 `trivy-action@master` floating ref | CRITICAL | `security.yml:79` pinned `@0.28.0` (tag, not master); Dependabot refreshes | ✅ Mitigated |
| R3 Invalid `category:` on CodeQL `init` | CRITICAL | No `category:` on `init` step (`security.yml:42-48`) | ✅ Mitigated (init) — but see W3 for `analyze` |
| R4 Trivy `exit-code: "1"` fails first run | WARNING | `exit-code: "0"` + `ignore-unfixed: true` (`security.yml:84-85`) | ✅ Mitigated |
| R5 20-min cold timeout tight | WARNING | `timeout-minutes: 20` kept (design wanted 25) | ⚠️ Partial |
| R6 `./gradlew test` aggregator spans 3 modules | WARNING | Confirmed; desired behaviour; `--continue` keeps full signal | ✅ OK |
| R7 `build-mode: none` unsupported for Kotlin | WARNING | Manual `:app:assembleDebug` between init/analyze present, but `build-mode: manual` **not declared** | ⚠️ Partial |
| R8 `actions: read` default noise | WARNING | Not present in either workflow | ✅ Mitigated |
| R9 Workflow-level perms too broad | WARNING | `security.yml` top-level `{}` + job-level ✅; `ci.yml` top-level `contents: read`, no job-level | ⚠️ Partial |
| R10 No `workflow_dispatch` | SUGGESTION | Both workflows have `workflow_dispatch` | ✅ Mitigated |
| R11 `ignore-unfixed` | SUGGESTION | Present (`security.yml:85`) | ✅ Mitigated |
| R12 Trivy on PR wastes/blocks early | SUGGESTION | No `if:` guard — Trivy runs on PRs | ❌ Not mitigated |
| R13 Dependabot for `github-actions` | SUGGESTION | `dependabot.yml` present, `github-actions` ecosystem | ✅ Mitigated |
| R14 Required-status-check guidance | SUGGESTION | `docs/branch-protection.md` created — but check names mismatch job display names (W6) | ⚠️ Partial |
| R15 `paths-ignore` for docs PRs | SUGGESTION | `ci.yml:8-10` | ✅ Mitigated |
| R16 Cache config opacity | SUGGESTION | `setup-gradle@v4` implicit | ✅ Mitigated |
| R17 `gradlew` exec bit | NOTE | Mode 755; no `chmod` needed | ✅ N/A |
| R18 `local.properties` gitignored | NOTE | Workflow uses `ANDROID_HOME`/`ANDROID_SDK_ROOT` only | ✅ Honored |
| R19 Strict TDD flaky → `--continue` | NOTE | `--continue` on `test` (`ci.yml:54`) | ⚠️ See C1 — gate is red, not flaky |
| R20 Future instrumented tests need device | NOTE | Emulator placeholder comment (`ci.yml:62-69`) | ✅ Mitigated |

### Out-of-Scope verification

`AGENTS.md` §Out of Scope (for this scaffold) no longer lists `bootstrap-android-ci`:
- `git diff main..ci/bootstrap-android-ci -- AGENTS.md` removes exactly the `* CI / GitHub Actions — `bootstrap-android-ci`` bullet + its trailing blank line (2 deletions).
- `grep "CI / GitHub Actions" AGENTS.md` returns zero matches.
- ✅ Removal verified.

### Commit Hygiene

| Check | Result |
|---|---|
| Commits on `ci/bootstrap-android-ci` beyond `main` | 6 (`2b681a6`, `6b2792a`, `1f2dfdc`, `2fccd2b`, `1e94088`, `6be45db`) — 5 implementer + 1 apply-progress. ✅ (brief asked for 6) |
| Subject format = Conventional Commits | `ci(github): …`, `ci(deps): …`, `docs(scaffold): …`, `chore(sdd): …` — all valid types/scopes. ✅ |
| `Co-Authored-By:` footer absent | `git log -6 --format='%H %b'` shows no `Co-Authored-By` on any commit. ✅ |
| Body lines ≤ 100 chars (commitlint) | All commit bodies pass the Husky `commit-msg` hook (commits were accepted at author time). ✅ |

### TDD Compliance

| Check | Result | Details |
|---|---|---|
| TDD Evidence reported | ✅ | "TDD Cycle Evidence (Strict TDD — Config Discipline)" table present in `apply-progress.md` (8 rows). |
| All tasks have tests | ➖ | Config-YAML change; no Kotlin production code touched, so no new test files were required — **except** that a pre-existing guard test (`NoWorkflowsDirectory`) had to be migrated to match the MODIFIED spec, and it was not. |
| RED confirmed (test files exist) | ❌ | The MODIFIED "CI / GitHub Actions Deferred" scenario flipped from "no workflows dir" to "workflows dir exists", but `ToolingConventionsTest$NoWorkflowsDirectory` was never inverted/replaced. RED step for the spec modification was skipped. |
| GREEN confirmed (tests pass) | ❌ | `./gradlew test --rerun-tasks` exits 1 (1 failure / 21). The apply-reported green was a stale up-to-date cache. |
| Triangulation adequate | ➖ | Config-only; n/a. |
| Safety Net for modified files | ❌ | `AGENTS.md` was modified (2-line deletion) without re-running the dependent `ToolingConventionsTest` suite fresh; the broken `NoWorkflowsDirectory` test is exactly the missing safety net. |

**TDD Compliance**: 1/6 checks passed. Strict TDD was **violated** for the MODIFIED requirement: the behavioral change (`.github/workflows/` now exists) was not driven through a test change first, and the regression suite is red on `main`.

### Test Layer Distribution

| Layer | Tests | Files | Tools |
|---|---|---|---|
| Unit (JVM) | 21 | 5 `:app` + 4 `:domain` | JUnit 5 + AssertJ + MockK + Turbine |
| Integration | 0 | 0 | not installed (`config.yaml` `layers.integration.available: false`) |
| Instrumented / E2E | 0 | 0 | no emulator (deferred to `add-alarm-permissions` via the placeholder) |
| **Total** | **21 (1 failing)** | 9 | |

### Changed File Coverage

Coverage analysis skipped — no coverage tool detected (`testing.coverage.available: false`, no JaCoCo). `verify.coverage_threshold: 0`, so no threshold is breached. No Kotlin source changed by this change (the change is YAML + docs + a 2-line `AGENTS.md` deletion), so coverage is not meaningful here.

### Assertion Quality

No new test files were authored by this change, so no new assertions to audit. The relevant assertion-quality issue is the **stale guard** `ToolingConventionsTest.kt:263-278` (`NoWorkflowsDirectory`): its assertion `assertThat(workflows.exists()).isFalse()` is not trivial *per se*, but it is now **inverted in intent** relative to the MODIFIED spec — it should assert `isTrue` (or be renamed `WorkflowsDirectoryExists`). Leaving it as-is is the root cause of C1.

**Assertion quality**: 0 CRITICAL (new), 0 WARNING (new). 1 stale-guard issue recorded under C1.

### Quality Metrics

- **Linter**: ➖ Not available (`quality.linter.available: false`; actionlint not installed locally — `apply-progress` Issue #1; `gh workflow view` will validate on first run, which has now happened because the PR is merged).
- **Type Checker**: ➖ n/a (no Kotlin source touched).

## Findings

### CRITICAL (must fix before merge — change is already merged, so this is now a hotfix on `main`)

- **C1 — Strict-TDD gate is RED on `main`.** `./gradlew test --continue --rerun-tasks` exits 1: `ToolingConventionsTest$NoWorkflowsDirectory.github workflows directory does not exist at repo root()` fails with `Expecting value to be false but was true` (`app/build/test-results/testDebugUnitTest/TEST-com.syncalarm.app.ToolingConventionsTest$NoWorkflowsDirectory.xml`).
  - **Root cause**: The `bootstrap-android-scaffold` change added `ToolingConventionsTest$NoWorkflowsDirectory` (at `app/src/test/kotlin/com/syncalarm/app/ToolingConventionsTest.kt:263-278`) to guard the *old* "CI / GitHub Actions Deferred" scenario (`.github/workflows/` must NOT exist). This change's MODIFIED delta (`tooling-conventions/spec.md` "CI / GitHub Actions Deferred") flips that to "Workflows directory is created", and the implementation creates `.github/workflows/ci.yml` + `security.yml` — but the guard test was never updated. Strict TDD was violated: the spec was modified before the test was modified, and the test was never re-run fresh.
  - **Why the apply missed it**: Gradle marked `:app:testDebugUnitTest` UP-TO-DATE using a cached green marker from *before* `.github/workflows/` existed. Gradle's test up-to-date check keys on source/resource inputs, not on the runtime filesystem read `File("../.github/workflows")` inside the test body. The apply's "12 tests / 0 failures / exit 0 / 8s" evidence was that stale marker; a forced `--rerun-tasks` reveals the failure.
  - **Impact**: `main` (now `68c1a42`, post-merge) has a red strict-TDD gate. `ci.yml`'s own `./gradlew test --continue` step will be red on the next clean-runner CI execution. The change that was meant to *protect* `main` has instead put `main` in a red state on its first merged commit.
  - **Recommended fix** (do NOT do here — report only): open an immediate follow-up change (e.g. `fix(ci)-workflows-guard-test`) that inverts `ToolingConventionsTest$NoWorkflowsDirectory` to assert the workflows directory now exists (and that both `ci.yml` and `security.yml` are present), matching the MODIFIED spec scenario "Workflows directory is created". Run `./gradlew test --rerun-tasks` to confirm green. Until then, **`main` is not archive-ready and should not have further changes merged on top.**
  - **Spec reference**: `tooling-conventions` — MODIFIED requirement "CI / GitHub Actions Deferred", scenario "Workflows directory is created"; plus the strict-TDD gate contract in `openspec/config.yaml`.

### WARNING (should fix before archival)

- **W1 — `trivy` job uploads no SARIF.** `security.yml:78-86` uses `format: table` and has no `github/codeql-action/upload-sarif@v3` step. The spec scenario "Initial scan is warning-only" requires "findings SHALL be uploaded as SARIF". The success criteria in `proposal.md` also require "SARIF uploaded". The Security tab will not be populated. **Fix**: add `format: sarif` / `output: trivy-results.sarif` and an `upload-sarif` step (the `security-events: write` permission is already granted). This is a scenario blocker.
- **W2 — CodeQL `build-mode: manual` is not declared.** `security.yml:42-48`'s `init` step omits `build-mode: manual`. The default for `java-kotlin` is `autobuild`; the explicit `:app:assembleDebug` build step (`security.yml:53-54`) is present but CodeQL may also run its own autobuild and stumble on Hilt KSP transforms (R7). Design D7 explicitly called for `build-mode: manual`. **Fix**: add `build-mode: manual` under the `init` `with:`.
- **W3 — `analyze` step carries a custom `category`.** `security.yml:61` sets `category: "/language:java-kotlin"`. The spec scenario "Manual CodeQL build completes in order" requires "analysis SHALL upload SARIF without a custom `category`". The value is a valid free-form label on `analyze` (not the invalid init input R3 warned about), but it contradicts the literal spec and the user-locked decision D7 ("no `category:`"). **Fix**: remove line 61 (or amend the spec scenario if the label is intentionally kept).
- **W4 — Trivy PR-skip guard absent (R12).** The workflow-level `pull_request` trigger applies to the `trivy` job; with `exit-code: "0"` it doesn't block PRs but wastes ~90s per PR. Design R12 explicitly wanted Trivy off on PRs in the first cut. **Fix**: add `if: github.event_name != 'pull_request'` to the `trivy` job (or amend design R12).
- **W5 — `ci.yml` uses workflow-level `permissions: contents: read`, no job-level perms (R9).** Design wanted top-level `permissions: {}` + job-level minimums for `ci.yml` too. `security.yml` follows R9; `ci.yml` does not. **Fix**: set top-level `permissions: {}` and add `permissions: { contents: read }` under `build-and-test`.
- **W6 — Required-check names in `docs/branch-protection.md` do not match the actual GitHub check names.** The doc lists `build-and-test`, `codeql`, `trivy-fs-scan` (job keys), but each workflow sets a display `name:` — `ci.yml:22` `name: Build & Unit Tests`, `security.yml:16` `name: CodeQL (Kotlin/Java)`, `security.yml:66` `name: Trivy Filesystem Scan`. GitHub surfaces the check run name from the job's display `name`, so branch protection configured per the doc's job-key names would **not match** the produced checks. **Fix**: either remove the `name:` lines (so checks surface as job keys, matching the doc) or update the doc table to list `Build & Unit Tests`, `CodeQL (Kotlin/Java)`, `Trivy Filesystem Scan`. This directly affects the change's stated goal (protecting `main`).

### SUGGESTION (follow-up improvements)

- **S1 — Branch-name enforcement is documentation-only.** The "Protection validates branch naming" scenario is a SHOULD, satisfied by documented guidance + a planned `branch-name-lint` follow-up. Fine for v1; track the follow-up.
- **S2 — No `actions/` SHA-pinning despite Dependabot being configured for it.** Design D5 allowed tag-min for 3rd-party, but the Dependabot `github-actions` ecosystem is more valuable when actions are SHA-pinned (Dependabot then bumps the SHA + comment). Consider SHA-pinning `trivy-action@0.28.0` and `setup-android@v4` in a follow-up.
- **S3 — `actionlint` was not available locally.** Now that the PR is merged, GitHub has validated the workflow syntax on first run. Consider installing `actionlint` in the dev environment (and eventually as a CI job) for offline validation.
- **S4 — `concurrency.group` in `ci.yml` uses `github.ref` only.** Design used `github.event.pull_request.number || github.ref` for stricter per-PR grouping. Minor; current grouping still cancels superseded runs.

### INFO (informational notes)

- **I1 — PR #5 is already MERGED.** The verify brief assumed state OPEN; `gh pr view 5` returns `"state": "MERGED"` and `origin/main` is at `68c1a42` (the merge commit). The implementation is on `main`; the CRITICAL regression C1 is therefore live on `main`, not just on a feature branch.
- **I2 — Bootstrap sub-order swap.** `ci.yml`/`security.yml` run `setup-android` + `sdkmanager` *before* `setup-gradle`, whereas design listed setup-gradle first. Functionally equivalent (no Gradle command runs before either), spec `SHALL precede` clause met.
- **I3 — `apply-progress.md` undercounts tests.** It reports "12 tests"; the actual suite is 21 distinct JVM tests (4 `:domain` + 17 `:app` debug; `:app:test` runs debug+release → 38 executions). The "12" appears to be a miscount of the cached `-debugUnitTest` subset. Non-blocking, but the evidence trail is inaccurate.

## Next Step

- **NOT ready for `sdd-archive`.** Verdict FAIL.
- The change is already merged to `main`, so the CRITICAL regression (C1) is live. Recommended immediate action:
  1. Open a hotfix change (e.g. `fix(ci)-workflows-guard-test`) that inverts/replaces `ToolingConventionsTest$NoWorkflowsDirectory` to match the MODIFIED "Workflows directory is created" scenario, and run `./gradlew test --rerun-tasks` to prove green.
  2. In the same or a follow-up change, address W1–W6 (Trivy SARIF upload, CodeQL `build-mode: manual`, drop `analyze` `category`, Trivy PR-skip guard, `ci.yml` permissions model, and the branch-protection check-name mismatch).
  3. Only after `./gradlew test` is green on `main` and the 2 partial scenarios are resolved (or formally amended in spec) should `sdd-archive bootstrap-android-ci` be run.
- If CRITICAL C1 is somehow resolved out-of-band and the 2 partial scenarios are accepted/amended, re-running this verification could yield `pass` — but as observed, the verdict is `fail` with 1 critical finding and 3 blockers.