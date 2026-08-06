// SyncAlarm root settings — bootstrap-android-scaffold, PR 1 (scaffold-build).
// Plain-text placeholder from T1.1 is replaced here with the real settings:
//
//   * pluginManagement.repositories → where plugins (AGP, KSP, Hilt, Compose Compiler) resolve.
//   * dependencyResolutionManagement.repositories → where project dependencies resolve.
//   * Version catalog → `gradle/libs.versions.toml` is auto-discovered by Gradle and
//     exposed as `libs` in every module's build script; no explicit `from(...)` call
//     is needed (declaring it explicitly duplicates the auto-binding and breaks the build).

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SyncAlarm"

// Module includes — exactly three modules per Clean Architecture (app → data → domain).
// Matches `module-boundaries` spec scenario "Settings file lists all three modules".
include(":app", ":domain", ":data")