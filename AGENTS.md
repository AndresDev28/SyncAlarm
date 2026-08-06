# AGENTS.md — SyncAlarm

> Project-level guidance for AI coding agents (and humans) working in this
> repository. Reads **on top of** the multi-agent rules in
> `~/.config/opencode/AGENTS.md` (which govern communication style, persona,
> memory protocol, and the no-Co-Authored-By commit rule). This file
> constrains **what** you build, not **how** you communicate.

## Stack & Architecture

SyncAlarm is a native Android app written in Kotlin. The architecture is
**Clean Architecture + MVVM** as established in the PRD (§6 NFRs):

```
:app  (com.android.application — Hilt + Compose entry point)
 │
 ├──► :domain  (org.jetbrains.kotlin.jvm — pure Kotlin, zero android.*)
 │
 └──► :data    (com.android.library — Room, DataStore, Calendar adapters)
        │
        └──► :domain
```

**Hard rule**: `:domain` is a `kotlin("jvm")` module. It MUST NOT import
`android.*`. This is enforced by the build, not by convention — adding
`com.android.library` to `domain/build.gradle.kts` breaks the build.

## Workflow

This project uses **Spec-Driven Development (SDD)**. The SDD artifacts live
under `openspec/`:

```
openspec/
  config.yaml               # TDD gate, runner config, rules
  changes/<change-name>/    # one folder per in-flight change
    proposal.md             # WHY the change exists
    spec.md(s)              # WHAT it must do (Given/When/Then)
    design.md               # HOW it must be structured
    tasks.md                # one checkable item per work unit
  specs/<capability>/       # NEW + MODIFIED capability spec folders
  context/sync-alarm.md     # the working PRD
```

**Strict TDD is enabled.** `openspec/config.yaml` `testing.strict_tdd: true`
is the gate. Every `sdd-apply` run follows the RED → GREEN → TRIANGULATE →
REFACTOR cycle from the `sdd-apply/strict-tdd.md` module. Tests are written
**before** production code, always.

## Module Ownership

| Module | Owner concern | Allowed Android imports |
|--------|---------------|--------------------------|
| `:app` | UI, ViewModels, navigation, the Hilt singleton component | Yes (full Android) |
| `:data` | Repos, network/calendar adapters, persistence | Yes (Android library) |
| `:domain` | Use cases, entities, rule engine, value classes | **No**. Zero `android.*`. |

## Code Patterns

* **Kotlin**: idiomatic, no premature coroutines. `Flow` for streams; `suspend`
  for one-shot. Run blocking work on `Dispatchers.IO`.
* **DI**: Hilt only. KSP for the compiler. **No KAPT** — it is broken on
  Kotlin 2.0+.
* **Compose**: function components, state hoisting, `MaterialTheme` (Material 3,
  not Material 2). `androidx.compose.material3.*` only.
* **Testing**: JUnit 5 + MockK + Turbine + AssertJ in `:domain` and `:data`.
  Compose UI test rule + Hilt Android test rule in `:app` (`androidTest`).

## Commit Conventions

Conventional Commits, enforced by commitlint + Husky (PR 4 of
`bootstrap-android-scaffold`). Format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Allowed types: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, `perf`,
`build`, `ci`. Common scopes for this project: `app`, `data`, `domain`,
`scaffold`, `deps`.

**Do NOT add `Co-Authored-By` or AI attribution footers.** The default
multi-agent rules already capture that constraint.

## Out of Scope (for this scaffold)

* Real persistence (Room, DataStore) — `add-room-persistence`
* OAuth2 PKCE, Google Calendar API — `add-oauth-google-calendar`
* AlarmManager scheduling — `add-alarm-scheduler`
* WorkManager periodic calendar scans — `add-workmanager-calendar-scan`
* Rule engine (offset, conflict resolution) — `add-rule-engine`
* CI / GitHub Actions — `bootstrap-android-ci`

These are tracked in `openspec/changes/` and the in-flight `tasks.md` files.
Do not start them implicitly; each lands as its own SDD change with its own
proposal/spec/design/tasks artifacts.
