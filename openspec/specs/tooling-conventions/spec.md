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

### Requirement: CI / GitHub Actions Deferred

GitHub Actions / CI SHALL be deferred to a separate change named `bootstrap-android-ci`. This change SHALL NOT introduce `.github/workflows/`.

#### Scenario: No workflows directory is created

- GIVEN the change is archived
- WHEN the repo root is listed
- THEN `.github/workflows/` SHALL NOT exist
- AND `.github/` either SHALL NOT exist or SHALL contain only `AGENTS.md`-style non-workflow files