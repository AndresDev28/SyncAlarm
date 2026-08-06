# SyncAlarm

Sync native Android calendar events → device alarms. Pure Kotlin, Clean Architecture
+ MVVM, strict TDD. (Full product context lives in the PRD; the working PRDs
land in `openspec/context/sync-alarm.md` once sdd-explore runs.)

## Build & Test

The wrapper is pinned to **Gradle 8.10.2** and the build requires **JDK 17**.
Android SDK paths are read from `local.properties` (see `local.properties.example`
for the schema).

```bash
# Build the debug APK
./gradlew :app:assembleDebug

# Run all unit tests (JUnit 5 + MockK + Turbine + AssertJ)
./gradlew test
```

> **First build note**: the cold-cache build of `:app` runs Hilt's KSP processor
> and the Compose Compiler plugin, which together take ~30s end-to-end on a
> fresh clone. Subsequent builds are sub-second when no input changes.

## Commit Convention

Commits are validated by **commitlint + Husky** on every `git commit`. The
hook is installed automatically the first time you run `npm install` in this
repo (the `prepare` script in `package.json` wires Husky). To install it
manually:

```bash
npm install                # one-time; installs commitlint + Husky locally
npm run prepare            # or: npx husky install
```

Commit messages MUST follow the Conventional Commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Allowed types: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, `perf`,
`build`, `ci`. Common scopes for this project: `app`, `data`, `domain`,
`scaffold`, `deps`, `tooling`.

The full ruleset lives in [`commitlint.config.js`](./commitlint.config.js);
the hook script lives in [`.husky/commit-msg`](./.husky/commit-msg). To
bypass the hook for a one-off (e.g. a `git revert` of a non-conventional
historical commit), pass `--no-verify`.

## Editor & Whitespace Baseline

A baseline [`.editorconfig`](./.editorconfig) enforces 4-space indent for
Kotlin / Kotlin-DSL / Java, LF line endings, UTF-8, trim trailing whitespace,
and a final newline. Most editors pick this up automatically; if not,
install the EditorConfig plugin for your editor of choice.

## Modules

| Module | Type | Purpose |
|--------|------|---------|
| `:app` | `com.android.application` | Host application — Hilt `Application`, Compose `MainActivity`, the APK the user installs. |
| `:data` | `com.android.library` | Data layer (Room, DataStore, Google Calendar adapters). Stub today; real sources land in follow-up changes. |
| `:domain` | `org.jetbrains.kotlin.jvm` | Pure-Kotlin business core. **Zero** `android.*` imports. TDD-friendly. |

The dependency direction is `app → domain ← data`, `app → data`. `app` and
`data` apply Hilt via KSP; `domain` is framework-free.

## Next Steps

* **PR 4 of `bootstrap-android-scaffold`**: commitlint + Husky + `.editorconfig`
  + the README extension with the canonical commit message format.
* **Follow-up changes**: `add-room-persistence`, `add-oauth-google-calendar`,
  `add-alarm-scheduler`, `add-workmanager-calendar-scan`, `add-rule-engine`.
* **CI / GitHub Actions**: deferred to `bootstrap-android-ci`.

## Forward-Compat Notes

* `minSdk = 26` does **NOT** unlock `SCHEDULE_EXACT_ALARM` (API 31) or
  `USE_EXACT_ALARM` (API 33). The runtime flow for those permissions lands
  in a future change named `alarm-permission-flow`.
