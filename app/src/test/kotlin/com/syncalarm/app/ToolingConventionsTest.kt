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
         * The header must appear at the start of a line (not inside a comment
         * that merely references the section name); the body ends at the next
         * `[` header or end-of-file.
         */
        private fun extractKtKtsJavaSection(text: String): String {
            // Anchor on a line start (`^` with MULTILINE) so the regex does not
            // match a reference to the section inside a `# comment`.
            val headerRegex = Regex(
                """(?m)^\[\*\.\{kt,kts,java\}\][\t ]*$""",
            )
            val headerMatch = headerRegex.find(text)
                ?: error(
                    ".editorconfig is missing the [*.{kt,kts,java}] section",
                )
            val tailStart = headerMatch.range.last + 1
            val tail = text.substring(tailStart)
            val nextHeaderIdx = tail.indexOf("\n[")
            return if (nextHeaderIdx == -1) {
                headerMatch.value + tail
            } else {
                val sectionEnd = nextHeaderIdx + 1
                text.substring(headerMatch.range.first, tailStart + sectionEnd)
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
        fun `README documents CI delivered by bootstrap-android-ci`() {
            val readme = readReadme()

            // T4.6 — note in README that CI was delivered by
            // `bootstrap-android-ci`. As of that change the spec scenario
            // "CI / GitHub Actions Delivered" requires `.github/workflows/`
            // to exist, and the README must state the delivery explicitly so
            // contributors know the CI infrastructure is in place.
            assertThat(readme)
                .describedAs(
                    "README must reference 'bootstrap-android-ci' as the" +
                        " change that delivered CI",
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
    @DisplayName(".github/workflows directory is bootstrapped (CI delivered)")
    inner class WorkflowsDirectoryExists {

        @Test
        fun `github workflows directory exists at repo root`() {
            // The spec scenario "Workflows directory is created" is the
            // positive-contract gate for "CI / GitHub Actions Delivered" (the
            // MODIFIED version of the previous "CI / GitHub Actions Deferred"
            // requirement). If this test ever fails, a future change
            // accidentally removed the CI scaffolding — that's a spec drift.
            val workflowsDir = File("../.github/workflows")
            assertThat(workflowsDir.exists())
                .describedAs(
                    ".github/workflows/ MUST exist; CI is delivered by" +
                        " the change 'bootstrap-android-ci'",
                )
                .isTrue()
        }

        @Test
        fun `expected workflow files exist`() {
            // Companion test: the spec requires `.github/workflows/` to
            // contain both the build and the security workflows.
            val workflowsDir = File("../.github/workflows")
            assertThat(workflowsDir.isDirectory)
                .describedAs(".github/workflows/ MUST be a directory")
                .isTrue()
            assertThat(File(workflowsDir, "ci.yml").exists())
                .describedAs("ci.yml MUST exist per build-system spec")
                .isTrue()
            assertThat(File(workflowsDir, "security.yml").exists())
                .describedAs("security.yml MUST exist per build-system spec")
                .isTrue()
        }
    }
}
