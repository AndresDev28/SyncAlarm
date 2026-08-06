# add-alarm-permissions — Proposal Stub

> **Status**: stub — created to formally track the forward-compat follow-up named in
> `bootstrap-android-scaffold`. Full proposal/spec/design/tasks artifacts land when this
> change is taken off the backlog.

## Why

`bootstrap-android-scaffold` declared `SCHEDULE_EXACT_ALARM` (API 31) and `USE_EXACT_ALARM`
(API 33) in `app/src/main/AndroidManifest.xml` but did not wire the runtime permission
flow. minSdk 26 does NOT unlock these permissions — they require:

- `SCHEDULE_EXACT_ALARM` permission at API 31 (Android 12) — `canScheduleExactAlarms()` +
  `requestPermissions(...)`
- `USE_EXACT_ALARM` permission at API 33 (Android 13) — granted at install for apps in
  the "Alarms & reminders" category; no runtime request needed
- Plus a conditional `Build.VERSION.SDK_INT >= 31` request flow before scheduling exact
  alarms; a denial path that either falls back to `setAndAllowWhileIdle` or deep-links to
  system Settings → "Alarms & reminders"

The `app-shell` spec scenario "Forward-compat — exact-alarm runtime flow is deferred"
points to this change. This stub makes the follow-up first-class in the OpenSpec artifact
store so downstream changes can reference it.

## What changes (preview)

- `MainActivity.kt` (or a new `AlarmPermissionActivity`) calls
  `requestPermissions(SCHEDULE_EXACT_ALARM)` on API 31+ when the user first tries to
  schedule an exact alarm
- `AlarmManager.canScheduleExactAlarms()` is checked before each `setExactAndAllowWhileIdle`
  / `setAlarmClock` call
- UI flow: detect → request → handle denial with a fallback to `setAndAllowWhileIdle` or
  a settings deep-link to "Alarms & reminders"
- Tests:
  - JVM unit test for the conditional logic (the `Build.VERSION.SDK_INT` branch)
  - Instrumented test for the actual permission grant flow (deferred to `bootstrap-android-ci`
    device runner — same emulator-gating rationale as the Compose smoke test)

## What does NOT change

- minSdk 26 / targetSdk 35 (unchanged from `bootstrap-android-scaffold`)
- The 5 `<uses-permission>` declarations in `app/src/main/AndroidManifest.xml` (already declared)
- `:domain` and `:data` modules (no permission logic leaks into the TDD-pure layer)

## Forward-compat references

- `openspec/specs/app-shell/spec.md` scenario "Forward-compat — exact-alarm runtime flow is deferred"
- `openspec/changes/bootstrap-android-scaffold/design.md` — forward-compat constraint section
- `openspec/changes/bootstrap-android-scaffold/verify-report.md` — W1 finding
- PRD §4 RF-04 (alarm scheduling)

## Out of scope for THIS stub

Full proposal/spec/design/tasks artifacts for `add-alarm-permissions` are deferred until
this change is taken off the backlog (likely after `bootstrap-android-ci` lands the
device runner that the instrumented tests need).
