package com.alexstyl.tabletlauncher

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

data class LauncherApp(
    val packageName: String,
    val activityName: String,
    val name: String,
    val icon: ImageBitmap?,
)

interface InstalledAppsProvider {
  fun installedApps(): List<LauncherApp>
}

@Composable expect fun rememberInstalledAppsProvider(): InstalledAppsProvider
