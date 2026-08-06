package com.syncalarm.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

/**
 * JVM unit tests asserting PR 4 (`scaffold-tooling`) of `bootstrap-android-scaffold`
 * satisfies the `tooling-conventions` spec.
 *
 * Each test reads a structural file from the repo root (relative to `:app`'s
 * working directory: `../<file>`) and asserts the spec contract. The tests are
 * the RED step of the TDD cycle for the following tasks:
 *
 *   - T4.1: `commitlint.config.js` extends `@commitlint/config-conventional`.
 *   - T4.2: `package.json` declares commitlint + husky devDependencies.
 *   - T4.3: `.husky/commit-msg` invokes commitlint.
 *   - T4.4: `.editorconfig` has the Kotlin/Java baseline.
 *   - T4.5: `README.md` mentions commitlint setup.
 *   - T4.6: `README.md` mentions `bootstrap-android-ci` deferral.
 *
 * The companion scenario "Non-conventional / Conventional message is rejected /
 * accepted by the hook" requires actually installing the hook and running
 * commitlint — that gate is end-to-end and lives in `sdd-verify`, not here.
 *
 * Run with: `./gradlew :app:testDebugUnitTest --tests "*ToolingConventionsTest*"`
 */
@DisplayName("Tooling conventions (PR 4) match the tooling-conventions spec")
class ToolingConventionsTest {

    @Nested
    @DisplayName("commitlint.config.js extends the conventional ruleset")
    inner class CommitlintConfig {

        @Test
        fun `commitlint config extends at-commitlint config-conventional`() {
            val config = readCommitlintConfig()

            // The spec scenario "commitlint config extends conventional ruleset"
            // requires the top-level `extends` field to include the conventional
            // ruleset. We assert the literal substring because the config is JS,
            // not JSON, and a full parse would couple to a JS engine.
            assertThat(config)
                .describedAs(
                    "commitlint.config.js must extend '@commitlint/config-conventional'",
                )
                .contains("@commitlint/config-conventional")
        }

        private fun readCommitlintConfig(): String {
            val file = File("../commitlint.config.js")
            check(file.exists()) {
                "commitlint.config.js not found at ${file.absolutePath}"
            }
            return file.readText()
        }
    }

    @Nested
    @DisplayName("package.json declares commitlint + husky devDependencies")
    inner class PackageJson {

        @Test
        fun `package json declares commitlint cli and config-conventional`() {
            val pkg = readPackageJson()

            assertThat(pkg)
                .describedAs(
                    "package.json must declare '@commitlint/cli' devDependency",
                )
                .contains("\"@commitlint/cli\"")
            assertThat(pkg)
                .describedAs(
                    "package.json must declare '@commitlint/config-conventional'" +
                        " devDependency",
                )
                .contains("\"@commitlint/config-conventional\"")
        }

        @Test
        fun `package json declares husky devDependency`() {
            val pkg = readPackageJson()

            assertThat(pkg)
                .describedAs("package.json must declare 'husky' devDependency")
                .contains("\"husky\"")
        }

        private fun readPackageJson(): String {
            val file = File("../package.json")
            check(file.exists()) {
                "package.json not found at ${file.absolutePath}"
            }
            return file.readText()
        }
    }

    @Nested
    @DisplayName(".husky/commit-msg invokes commitlint on the message")
    inner class HuskyCommitMsg {

        @Test
        fun `commit-msg hook runs commitlint --edit with the message file`() {
            val hook = readHuskyCommitMsg()

            assertThat(hook)
                .describedAs(
                    ".husky/commit-msg must invoke commitlint via npx " +
                        "and pass --edit with the message file argument",
                )
                .contains("commitlint")
                .contains("--edit")
        }

        private fun readHuskyCommitMsg(): String {
            val file = File("../.husky/commit-msg")
            check(file.exists()) {
                ".husky/commit-msg not found at ${file.absolutePath}"
            }
            return file.readText()
        }
    }

