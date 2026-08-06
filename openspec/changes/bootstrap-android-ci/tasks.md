# Tasks: bootstrap-android-ci

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~271 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Likely PR | Focused test command | Runtime harness | Rollback boundary |
|------|------|-----------|----------------------|-----------------|-------------------|
| 1 | Add artifacts and AGENTS edit | PR 1 | `actionlint`/`gh workflow view`; Gradle gates | N/A: requires GitHub push/PR | Revert listed files |

## Phase 1: CI workflow scaffolding

- [x] **1.1** Description: Create `.github/workflows/ci.yml`: build job, main triggers, cancellation, read-only permissions, checkout → Temurin 17 → Android/Gradle/SDK 35 → test/app/data assemblies, emulator placeholder. Files: `.github/workflows/ci.yml`. Pre: Gradle scaffold exists. Verify: `actionlint` or `gh workflow view`; syntax validation substitutes RED/GREEN for config TDD. Commit: `ci(build): add Android build workflow`. Estimate: ~70 lines.
- [x] **1.2** Description: Add `paths-ignore: ['docs/**', '**.md']`. Files: `.github/workflows/ci.yml`. Pre: 1.1. Verify: read-back confirms docs PR skip and code PR execution intent. Commit: `ci(build): skip docs-only pull requests`. Estimate: ~5 lines.
- [x] **1.3** Description: Add informational `tighten-trivy-gate` comment without behavior changes. Files: `.github/workflows/ci.yml`. Pre: 1.1. Verify: comment exists and gate remains unchanged. Commit: `ci(build): document Trivy gate follow-up`. Estimate: ~3 lines.

## Phase 2: Security workflow

- [x] **2.1** Description: Create CodeQL in `.github/workflows/security.yml`: triggers, empty top-level permissions, job permissions, `java-kotlin`, `manual`, no category, checkout → Java → init → SDK/build → analyze. Files: `.github/workflows/security.yml`. Pre: 1.1. Verify: actionlint/`gh workflow view`; ordering is the config-TDD RED check. Commit: `ci(security): add manual CodeQL analysis`. Estimate: ~50 lines.
- [x] **2.2** Description: Add `trivy-fs-scan` using `aquasecurity/trivy-action@0.28.0`, fs/HIGH,CRITICAL/table/SARIF, `exit-code: "0"`, `ignore-unfixed: true`, and PR skip. Files: `.github/workflows/security.yml`. Pre: 2.1. Verify: triggers/permissions match CodeQL and PR skip is read back, then validate YAML. Commit: `ci(security): add warning-only Trivy scan`. Estimate: ~40 lines.

## Phase 3: Dependabot

- [x] **3.1** Description: Create `.github/dependabot.yml` version 2 with weekly GitHub Actions updates, groups/labels, and a comment deferring Gradle. Files: `.github/dependabot.yml`. Pre: action refs finalized. Verify: YAML read-back/parser confirms validity. Commit: `ci(deps): configure GitHub Actions Dependabot updates`. Estimate: ~20 lines.

## Phase 4: Documentation

- [x] **4.1** Description: Create `docs/branch-protection.md` covering main PR/green-CI, checks, branch prefixes/scopes, worktrees/house rules, and v1 follow-ups. Files: `docs/branch-protection.md`. Pre: job names fixed. Verify: checks, sections, and `../syncalarm-<branch>` exist. Commit: `docs(ci): document branch protection protocol`. Estimate: ~80 lines.
- [x] **4.2** Description: Remove the CI/GitHub Actions Out-of-Scope bullet. Files: `AGENTS.md`. Pre: 4.1. Verify: no stale bullet remains. Commit: `docs(agents): close CI out-of-scope item`. Estimate: ~3 lines.

## Phase 5: Verification

- [x] **5.1** Run `./gradlew test`, `./gradlew :app:assembleDebug`, `./gradlew :data:assembleDebug`; check clean `git status`. Files: none. Pre: phases 1–4. Verify: commands exit 0.
- [x] **5.2** Run `actionlint`/YAML validation; fallback: document `gh workflow view` after push. Files: none. Pre: workflows exist. Verify: result recorded.
