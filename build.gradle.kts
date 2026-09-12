plugins {
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.jetbrains.compose.compiler) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.android.application) apply false
}

subprojects {
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target(
                fileTree(project.projectDir) {
                    include("src/**/*.kt")
                    exclude("src/**/resources/**/*.kt")
                }
            )
            ktfmt()
        }
        kotlinGradle {
            target("build.gradle.kts")
            ktfmt()
        }
        format("xml") {
            target("src/**/*.xml")
        }
    }
}
