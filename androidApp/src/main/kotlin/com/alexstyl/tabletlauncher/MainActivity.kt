package com.alexstyl.tabletlauncher

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
  private var hasRequestedHomeRole = false

  private val requestHomeRole =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent { HomeLauncher(::launchApp, ::launchAppInSplitScreen) }
  }

  override fun onResume() {
    super.onResume()

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || hasRequestedHomeRole) {
      return
    }

    val roleManager = getSystemService(RoleManager::class.java)
    if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
        !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
      hasRequestedHomeRole = true
      requestHomeRole.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
    }
  }

  private fun launchApp(app: LauncherApp) {
    startActivity(launcherIntentFor(app))
  }

  private fun launchAppInSplitScreen(app: LauncherApp) {
    check(OnyxSplitScreenLauncher.isSupported) { "Split screen requires a BOOX device" }
    OnyxSplitScreenLauncher.open(this, app)
  }

  private fun launcherIntentFor(app: LauncherApp): Intent =
      Intent(Intent.ACTION_MAIN)
          .addCategory(Intent.CATEGORY_LAUNCHER)
          .setComponent(ComponentName(app.packageName, app.activityName))
}

@Composable
private fun HomeLauncher(
    launchApp: (LauncherApp) -> Unit,
    launchAppInSplitScreen: (LauncherApp) -> Unit,
) {
  val installedAppsProvider = rememberInstalledAppsProvider()
  var apps by remember { mutableStateOf(emptyList<LauncherApp>()) }
  LaunchedEffect(installedAppsProvider) { apps = installedAppsProvider.installedApps() }

  HomeScreen(
      apps = apps,
      onAppClick = { app, splitScreenMode ->
        if (splitScreenMode) {
          launchAppInSplitScreen(app)
        } else {
          launchApp(app)
        }
      },
  )
}
