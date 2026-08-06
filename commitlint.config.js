// commitlint.config.js
//
// SyncAlarm conventional-commit enforcement.
// Extends the official @commitlint/config-conventional ruleset; future changes
// can layer custom rules here (e.g. enforcing max subject length, scope
// whitelist, or per-package overrides) without losing the upstream baseline.
//
// Activated by the .husky/commit-msg hook (see PR 4 of bootstrap-android-scaffold).
// Run manually with: npx commitlint --edit <message-file>
//
// Allowed types per the project AGENTS.md: feat, fix, chore, docs, refactor,
// test, perf, build, ci. Allowed scopes: app, data, domain, scaffold, deps.

module.exports = {
    extends: ['@commitlint/config-conventional'],
};
