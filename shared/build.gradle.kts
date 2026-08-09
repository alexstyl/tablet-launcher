import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.jetbrains.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.jetbrains.compose.compiler)
  alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
  android {
    namespace = "com.alexstyl.tabletlauncher.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    withJava()
    androidResources { enable = true }
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
  }

  jvm()

  sourceSets {
    commonMain.dependencies {
      implementation(compose.material3)
      implementation(libs.compose.ui.tooling.preview)
      implementation(libs.androidx.navigation3.ui)
      implementation(libs.composables.icons.lucide)
      implementation(libs.composables.uri.painter)
      implementation(libs.composables.ui)
    }
  }
}

dependencies { androidRuntimeClasspath(libs.compose.ui.tooling) }
