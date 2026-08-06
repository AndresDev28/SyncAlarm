# tooling-conventions — MODIFIED Requirements

`Conventional Commits Enforced via commitlint` remains unchanged: commitlint + Husky continue to reject invalid messages.

## ADDED Requirements

### Requirement: Branch Naming Convention

<!-- trace: synthetic-NFR-02-pending: contributor workflow consistency and ergonomics; aligned with existing Conventional Commits convention. -->
Contributors SHOULD create branches whose names MUST match `feat/*`, `fix/*`, `chore/*`, `docs/*`, `refactor/*`, `test/*`, `perf/*`, `build/*`, or `ci/*`. The prefixes mirror the repository's Conventional Commits types.

#### Scenario: Developer creates a feature branch
- **GIVEN** a contributor starts feature work
- **WHEN** the branch is created
- **THEN** its name SHOULD use `feat/<descriptive-name>`

#### Scenario: Protection validates branch naming
- **GIVEN** a pull request targets protected `main`
- **WHEN** its source branch does not match an allowed pattern
- **THEN** repository policy SHOULD reject or flag the pull request

#### Scenario: Branch and commit taxonomy align
- **GIVEN** a branch uses an allowed prefix
- **WHEN** its commits are reviewed
- **THEN** the prefix SHOULD correspond to an allowed Conventional Commit type

### Requirement: Git Worktrees for Concurrent Work and Hotfixes

<!-- trace: synthetic-NFR-02-pending; project convention motivated by AGENTS.md Out-of-Scope isolation and bootstrap-android-ci intent. -->
The repository SHOULD document a Git Worktrees protocol for concurrent tasks, reviews, and urgent hotfixes. Additional worktrees SHOULD use `../syncalarm-<branch>`.

#### Scenario: Context-switch without losing feature work
- **GIVEN** a developer has uncommitted work on a `feat/*` branch
- **WHEN** an urgent hotfix requires a context switch
- **THEN** they SHOULD create `../syncalarm-<branch>` and check out `main` there
- **AND** the original worktree SHALL preserve the in-progress changes

#### Scenario: Hotfix and feature proceed in parallel
- **GIVEN** feature work is in progress
- **WHEN** a hotfix is developed in another worktree
- **THEN** both branches SHOULD remain editable without `git stash` overhead

#### Scenario: Parallel reviews remain independent
- **GIVEN** multiple features each use a worktree
- **WHEN** their pull requests are reviewed
- **THEN** review and CI SHOULD run independently for each branch

### Requirement: Main Branch Requires Pull Requests and Green CI

<!-- trace: PRD §6 Clean Architecture/TDD; synthetic-NFR-01-pending. Cross-reference: build-system/Main Branch Requires Green CI. -->
The `main` branch SHALL accept changes only through pull requests with green required CI checks. Branch protection SHALL block direct pushes.

#### Scenario: Direct push is blocked
- **GIVEN** branch protection applies to `main`
- **WHEN** a contributor pushes directly to `main`
- **THEN** GitHub SHALL reject the push

#### Scenario: Failed CI blocks merge
- **GIVEN** a pull request targets `main`
- **WHEN** `build-and-test` or another required check is not green
- **THEN** GitHub SHALL block merge

## MODIFIED Requirements

### Requirement: CI / GitHub Actions Deferred

<!-- trace: PRD §6 Clean Architecture/TDD; synthetic-NFR-01-pending. -->
GitHub Actions / CI SHALL be delivered by `bootstrap-android-ci`; `.github/workflows/` SHALL contain the build and security workflows.

(Previously: CI and `.github/workflows/` were explicitly deferred and prohibited.)

#### Scenario: Workflows directory is created
- **GIVEN** `bootstrap-android-ci` is applied
- **WHEN** the repository root is inspected
- **THEN** `.github/workflows/ci.yml` SHALL exist
- **AND** `.github/workflows/security.yml` SHALL exist

> **NOTE:** Trivy initially remains warning-only with `exit-code: "0"` and `ignore-unfixed: true`; `tighten-trivy-gate` will introduce enforcement after findings are triaged.
