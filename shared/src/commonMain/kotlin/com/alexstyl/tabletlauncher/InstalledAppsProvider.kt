package com.alexstyl.tabletlauncher

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

data class LauncherApp(
    val packageName: String,
    val activityName: String,
    val name: String,
    val icon: ImageBitmap?,
)

data class LauncherAppKey(
    val packageName: String,
    val activityName: String,
)

val LauncherApp.key: LauncherAppKey
  get() = LauncherAppKey(packageName = packageName, activityName = activityName)

data class LauncherFolder(
    val id: String,
    val name: String,
    val appKeys: Set<LauncherAppKey>,
)

interface InstalledAppsProvider {
  fun installedApps(): List<LauncherApp>
}

@Composable expect fun rememberInstalledAppsProvider(): InstalledAppsProvider
