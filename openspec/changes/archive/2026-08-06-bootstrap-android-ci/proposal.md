# Proposal: bootstrap-android-ci

## Intent

The repo at `b4a2cc1` has **zero** CI: no `.github/`, no required checks, no Dependabot. `main` is unprotected — every future merge can break `./gradlew test` (the strict-TDD gate, `openspec/config.yaml` `testing.strict_tdd: true`) with no automated signal. The PRD has **no CI functional requirement**: PRD §6 mandates only a TDD-friendly Clean Architecture. This change is therefore **operational hardening, not feature work** — it protects PRD §6's TDD promise and the PRD §3 reliability goal (100% alarm activation) by making `main` always buildable and testable. It also closes the explicit `AGENTS.md` §Out-of-Scope bullet ("CI / GitHub Actions — `bootstrap-android-ci`") and shapes the device runner that `add-alarm-permissions` instrumented tests are deferred to. No new product behavior; zero Kotlin source touched.

## Scope

### In Scope (4 artifacts, user-locked)
- `.github/workflows/ci.yml` — build + unit tests on every push/PR
- `.github/workflows/security.yml` — CodeQL `java-kotlin` + Trivy fs scan, weekly cron
- `.github/dependabot.yml` — GitHub Actions version hygiene
- `docs/branch-protection.md` — human-readable required-status-checks guide for repo admins (not enforced by YAML)
- SDD bookkeeping: delta specs for `build-system` + `tooling-conventions`; remove the CI bullet from `AGENTS.md` §Out of Scope

### Out of Scope (non-goals)
- Emulator / instrumented tests (→ follow-up PR for `add-alarm-permissions`)
- Signed release builds, Play Store deploy
- Lint / Detekt / ktlint gate
- Kover / JaCoCo coverage gate
- PR labeling bots, `.github/CODEOWNERS` (marginal value, one owner)
- Enforcing branch protection via API (repo-settings action, documented only)
- Trivy gate tightening (→ `tighten-trivy-gate` follow-up)

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `build-system`: ADDED requirement "CI workflow exists" — `.github/workflows/ci.yml` SHALL run `./gradlew test` and `./gradlew :app:assembleDebug` on push/PR with Android SDK 35 provisioned (mirrors the "Gradle Wrapper Pinned" pattern).
- `tooling-conventions`: MODIFIED requirement "CI / GitHub Actions Deferred" — rewritten to state CI now exists under `bootstrap-android-ci`; the "no workflows directory" scenario is removed (it becomes self-contradicting).

## Approach

