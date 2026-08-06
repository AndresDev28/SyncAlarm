package com.syncalarm.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Unit test asserting that `app/src/main/AndroidManifest.xml` meets the
 * `app-shell` spec requirements:
 *
 *   * Five `<uses-permission>` entries (`READ_CALENDAR`, `SCHEDULE_EXACT_ALARM`,
 *     `POST_NOTIFICATIONS`, `INTERNET`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
 *   * `android:name=".SyncAlarmApp"` is registered on `<application>`.
 *   * `MainActivity` declares a `MAIN` / `LAUNCHER` intent filter so the app
 *     has an icon on the device home screen.
 *
 * The test reads the manifest from the module's working directory
 * (`src/main/AndroidManifest.xml`) — Gradle's `Test` task runs from the module
 * root, so the relative path is stable across machines and CI.
 *
 * This is the **RED** step of the TDD cycle for T3.4: the manifest is not yet
 * written, so the test fails on `DocumentBuilderFactory.parse(...)` with a
 * `FileNotFoundException`. The companion production step (writing the manifest)
 * is what makes the test go GREEN.
 *
 * Run with: `./gradlew :app:test`
 */
@DisplayName("AndroidManifest declares the app-shell contract")
class AndroidManifestTest {

    @Test
    fun `manifest declares exactly five required permissions`() {
        val permissions = parseUsesPermissionNames()

        assertAll(
            { assertThat(permissions).contains("android.permission.READ_CALENDAR") },
            { assertThat(permissions).contains("android.permission.SCHEDULE_EXACT_ALARM") },
            { assertThat(permissions).contains("android.permission.POST_NOTIFICATIONS") },
            { assertThat(permissions).contains("android.permission.INTERNET") },
            { assertThat(permissions).contains("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS") },
            { assertThat(permissions).hasSize(5) },
        )
    }

    @Test
    fun `application element registers SyncAlarmApp`() {
        val applicationName = parseApplicationName()

        assertThat(applicationName).isEqualTo(".SyncAlarmApp")
    }

    @Test
    fun `MainActivity is the launcher activity`() {
        val launcherFound = parseLauncherActivity()

        assertThat(launcherFound).isTrue()
    }

    private fun parseUsesPermissionNames(): List<String> {
        val document = parseManifest()
        val nodes = document.getElementsByTagName("uses-permission")
        return (0 until nodes.length).map { index ->
            nodes.item(index).attributes.getNamedItem("android:name").nodeValue
        }
    }

    private fun parseApplicationName(): String? {
        val document = parseManifest()
        val application = document.getElementsByTagName("application").item(0)
            ?: error("<application> element missing from AndroidManifest.xml")
        return application.attributes.getNamedItem("android:name")?.nodeValue
    }

    private fun parseLauncherActivity(): Boolean {
        val document = parseManifest()
        val activities = document.getElementsByTagName("activity")
        for (i in 0 until activities.length) {
            val activity = activities.item(i)
            val intentFilters = activity.childNodes
            for (j in 0 until intentFilters.length) {
                val filter = intentFilters.item(j)
                if (filter.nodeName != "intent-filter") continue
                val hasMain = (0 until filter.childNodes.length).any { k ->
                    val child = filter.childNodes.item(k)
                    child.nodeName == "action" &&
                        child.attributes.getNamedItem("android:name")?.nodeValue ==
                        "android.intent.action.MAIN"
                }
                val hasLauncher = (0 until filter.childNodes.length).any { k ->
                    val child = filter.childNodes.item(k)
                    child.nodeName == "category" &&
                        child.attributes.getNamedItem("android:name")?.nodeValue ==
                        "android.intent.category.LAUNCHER"
                }
                if (hasMain && hasLauncher) return true
            }
        }
        return false
    }

    private fun parseManifest(): org.w3c.dom.Document {
        val manifestFile = java.io.File("src/main/AndroidManifest.xml")
        check(manifestFile.exists()) {
            "AndroidManifest.xml not found at ${manifestFile.absolutePath} — " +
                "module working directory is expected to be the :app module root."
        }
        val factory = DocumentBuilderFactory.newInstance().apply {
            // Required to read attributes with the `android:` namespace prefix.
            isNamespaceAware = true
        }
        return factory.newDocumentBuilder().parse(manifestFile)
    }
}
