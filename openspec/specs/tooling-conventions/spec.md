# tooling-conventions Specification

## Purpose

Project-level contributor conventions — Conventional Commits enforced via commitlint + Husky, an `.editorconfig` baseline, a multi-agent `AGENTS.md`, and a `README.md` quick-start. Establishes the contributor contract for every future change in the repository.

## Requirements

### Requirement: Conventional Commits Enforced via commitlint

`commitlint.config.js` SHALL extend `@commitlint/config-conventional`. A git `commit-msg` hook SHALL invoke commitlint and reject non-conventional commit messages.

#### Scenario: commitlint config extends conventional ruleset

- GIVEN `commitlint.config.js` is read
- WHEN the top-level `extends` field is inspected
- THEN it SHALL include `"@commitlint/config-conventional"`

#### Scenario: Non-conventional message is rejected by the hook

- GIVEN a working copy with the hook installed
- WHEN the user runs `git commit -m "wip stuff"`
- THEN commitlint SHALL exit non-zero
- AND the commit SHALL be aborted with a message naming the failing rule

#### Scenario: Conventional message is accepted by the hook

- GIVEN the hook is installed
- WHEN the user runs `git commit -m "feat(app-shell): wire Hilt Application"`
- THEN commitlint SHALL exit 0
- AND the commit SHALL proceed

### Requirement: `.editorconfig` Baseline

`.editorconfig` SHALL enforce a consistent baseline: 4-space indent for Kotlin/Java, LF line endings, UTF-8 encoding, trim trailing whitespace, and a final newline.

#### Scenario: Required editorconfig properties are present

- GIVEN `.editorconfig` is read
- WHEN the `[*.{kt,kts,java}]` glob section is inspected
- THEN `indent_style = space`, `indent_size = 4`, `end_of_line = lf`, `charset = utf-8`, `trim_trailing_whitespace = true`, and `insert_final_newline = true` SHALL each be present

### Requirement: `README.md` Quick-Start

`README.md` SHALL include a quick-start section listing the build and test commands.

#### Scenario: README names both runner commands

- GIVEN `README.md` is read
- WHEN the file is searched
- THEN `./gradlew :app:assembleDebug` SHALL appear in a build/test section
- AND `./gradlew test` SHALL appear in the same section

### Requirement: `AGENTS.md` Project Guidance

`AGENTS.md` SHALL exist at the repo root as project-level AI-agent guidance.

#### Scenario: AGENTS.md exists

- GIVEN the repo root is listed
- WHEN `AGENTS.md` is checked
- THEN the file SHALL exist and SHALL be non-empty

### Requirement: CI / GitHub Actions Delivered

GitHub Actions / CI SHALL be delivered by `bootstrap-android-ci`. `.github/workflows/` SHALL contain the build (`.github/workflows/ci.yml`) and security (`.github/workflows/security.yml`) workflows. Trivy SHALL initially run in warning-only mode; a follow-up change `tighten-trivy-gate` SHALL tighten the gate after the first scan is triaged.

#### Scenario: Workflows directory is created

- GIVEN `bootstrap-android-ci` is archived
- WHEN the repo root is listed
- THEN `.github/workflows/ci.yml` SHALL exist
- AND `.github/workflows/security.yml` SHALL exist

> **NOTE**: Trivy initially runs warning-only with `exit-code: "0"` and `ignore-unfixed: true`; the follow-up `tighten-trivy-gate` change will harden the gate after triage.

### Requirement: Branch Naming Convention

Contributors SHALL create branches whose names match one of the allowed prefixes: `feat/*`, `fix/*`, `chore/*`, `docs/*`, `refactor/*`, `test/*`, `perf/*`, `build/*`, or `ci/*`. The prefixes mirror the project's Conventional Commits types enforced by commitlint.

#### Scenario: Developer creates a feature branch

- GIVEN a contributor starts feature work
- WHEN the branch is created
- THEN its name SHALL use `feat/<descriptive-name>` (or one of the other allowed prefixes)

#### Scenario: Branch and commit taxonomy align

- GIVEN a branch uses an allowed prefix
- WHEN its commits are reviewed
- THEN the prefix SHALL correspond to an allowed Conventional Commit type

### Requirement: Git Worktrees for Concurrent Work and Hotfixes

The repository SHALL document a Git Worktrees protocol for concurrent tasks, reviews, and urgent hotfixes. Additional worktrees SHALL use `../syncalarm-<branch>`. The protocol is documented in `docs/branch-protection.md`.

#### Scenario: Context-switch without losing feature work

- GIVEN a developer has uncommitted work on a `feat/*` branch
- WHEN an urgent hotfix requires a context switch
- THEN they SHALL create `../syncalarm-<branch>` and check out `main` there
- AND the original worktree SHALL preserve the in-progress changes

#### Scenario: Hotfix and feature proceed in parallel

- GIVEN feature work is in progress
- WHEN a hotfix is developed in another worktree
- THEN both branches SHALL remain editable without `git stash` overhead

#### Scenario: Parallel reviews remain independent

- GIVEN multiple features each use a worktree
- WHEN their pull requests are reviewed
- THEN review and CI SHALL run independently for each branch

### Requirement: Main Branch Requires Pull Requests and Green CI

The `main` branch SHALL accept changes only through pull requests with green required CI checks. Branch protection SHALL block direct pushes. The required status checks are the display names of the three jobs: `Build & Unit Tests` (from `ci.yml`), `CodeQL (Kotlin/Java)` (from `security.yml`), and `Trivy Filesystem Scan` (from `security.yml`).

#### Scenario: Direct push is blocked

- GIVEN branch protection applies to `main`
- WHEN a contributor pushes directly to `main`
- THEN GitHub SHALL reject the push

#### Scenario: Failed CI blocks merge

- GIVEN a pull request targets `main`
- WHEN `Build & Unit Tests`, `CodeQL (Kotlin/Java)`, or `Trivy Filesystem Scan` is not green
- THEN GitHub SHALL block merge