package com.alexstyl.tabletlauncher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberInstalledAppsProvider(): InstalledAppsProvider = remember {
  EmptyInstalledAppsProvider
}

private data object EmptyInstalledAppsProvider : InstalledAppsProvider {
  override fun installedApps(): List<LauncherApp> = emptyList()

  override fun launch(app: LauncherApp) = Unit
}