    @Nested
    @DisplayName(".editorconfig has the Kotlin/Java baseline")
    inner class EditorConfig {

        @Test
        fun `editorconfig kt kts java section declares all six baseline properties`() {
            val editorconfig = readEditorConfig()

            // The spec scenario "Required editorconfig properties are present"
            // requires these six properties in the `[*.{kt,kts,java}]` section.
            // We assert each property individually so a regression on any single
            // property is reported with a clear failure message.
            val ktSection = extractKtKtsJavaSection(editorconfig)

            assertThat(ktSection)
                .describedAs("editorconfig must declare indent_style = space")
                .contains("indent_style = space")
            assertThat(ktSection)
                .describedAs("editorconfig must declare indent_size = 4")
                .contains("indent_size = 4")
            assertThat(ktSection)
                .describedAs("editorconfig must declare end_of_line = lf")
                .contains("end_of_line = lf")
            assertThat(ktSection)
                .describedAs("editorconfig must declare charset = utf-8")
                .contains("charset = utf-8")
            assertThat(ktSection)
                .describedAs(
                    "editorconfig must declare trim_trailing_whitespace = true",
                )
                .contains("trim_trailing_whitespace = true")
            assertThat(ktSection)
                .describedAs(
                    "editorconfig must declare insert_final_newline = true",
                )
                .contains("insert_final_newline = true")
        }

        private fun readEditorConfig(): String {
            val file = File("../.editorconfig")
            check(file.exists()) {
                ".editorconfig not found at ${file.absolutePath}"
            }
            return file.readText()
        }

        /**
         * Extracts the `[*.{kt,kts,java}]` section body from the editorconfig.
         * The section begins at the matching header and ends at the next `[`
         * header (or end-of-file). Returns the section including the header
         * for readable assertion failure messages.
         */
        private fun extractKtKtsJavaSection(text: String): String {
            val headerRegex = Regex("""\[\*\.\{kt,kts,java\}\]""")
            val headerMatch = headerRegex.find(text)
                ?: error(
                    ".editorconfig is missing the [*.{kt,kts,java}] section",
                )
            val tail = text.substring(headerMatch.range.last + 1)
            val nextHeaderIdx = tail.indexOf('[')
            return if (nextHeaderIdx == -1) {
                headerMatch.value + tail
            } else {
                headerMatch.value + tail.substring(0, nextHeaderIdx)
            }
        }
    }

    @Nested
    @DisplayName("README.md names both runner commands and the commitlint setup")
    inner class ReadmeQuickStart {

        @Test
        fun `README quick-start mentions assembleDebug and test commands`() {
            val readme = readReadme()

            // The spec scenario "README names both runner commands" requires
            // both `./gradlew :app:assembleDebug` AND `./gradlew test` to appear
            // in the file (PR 3 already added these; PR 4 protects the contract).
            assertThat(readme)
                .describedAs(
                    "README must include './gradlew :app:assembleDebug'",
                )
                .contains("./gradlew :app:assembleDebug")
            assertThat(readme)
                .describedAs("README must include './gradlew test'")
                .contains("./gradlew test")
        }

        @Test
        fun `README mentions the commitlint and Husky tooling setup`() {
            val readme = readReadme()

            // T4.5 — extend README with the commitlint setup note. The exact
            // wording is the design's call, but the README must reference both
            // `commitlint` and `Husky` so contributors know the hook is enforced.
            assertThat(readme)
                .describedAs("README must reference 'commitlint'")
                .contains("commitlint")
            assertThat(readme)
                .describedAs("README must reference 'Husky'")
                .contains("Husky")
        }

        @Test
        fun `README defers CI to bootstrap-android-ci follow-up change`() {
            val readme = readReadme()

            // T4.6 — note in README that CI is deferred to a follow-up change
            // named `bootstrap-android-ci`. The spec scenario "CI / GitHub
            // Actions Deferred" requires that `.github/workflows/` is NOT
            // created, and the README must state the deferral explicitly so
            // contributors know not to introduce workflows here.
            assertThat(readme)
                .describedAs(
                    "README must reference 'bootstrap-android-ci' as the" +
                        " follow-up change for CI",
                )
                .contains("bootstrap-android-ci")
        }

        private fun readReadme(): String {
            val file = File("../README.md")
            check(file.exists()) {
                "README.md not found at ${file.absolutePath}"
            }
            return file.readText()
        }
    }

    @Nested
    @DisplayName("No .github/workflows directory is created (CI deferred)")
    inner class NoWorkflowsDirectory {

        @Test
        fun `github workflows directory does not exist at repo root`() {
            // The spec scenario "No workflows directory is created" is the
            // negative-contract gate for "CI / GitHub Actions Deferred". If
            // this test ever fails, a future change accidentally shipped CI
            // scaffolding inside this scaffold change — that's a spec drift.
            val workflows = File("../.github/workflows")
            assertThat(workflows.exists())
                .describedAs(
                    ".github/workflows/ must NOT exist; CI is deferred to" +
                        " the follow-up change 'bootstrap-android-ci'",
                )
                .isFalse()
        }
    }
}
