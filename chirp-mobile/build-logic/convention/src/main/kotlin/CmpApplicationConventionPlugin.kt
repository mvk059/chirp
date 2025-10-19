import fyi.manpreet.chirp.convention.applyHierarchyTemplate
import fyi.manpreet.chirp.convention.configureAndroidTarget
import fyi.manpreet.chirp.convention.configureDesktopTarget
import fyi.manpreet.chirp.convention.configureIosTargets
import fyi.manpreet.chirp.convention.configureWasmTarget
import fyi.manpreet.chirp.convention.configureJsTarget
import fyi.manpreet.chirp.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CmpApplicationConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("fyi.manpreet.convention.android.application.compose")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureAndroidTarget()
            configureIosTargets()
            configureDesktopTarget()
            configureWasmTarget()
            configureJsTarget()

            extensions.configure<KotlinMultiplatformExtension> {
                applyHierarchyTemplate()
            }

            dependencies {
                "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}