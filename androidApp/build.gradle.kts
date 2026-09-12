import com.android.build.api.dsl.ApplicationExtension

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.jetbrains.compose.compiler)
}

extensions.configure<ApplicationExtension> {
  namespace = "com.alexstyl.tabletlauncher"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.alexstyl.tabletlauncher"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  buildFeatures { compose = true }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  buildTypes { getByName("release") { isMinifyEnabled = false } }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(compose.material3)
  implementation(libs.composables.icons.lucide)
  implementation(libs.composables.ui)
}