**`.github/workflows/ci.yml`** — job `build-and-test` on `ubuntu-latest`, `timeout-minutes: 25` (R5), top-level `permissions: {}` + job-level `contents: read` (R9). Triggers: `push` to `main`, `pull_request` with `paths-ignore: ['docs/**', '**.md']` (R15), `workflow_dispatch` (R10); concurrency cancel-in-progress. Step order (SDK before any Gradle step, R1):
1. `actions/checkout@v4`
2. `actions/setup-java@v4` (temurin, 17)
3. `gradle/actions/setup-gradle@v4` (Gradle home + config-cache automatic; no separate `actions/cache`)
4. `android-actions/setup-android@v4` (SHA-pinned; sets `ANDROID_HOME`/`ANDROID_SDK_ROOT` — `local.properties` is gitignored, R18)
5. `sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"` (compileSdk match from `libs.versions.toml`)
6. `./gradlew test --continue` (all 3 modules; single failure doesn't mask the rest, R19)
7. `./gradlew :app:assembleDebug`
8. `./gradlew :data:assembleDebug` (cheap; catches `:data` regressions — exploration guard Q6)
9. Documented placeholder comment: future `instrumented` job via `reactivecircus/android-emulator-runner@v2` (forward-compat, user-locked)

**`.github/workflows/security.yml`** — triggers: `push` to `main`, `pull_request` to `main`, `schedule: cron: '0 6 * * 1'`, `workflow_dispatch`. Top-level `permissions: {}`; `actions: read` dropped (it is the default, R8).
- Job `codeql` (`contents: read` + `security-events: write`): checkout → setup-java → setup-gradle → setup-android + `sdkmanager` (manual build needs the SDK) → `github/codeql-action/init` with `languages: java-kotlin`, `build-mode: manual`, **no `category:`** (R3 — invalid value; default suite covers Java+Kotlin) → `./gradlew :app:assembleDebug` (Kotlin requires a real build, R7) → `github/codeql-action/analyze` → SARIF upload.
- Job `trivy-fs` (`contents: read` + `security-events: write`), `if: github.event_name != 'pull_request'` (no PR gating in first cut, R12): `aquasecurity/trivy-action@0.28.0` (SHA-pinned; `@master` floating ref hard-rejected, R2) with `scan-type: fs`, `severity: HIGH,CRITICAL`, `ignore-unfixed: true` (R11), `exit-code: "0"` (warning-only, user-locked), SARIF upload to populate the Security tab.

**`.github/dependabot.yml`** — `version: 2`, weekly schedule, `package-ecosystem: github-actions` on `/` (keeps SHA pins fresh, R13). Note inside the file: `gradle` ecosystem lands in a follow-up once `libs.versions.toml` is stable enough for auto-PRs; `npm` (commitlint/Husky) deferred likewise.

**`docs/branch-protection.md`** — recommends required status checks on `main`: `build-and-test` (now), `codeql` (now), `trivy-fs-scan` (requirable only after `tighten-trivy-gate` adds the PR trigger). Rationale per check + "require branch up to date" and PR-review recommendations. Explicitly documentation: branch protection lives in repo settings, not in workflow YAML.

## Architectural Decisions

| # | Decision | Source | Rationale |
|---|---|---|---|
| D1 | Trivy gate warning-only: `exit-code: "0"` + `ignore-unfixed: true`; tighten in follow-up `tighten-trivy-gate` | User-locked | First scan of fresh Android transitive deps almost always finds HIGH/CRITICAL; don't break `main` on day 1 (R4) |
| D2 | All four artifacts in scope | User-locked | One review, one coherent CI bootstrap |
| D3 | ONE OpenSpec change (no CI-vs-Security split) | User-locked | Same `.github/` surface, same SDK/JDK bootstrap, fits review budget (Approach F) |
| D4 | Emulator placeholder comment in `ci.yml` for `reactivecircus/android-emulator-runner@v2` | User-locked | `add-alarm-permissions` instrumented tests become a one-job follow-up PR |
| D5 | SHA-pin third-party actions; Dependabot `github-actions` keeps them fresh | Exploration A | Supply-chain safety; matches commitlint/Husky posture; `@master` hard-rejected (R2) |
| D6 | `android-actions/setup-android@v4` + explicit `sdkmanager` before any Gradle step | Exploration C | Runner has no Android SDK; AGP fails fast at `checkDebugAarMetadata` without it (R1) |
| D7 | CodeQL `build-mode: manual`, omit `category:`, job-level permissions, drop `actions: read` | Exploration D/E | Kotlin unsupported with `build-mode: none` (R7); `category:` was invalid (R3); least privilege (R8/R9) |
| D8 | Caching via `gradle/actions/setup-gradle@v4` only | Exploration | Implicit Gradle home + config-cache; no separate `actions/cache@v4` needed (R16) |

## Forward-Compat Hooks

- `ci.yml` placeholder comment marks where the `instrumented` emulator job lands (D4); `runs-on: ubuntu-latest` and job-level permissions mean the future job is additive — no edit to `build-and-test`.
- `setup-gradle` cache key is OS-scoped, so a future emulator job reuses the warm Gradle cache.
- `tighten-trivy-gate` follow-up flips `exit-code: "1"` and adds the `pull_request` trigger after the first scan is triaged (D1).

## Forward-Compat References

- `openspec/specs/build-system/spec.md` — `verify.build_command` / `verify.test_command` contract this change keeps green on every push.
- `openspec/changes/add-alarm-permissions/proposal.md` — instrumented tests "deferred to `bootstrap-android-ci` device runner"; this change designs that runner's landing zone.
- `AGENTS.md` §Out of Scope — this change closes the "CI / GitHub Actions — `bootstrap-android-ci`" bullet.

## Affected Areas

| Area | Impact | Description |
|---|---|---|
| `.github/workflows/ci.yml` | New | PR/push build + unit tests, SDK provisioning |
| `.github/workflows/security.yml` | New | CodeQL java-kotlin + Trivy fs (warning-only) |
| `.github/dependabot.yml` | New | `github-actions` ecosystem, weekly |
| `docs/branch-protection.md` | New | Required-status-checks guide for admins |
| `openspec/specs/build-system/spec.md` | Modified | Delta: "CI workflow exists" requirement |
| `openspec/specs/tooling-conventions/spec.md` | Modified | Delta: rewrite "CI Deferred" requirement |
| `AGENTS.md` | Modified | Remove CI out-of-scope bullet |

## Impact (Downstream Unblocks)

| Future change | Unblocked by this PR |
|---|---|
| `add-alarm-permissions` | Emulator-runner landing zone for instrumented tests |
| `tighten-trivy-gate` | Populated Security tab to triage before flipping the gate |
| Every feature change | Protected `main`: `./gradlew test` + `:app:assembleDebug` green on every PR |

## Risks

Sorted by severity (full analysis in `exploration.md` §Risks).

| # | Sev | Risk | Mitigation |
|---|---|---|---|
| R1 | CRITICAL | No Android SDK on runner; AGP fails at `checkDebugAarMetadata` | `setup-android@v4` + `sdkmanager` before Gradle steps |
| R2 | CRITICAL | `trivy-action@master` floating ref = supply-chain risk | Pin `@0.28.0` (SHA preferred) + Dependabot |
| R3 | CRITICAL | `category: "/language:java-kotlin"` invalid for CodeQL | Omit `category:` entirely; default suite covers both |
| R4 | WARNING | Trivy `exit-code: "1"` fails first run | Warning-only now (D1); tighten in follow-up |
| R5 | WARNING | 20-min timeout tight on cold first run | `timeout-minutes: 25` |
| R6 | WARNING | `./gradlew test` aggregator spans 3 modules | Desired behavior; matches `verify.test_command` |
| R7 | WARNING | `build-mode: none` unsupported for Kotlin | `build-mode: manual` + explicit assemble step |
| R8 | WARNING | `actions: read` is default; noise for auditors | Dropped from permissions |
| R9 | WARNING | Workflow-level permissions too broad | Top-level `permissions: {}` + job-level minimums |
| R10 | SUGGESTION | No manual re-run path | `workflow_dispatch` added |
| R11 | SUGGESTION | Unfixed transitive vulns create noise | `ignore-unfixed: true` |
| R12 | SUGGESTION | Trivy on `pull_request` wastes/blocks PRs early | Trivy job skips PR events in first cut |
| R13 | SUGGESTION | SHA pins rot without a bot | `dependabot.yml` `github-actions` ecosystem |
| R14 | SUGGESTION | No required-status-check guidance | `docs/branch-protection.md` |
| R15 | SUGGESTION | Docs-only PRs trigger full builds | `paths-ignore: ['docs/**', '**.md']` |
| R16 | SUGGESTION | Cache config opacity | Implicit via `setup-gradle@v4`; documented in design |
| R17 | NOTE | `gradlew` exec bit | Already mode 755; no `chmod` needed |
| R18 | NOTE | `local.properties` gitignored | Workflow uses `ANDROID_HOME`/`ANDROID_SDK_ROOT` only |
| R19 | NOTE | Strict TDD gate breaks if CI flaky | `--continue` keeps full failure signal |
| R20 | NOTE | Future instrumented tests need device runner | Emulator placeholder (D4) |

## Rollback Plan

Single-PR revert. Deleting `.github/workflows/ci.yml` + `security.yml` removes all CI; `.github/dependabot.yml` is safe to keep (or delete — no state); `docs/branch-protection.md` is documentation only; spec deltas revert with the same commit. No data, no remote state, no schema. Branch protection (if applied manually in repo settings) must be un-checked manually — it is not code-managed.

## Dependencies

- Internal: `bootstrap-android-scaffold` (archived 2026-08-06, complete — wrapper, modules, strict TDD).
- External: GitHub Actions runners on `AndresDev28/SyncAlarm`. **Caveat**: CodeQL is free for public repos; if the repo is private it requires GitHub Advanced Security — verify visibility before merge.

## Success Criteria

- [ ] `ci.yml` runs green on a push to `main`: `./gradlew test --continue` + `:app:assembleDebug` + `:data:assembleDebug`
- [ ] SDK install step precedes every Gradle step in both workflows
- [ ] CodeQL SARIF appears in the Security tab; no `category:` line; `build-mode: manual`
- [ ] Trivy runs warning-only (`exit-code: "0"`, `ignore-unfixed: true`), SARIF uploaded, no PR gating
- [ ] All third-party actions SHA-pinned; Dependabot `github-actions` ecosystem active
- [ ] `docs/branch-protection.md` lists `build-and-test`, `codeql`, `trivy-fs-scan` with rationale
- [ ] Emulator placeholder comment present in `ci.yml`
- [ ] `./gradlew test` still exits 0 locally (zero Kotlin source touched; strict-TDD gate intact)
