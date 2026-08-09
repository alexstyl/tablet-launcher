package com.alexstyl.tabletlauncher

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

data class LauncherApp(
    val packageName: String,
    val activityName: String,
    val name: String,
    val icon: ImageBitmap?,
    val launchAdjacent: Boolean = true,
)

interface InstalledAppsProvider {
  fun installedApps(): List<LauncherApp>

  fun launch(app: LauncherApp)

  fun launchAdjacent(app: LauncherApp)
}

@Composable expect fun rememberInstalledAppsProvider(): InstalledAppsProvider
