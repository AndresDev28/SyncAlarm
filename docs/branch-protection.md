# Branch Protection — SyncAlarm

This document describes the GitHub branch protection contract for
`SyncAlarm`'s `main` branch and the recommended developer workflow around
it. It is human-readable guidance; the actual enforcement happens in
GitHub repository settings (Settings → Branches → Branch protection rules).

> **Status**: v1, established by `bootstrap-android-ci`. Follow-up changes
> will add emulator/lint/coverage gates.

## Rule: `main` requires PRs with green CI

- **`main`** accepts changes only through pull requests with green required
  CI checks.
- Direct pushes to `main` are **rejected** by GitHub branch protection.
- The strict-TDD gate from `openspec/config.yaml` (`./gradlew test`) is
  enforced by CI; a red `build-and-test` check blocks merge.

### Required status checks

The following job names must be green on a PR before `main` can be
merged. Configure these in the repository's branch protection rule for
`main`:

| Check name | Source workflow | Purpose |
|---|---|---|
| `build-and-test` | `CI` | `./gradlew test` + `:app:assembleDebug` + `:data:assembleDebug` |
| `codeql` | `Security` | CodeQL Java/Kotlin analysis |
| `trivy-fs-scan` | `Security` | Trivy filesystem HIGH/CRITICAL scan |

## Branch naming convention

Branches should match one of the allowed prefixes. The prefixes mirror the
project's Conventional Commits types enforced by commitlint:

| Prefix | Use case |
|---|---|
| `feat/*` | New feature |
| `fix/*` | Bug fix |
| `chore/*` | Tooling / maintenance |
| `docs/*` | Documentation only |
| `refactor/*` | Code refactor, no behavior change |
| `test/*` | Test-only changes |
| `perf/*` | Performance improvement |
| `build/*` | Build system / CI |
| `ci/*` | CI / GitHub Actions specifically |

**Examples**: `feat/alarm-permissions-flow`, `fix/notification-crash`,
`ci/android-sdk-bump`.

> **Note**: GitHub does NOT enforce branch naming natively. A follow-up
> change will add a `branch-name-lint` GitHub Action to enforce this.

## Git Worktrees protocol

For concurrent work, parallel reviews, and urgent hotfixes, use Git
Worktrees. This avoids `git stash` overhead and lets each PR live in its
own reviewable directory.

### Recommended directory layout

Place worktrees as siblings of the main checkout:

```
/home/<user>/dev/personal-projects/sync-alarm/
├── SyncAlarm/                  # main worktree (always on `main`)
├── syncalarm-feat-alarms/      # git worktree for `feat/alarms`
├── syncalarm-fix-notif/        # git worktree for `fix/notification-crash`
└── syncalarm-hotfix/           # git worktree for an urgent hotfix
```

### Common patterns

**Context switch for an urgent hotfix**

You are mid-work on `feat/alarms` with uncommitted changes. An urgent
hotfix needs to ship. From the `feat/alarms` worktree:

```bash
git worktree add ../syncalarm-hotfix main
cd ../syncalarm-hotfix
git checkout -b fix/urgent-thing
# ... make the fix, commit, push, open PR, merge ...
cd ../SyncAlarm   # back to your feature work
```

Your in-progress `feat/alarms` work is preserved in the original worktree.

**Parallel reviews**

Each PR review gets its own worktree. Run `./gradlew test` locally for
each PR without contaminating any other worktree's state.

**Concurrent features**

Multiple `feat/*` worktrees side by side. Each runs its own CI pipeline
on push, no interference.

### House rules

- **NEVER** create a worktree inside another worktree. Worktrees must be
  siblings of the main checkout.
- **ALWAYS** keep the `main` worktree clean (no in-progress commits). Use
  a branch worktree for any non-trivial work.
- **ALWAYS** run `./gradlew test` in the worktree that owns the change
  before pushing. The CI gate is the same as your local gate.

### Forward-compat

The Worktrees protocol is a SHOULD, not a MUST. Single-worktree workflows
remain valid for solo developers or short-lived branches. The protocol
becomes a MUST when multiple PRs land in parallel or during incident
response.
