package com.alexstyl.tabletlauncher

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class SplitScreenLauncherActivity : ComponentActivity() {
  private var selectedApp: LauncherApp? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    selectedApp = OnyxSplitScreenLauncher.selectedAppFrom(intent)
    enableEdgeToEdge()
    setContent { SplitScreenLauncher(::replaceCurrentPane) }
  }

  override fun onPostResume() {
    super.onPostResume()

    selectedApp?.let { app ->
      selectedApp = null
      OnyxSplitScreenLauncher.startSplit(this, app)
    }
  }

  private fun replaceCurrentPane(app: LauncherApp) {
    startActivity(
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(app.packageName, app.activityName)),
    )
  }
}

@Composable
private fun SplitScreenLauncher(replaceCurrentPane: (LauncherApp) -> Unit) {
  val installedAppsProvider = rememberInstalledAppsProvider()
  var apps by remember { mutableStateOf(emptyList<LauncherApp>()) }

  LaunchedEffect(installedAppsProvider) { apps = installedAppsProvider.installedApps() }

  LauncherAppGrid(
      apps = apps,
      onAppClick = replaceCurrentPane,
      backgroundColor = Color.Black,
      contentColor = Color.White,
      showWallpaper = false,
  )
}
