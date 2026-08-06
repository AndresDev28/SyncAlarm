# Archive Report — bootstrap-android-ci

**Change**: `bootstrap-android-ci`
**Archived**: 2026-08-06
**Branch**: `ci/bootstrap-android-ci` (PR #5) → `fix/ci-test-guard-migration` (PR #6)
**Mode**: openspec only (no Engram coupling; artifact store mode is `openspec` per session preflight)
**Strict TDD**: enabled (`openspec/config.yaml`)
**Final commit**: `f916816` (main HEAD)

## Outcome

**SUCCESS**. The change is feature-complete, validated, and merged to `main`. The strict-TDD gate (`./gradlew test --rerun-tasks`) is green at the merged commit.

## Phase Trace

| Phase | Artifact | Status |
|---|---|---|
| sdd-explore | `exploration.md` (315 lines, 20 risks) | success |
| sdd-propose | `proposal.md` (145 lines) | success |
| sdd-spec | `specs/build-system/spec.md`, `specs/tooling-conventions/spec.md` (deltas) | success |
| sdd-design | `design.md` (302 lines, 3 mermaid sequence diagrams) | success |
| sdd-tasks | `tasks.md` (49 lines, 11 tasks) | success |
| sdd-apply | `apply-progress.md` (201 lines, 6 commits) | success |
| sdd-verify (initial) | `verify-report.md` | **failed** (1 CRITICAL, 2 PARTIAL, 6 WARNING) |
| sdd-remediate | 3 commits on `fix/ci-test-guard-migration`, PR #6 | success |
| sdd-verify (re-verify) | `verify-report-2.md` | **pass** (0 CRITICAL, 0 WARNING, 9/9 requirements, 20/20 scenarios) |
| sdd-archive | `archive-report.md` (this file) | — |

## Spec Deltas Applied

### `build-system` (5 ADDED requirements)

- `CI Workflow Exists` (4 scenarios)
- `Main Branch Requires Green CI` (2 scenarios)
- `CodeQL Java and Kotlin Analysis` (2 scenarios; **W1 + W4 fixed** in remediation)
- `Weekly Trivy Filesystem Scan` (3 scenarios; **W2 + W5 fixed** in remediation)
- `Emulator Runner Forward Compatibility` (1 scenario)

### `tooling-conventions` (1 REMOVED + 4 ADDED requirements)

- REMOVED: `CI / GitHub Actions Deferred` (the old negative contract)
- MODIFIED: `CI / GitHub Actions Deferred` → `CI / GitHub Actions Delivered` (with `tighten-trivy-gate` follow-up note)
- ADDED: `Branch Naming Convention` (3 scenarios)
- ADDED: `Git Worktrees for Concurrent Work and Hotfixes` (3 scenarios)
- ADDED: `Main Branch Requires Pull Requests and Green CI` (2 scenarios)

**Totals**: 9 requirements across 2 capabilities, 20 scenarios.

## Validation Evidence (Final)

| Gate | Command | Result | Exit |
|------|---------|--------|------|
| Strict TDD | `./gradlew test --rerun-tasks` | BUILD SUCCESSFUL — 21/21 tests | 0 |
| Build app | `./gradlew :app:assembleDebug --rerun-tasks` | BUILD SUCCESSFUL — APK packaged | 0 |
| Build data | `./gradlew :data:assembleDebug --rerun-tasks` | BUILD SUCCESSFUL — AAR packaged | 0 |
| PR #6 | `gh pr view 6` | MERGED at `f916816` | — |

## Runtime Ledger Final State

```
{
  "complete": true,
  "next_action": "complete",
  "lifetime_attempts": 3,
  "lifetime_changed_lines": 879,
  "evidence_revision": "sha256:eacc474dcd01495cf5088ec9121f4334b263169f72a632e8ae1de4ace8e08ca5"
}
```

3 attempts total:
1. `sdd-apply` (ordinal 1) — passed, 518 lines, budget exceeded by 118 (reset by maintainer)
2. `sdd-verify` (ordinal 2) — failed, 259 lines, CRITICAL surfaced
3. `sdd-remediate` (ordinal 3) — passed, 102 lines, all gates green

## Final Diff (main HEAD)

```
.github/dependabot.yml                             |  26 +
.github/workflows/ci.yml                           |  70 ++ (after W3 fix)
.github/workflows/security.yml                     | 100 + (after W1-W5 fixes)
AGENTS.md                                          |   2 -  (stale CI bullet removed)
README.md                                          |   4 +- (CI deferral -> delivery)
docs/branch-protection.md                          | 117 ++ (after W6 fix)
app/src/test/kotlin/com/syncalarm/app/ToolingConventionsTest.kt |  55 +- (test guard migrated)
```

**Authored lines** (excluding test guard migration which is a hotfix): 296 + 67 = 363 across 2 PRs.

## Lessons Captured

A non-obvious lesson was captured in Engram under `lesson/gradle-cache-fs-tests`:

> **Gradle's `UP-TO-DATE` cache does NOT track filesystem reads inside test bodies.** When a test asserts a contract on the project's filesystem (e.g., `File("../.github/workflows").exists()`), Gradle can mark the test task up-to-date even after the filesystem changes, producing a false-green validation result. For changes that alter files which a test asserts on at runtime, apply-phase validation MUST use `./gradlew test --rerun-tasks` (or `./gradlew test --rerun-tasks --continue`). The original apply reported "12/12 green" because of a stale up-to-date cache; the actual state was a regression that surfaced only after `--rerun-tasks`.

This is the project's first encounter with this class of error. The fix (a 3-commit hotfix in PR #6) is now in `main`.

## Files Archived

```
openspec/changes/archive/2026-08-06-bootstrap-android-ci/
├── apply-progress.md
├── archive-report.md    (this file)
├── design.md
├── exploration.md
├── proposal.md
├── specs/
│   ├── build-system/spec.md
│   └── tooling-conventions/spec.md
├── tasks.md
├── verify-report.md     (initial verify, FAILED)
└── verify-report-2.md   (re-verify after remediation, PASS)
```

## Convention Compliance

- ✅ Conventional Commits on all 10 commits (no `Co-Authored-By` footers)
- ✅ Branch names follow `ci/*` and `fix/*` conventions
- ✅ All planning artifacts under `openspec/changes/archive/2026-08-06-bootstrap-android-ci/`
- ✅ Strict TDD config discipline applied (RED stub → GREEN feature → TRIANGULATE → REFACTOR)
- ✅ Spec deltas documented with Given/When/Then scenarios
- ✅ RFC 2119 keywords (MUST, SHALL, SHOULD, MAY) used consistently
- ✅ OpenSpec delta format respected (`## ADDED Requirements`, `## MODIFIED Requirements`, `## REMOVED Requirements`)

## Forward-compat Hooks

- **Emulator runner placeholder** in `ci.yml` — `reactivecircus/android-emulator-runner@v2` can be added without restructuring `build-and-test`.
- **tighten-trivy-gate** — follow-up change to flip Trivy from `exit-code: "0"` to `exit-code: "1"` after the first scan is triaged.
- **branch-name-lint GitHub Action** — suggested for a follow-up change to enforce the `feat/*`, `fix/*`, etc. naming in branch protection.
- **actionlint in CI** — suggested for a follow-up change to add offline workflow validation.

## Out of Scope (Confirmed)

- No emulator / instrumented tests (forward-compat only)
- No signed release builds / Play Store deploy
- No Lint/Detekt gate
- No Kover/JaCoCo coverage gate
- No PR labeling bot
- No CODEOWNERS

## Repository Standpoint After Archive

`main` is now in a state where:
1. The strict-TDD gate is green (`./gradlew test --rerun-tasks` passes).
2. CI is enabled and will run on every push/PR to `main`.
3. Trivy is in warning-only mode (first scan will be uploaded as SARIF to the Security tab).
4. Branch protection is **NOT YET** enabled in the GitHub repo settings — a follow-up admin action is required to enforce the rules documented in `docs/branch-protection.md`. This is documented in the PR #6 test plan as a manual checklist.

## Next Steps (for the next session)

1. **Enable branch protection on `main`** in the GitHub repo settings (Settings → Branches → Add rule). Use the display names from `docs/branch-protection.md`:
   - `Build & Unit Tests`
   - `CodeQL (Kotlin/Java)`
   - `Trivy Filesystem Scan`
2. **Monitor the first Trivy scan** (the weekly cron will run Monday 06:00 UTC). Triage findings before unzipping `tighten-trivy-gate`.
3. **Start `add-alarm-permissions`** — the instrumented tests can now plug into the placeholder in `ci.yml`.
4. **Address Husky v9 deprecation** — pre-existing from `bootstrap-android-scaffold`, will break on v10.

## Archived

This change is now closed. The `bootstrap-android-ci` folder under `openspec/changes/` is removed; the artifacts live only in `openspec/changes/archive/2026-08-06-bootstrap-android-ci/`.
