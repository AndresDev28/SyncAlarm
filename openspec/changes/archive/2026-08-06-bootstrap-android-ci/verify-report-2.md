```yaml
schema: gentle-ai.verify-result/v1
evidence_revision: sha256:eacc474dcd01495cf5088ec9121f4334b263169f72a632e8ae1de4ace8e08ca5
verdict: pass
blockers: 0
critical_findings: 0
requirements: 9/9
scenarios: 20/20
test_command: ./gradlew test --rerun-tasks
test_exit_code: 0
test_output_hash: sha256:$(./gradlew test --rerun-tasks 2>&1 | sha256sum | awk '{print $1}')
build_command: ./gradlew :app:assembleDebug
build_exit_code: 0
build_output_hash: sha256:4b0a51bcc60c4c868ff83330a77b2e6506e04756816d30a31122b59f3874784f
```

## Verification Report — Post-Remediation Re-verify

**Change**: bootstrap-android-ci
**Version**: N/A (2 MODIFIED capability specs: `build-system`, `tooling-conventions`)
**Mode**: Strict TDD (`openspec/config.yaml` `testing.strict_tdd: true`, runner `./gradlew test`)
**Commit verified**: `f916816` (merge of PR #6 into `origin/main`; branch `fix/ci-test-guard-migration` at `5d9be51`). **PR #6 state: MERGED**.
**Re-verify date**: 2026-08-06

### Summary

- Status: **pass** — all CRITICAL, WARNING, and PARTIAL findings from the original verify are resolved.
- Specs verified: 2 capabilities (`build-system`, `tooling-conventions`).
- Requirements: 9 total — **9/9 COMPLIANT** (was 8/9 with 1 PARTIAL).
- Scenarios: 20 total — **20/20 COMPLIANT** (was 18/20 with 2 PARTIAL).
- Warnings: 0 (was 6).
- Suggestions: 4 (preserved from original).
- INFO: 3 (preserved from original).
- **CRITICAL findings: 0** (was 1).

### Remediation Trace

The original verify-report (this same file before remediation) flagged:

| # | Finding | Severity | Resolution |
|---|---|---|---|
| C1 | `ToolingConventionsTest$NoWorkflowsDirectory` failed on main after PR #5 merge — test asserts no `.github/workflows/` exists; spec delta did not migrate the test guard | CRITICAL | Commit `8ee0237` — `WorkflowsDirectoryExists` replaces `NoWorkflowsDirectory`; asserts directory + `ci.yml` + `security.yml` exist; README updated to "delivered by" |
| W1 | CodeQL `analyze` had a `category:` input that the spec forbids | WARNING | Commit `2fe5fcc` — removed `category:` from `analyze`; init keeps explicit comment |
| W2 | Trivy did not upload SARIF (only `format: table`) | WARNING | Commit `2fe5fcc` — Trivy emits `format: sarif` + `output: trivy-fs.sarif`; new `codeql-action/upload-sarif@v3` step uploads to the Security tab |
| W3 | CI timeout 20 min vs design's 25 min | WARNING | Commit `2fe5fcc` — bumped `timeout-minutes: 20` → `25` in `ci.yml` |
| W4 | CodeQL `build-mode: manual` missing (implicit only) | WARNING | Commit `2fe5fcc` — added explicit `build-mode: manual` to `codeql-action/init` |
| W5 | Trivy ran on PRs (no `if: github.event_name != 'pull_request'`) | WARNING | Commit `2fe5fcc` — added `if: github.event_name != 'pull_request'` to the Trivy job |
| W6 | `docs/branch-protection.md` listed job keys (`build-and-test`, `codeql`, `trivy-fs-scan`) instead of display names | WARNING | Commit `5d9be51` — table now lists `Build & Unit Tests`, `CodeQL (Kotlin/Java)`, `Trivy Filesystem Scan`; an explanatory note clarifies that GitHub matches the `name:` field |

### Validation Evidence

| Gate | Command | Result | Exit |
|------|---------|--------|------|
| 1 | `./gradlew test --rerun-tasks` (fresh) | BUILD SUCCESSFUL — 21/21 tests pass | 0 |
| 2 | `./gradlew :app:assembleDebug --rerun-tasks` | BUILD SUCCESSFUL — APK packaged | 0 |
| 3 | `./gradlew :data:assembleDebug --rerun-tasks` | BUILD SUCCESSFUL — AAR packaged | 0 |
| 4 | `gh pr view 6` | Title descriptive (`fix(ci): migrate test guard + harden security workflow`), body has Summary/Changes/Test Plan, label `bug`, **state = MERGED** at `f916816` | — |

### Spec Traceability (post-remediation)

| Capability | Requirement | Scenarios | Status |
|---|---|---|---|
| `build-system` | CI Workflow Exists | 4 / 4 | ✅ COMPLIANT |
| `build-system` | Main Branch Requires Green CI | 2 / 2 | ✅ COMPLIANT |
| `build-system` | CodeQL Java and Kotlin Analysis | 2 / 2 | ✅ COMPLIANT (W1 + W4 fixed) |
| `build-system` | Weekly Trivy Filesystem Scan | 2 / 2 | ✅ COMPLIANT (W2 + W5 fixed) |
| `build-system` | Emulator Runner Forward Compatibility | 1 / 1 | ✅ COMPLIANT |
| `tooling-conventions` | Branch Naming Convention | 3 / 3 | ✅ COMPLIANT |
| `tooling-conventions` | Git Worktrees for Concurrent Work and Hotfixes | 3 / 3 | ✅ COMPLIANT |
| `tooling-conventions` | Main Branch Requires Pull Requests and Green CI | 2 / 2 | ✅ COMPLIANT |
| `tooling-conventions` | CI / GitHub Actions Deferred (MODIFIED) | 1 / 1 | ✅ COMPLIANT (test guard migrated) |

**Totals**: 9/9 requirements, 20/20 scenarios.

### `apply-progress.md` Calibration Note

The original `apply-progress.md` reported "12 tests / 0 failures" with `./gradlew test` exit 0. That result was a stale-cache green marker — Gradle's `UP-TO-DATE` for `:app:testDebugUnitTest` does not track the runtime filesystem read `File("../.github/workflows")` inside the test body. The cache had the old green marker from before `.github/workflows/` existed, and the marker persisted even after the directory the test guards against was created.

Future apply phases **MUST** run `./gradlew test --rerun-tasks` (or `--rerun-tasks --continue`) for any change that alters files which a test asserts on at runtime. This lesson was captured in Engram under topic_key `lesson/gradle-cache-fs-tests`.

### Suggestions (preserved)

The original verify flagged 4 SUGGESTION items that remain relevant for follow-up changes; they are not blocking this one:

1. **actionlint in CI**: Add an `actionlint` job for offline workflow validation.
2. **branch-name-lint GitHub Action**: Enforce the `feat/*`, `fix/*`, etc. conventions in branch protection.
3. **Husky v9 deprecation preamble**: Already tracked; pre-existing from `bootstrap-android-scaffold`.
4. **Auto-merge bot**: Out of scope for this change.

### INFO Notes (preserved)

- The bootstrap step ordering in `ci.yml` has `setup-android` BEFORE `setup-gradle` (cf. design which had them adjacent). The spec's `SHALL precede` clause is satisfied either way; no functional impact.
- The `paths-ignore` filter in `ci.yml` only applies to `pull_request`; pushes to `main` from the PR merge would still trigger a full CI run. This is by design (post-merge integrity gate).

### Next Step

**Ready for `sdd-archive bootstrap-android-ci`**. The change is feature-complete, all gates are green, and the spec deltas are ready to be applied to the base capability specs.
