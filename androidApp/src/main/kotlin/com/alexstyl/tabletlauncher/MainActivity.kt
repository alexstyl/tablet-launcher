package com.alexstyl.tabletlauncher

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
  private var hasRequestedHomeRole = false

  private val requestHomeRole =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    hideSystemBars()
    setContent {
      val installedAppsProvider = rememberInstalledAppsProvider()
      var apps by remember<MutableState<List<LauncherApp>>> { mutableStateOf(emptyList()) }
      LaunchedEffect(installedAppsProvider) { apps = installedAppsProvider.installedApps() }
      DisposableEffect(installedAppsProvider) {
        val packageChangeReceiver =
            object : BroadcastReceiver() {
              override fun onReceive(context: Context, intent: Intent) {
                apps = installedAppsProvider.installedApps()
              }
            }

        val filter =
            IntentFilter().apply {
              addAction(Intent.ACTION_PACKAGE_ADDED)
              addAction(Intent.ACTION_PACKAGE_REMOVED)
              addAction(Intent.ACTION_PACKAGE_CHANGED)
              addAction(Intent.ACTION_PACKAGE_REPLACED)
              addDataScheme("package")
            }
        registerPackageChangeReceiver(packageChangeReceiver, filter)

        onDispose { unregisterReceiver(packageChangeReceiver) }
      }
      HomeScreen(
          apps = apps,
          onAppClick = ::launchApp,
          onAppLongClick = ::openAppInfo,
      )
    }
  }

  private fun launchApp(app: LauncherApp) {
    startActivity(
        launcherIntentFor(app)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK,
            ))
  }

  private fun launcherIntentFor(app: LauncherApp): Intent =
      Intent(Intent.ACTION_MAIN)
          .addCategory(Intent.CATEGORY_LAUNCHER)
          .setComponent(ComponentName(app.packageName, app.activityName))

  private fun openAppInfo(app: LauncherApp) {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", app.packageName, null),
        ),
    )
  }

  private fun registerPackageChangeReceiver(
      receiver: BroadcastReceiver,
      filter: IntentFilter,
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
      registerReceiver(receiver, filter)
    }
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

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)

    if (hasFocus) {
      hideSystemBars()
    }
  }

  private fun hideSystemBars() {
    WindowCompat.getInsetsController(window, window.decorView).apply {
      systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      hide(WindowInsetsCompat.Type.systemBars())
    }
  }
}
