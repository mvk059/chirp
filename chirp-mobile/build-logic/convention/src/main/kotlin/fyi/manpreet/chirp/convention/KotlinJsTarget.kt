package fyi.manpreet.chirp.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureJsTarget() {
    extensions.configure<KotlinMultiplatformExtension> {
        js {
            browser()
            binaries.executable()
        }
    }
}
