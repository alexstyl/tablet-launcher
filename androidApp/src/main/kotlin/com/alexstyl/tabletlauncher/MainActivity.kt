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
import android.util.Base64
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
import java.util.UUID

class MainActivity : ComponentActivity() {
  private var hiddenPackageNames by mutableStateOf<Set<String>>(emptySet())
  private var folders by mutableStateOf<List<LauncherFolder>>(emptyList())
  private var hasRequestedHomeRole = false

  private val requestHomeRole =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    hideSystemBars()
    hiddenPackageNames =
        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
            .getStringSet(HIDDEN_PACKAGES_KEY, emptySet()) ?: emptySet()
    folders = loadFolders()
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
      val visibleApps = apps.filterNot { it.packageName in hiddenPackageNames }
      val hiddenApps = apps.filter { it.packageName in hiddenPackageNames }
      HomeScreen(
          apps = visibleApps,
          hiddenApps = hiddenApps,
          folders = folders,
          onAppClick = ::launchApp,
          onAppLongClick = ::openAppInfo,
          onHideApp = ::hideApp,
          onRestoreApp = ::restoreApp,
          onSaveFolder = ::saveFolder,
          onDeleteFolder = ::deleteFolder,
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

  private fun hideApp(app: LauncherApp) {
    updateHiddenPackages(hiddenPackageNames + app.packageName)
  }

  private fun restoreApp(app: LauncherApp) {
    updateHiddenPackages(hiddenPackageNames - app.packageName)
  }

  private fun saveFolder(folderId: String?, name: String, apps: Set<LauncherApp>) {
    val appKeys = apps.mapTo(mutableSetOf()) { it.key }
    folders =
        folders.mapNotNull { folder ->
          val remainingAppKeys = folder.appKeys - appKeys
          if (folder.id == folderId) null
          else folder.copy(appKeys = remainingAppKeys).takeIf { it.appKeys.isNotEmpty() }
        } +
            LauncherFolder(
                id = folderId ?: UUID.randomUUID().toString(), name = name, appKeys = appKeys)
    saveFolders()
  }

  private fun deleteFolder(folderId: String) {
    folders = folders.filterNot { it.id == folderId }
    saveFolders()
  }

  private fun loadFolders(): List<LauncherFolder> {
    val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
    val encodedFolders = preferences.getStringSet(FOLDERS_KEY, emptySet()).orEmpty()
    val folders = encodedFolders.mapNotNull(::decodeFolder)
    val migratedFolders = folders.mapTo(mutableSetOf(), ::encodeFolder)
    if (encodedFolders != migratedFolders) {
      preferences.edit().putStringSet(FOLDERS_KEY, migratedFolders).apply()
    }
    return folders.sortedBy { it.name.lowercase() }
  }

  private fun saveFolders() {
    getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        .edit()
        .putStringSet(FOLDERS_KEY, folders.mapTo(mutableSetOf(), ::encodeFolder))
        .apply()
  }

  private fun encodeFolder(folder: LauncherFolder): String =
      listOf(
              folder.id,
              folder.name,
              folder.appKeys.joinToString("\n") { "${it.packageName}\t${it.activityName}" },
          )
          .joinToString("|") { value ->
            Base64.encodeToString(
                value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
          }

  private fun decodeFolder(encodedFolder: String): LauncherFolder? =
      runCatching {
            val values =
                encodedFolder.split("|").map { value ->
                  String(Base64.decode(value, Base64.NO_WRAP or Base64.URL_SAFE), Charsets.UTF_8)
                }
            require(values.size == 3)
            LauncherFolder(
                id = values[0],
                name = values[1],
                appKeys =
                    values[2]
                        .split(if ('\n' in values[2]) "\n" else "\\n")
                        .filter(String::isNotBlank)
                        .map { app ->
                          val (packageName, activityName) =
                              app.split(if ('\t' in app) "\t" else "\\t", limit = 2)
                          LauncherAppKey(packageName, activityName)
                        }
                        .toSet(),
            )
          }
          .getOrNull()

  private fun updateHiddenPackages(packages: Set<String>) {
    hiddenPackageNames = packages
    getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        .edit()
        .putStringSet(HIDDEN_PACKAGES_KEY, packages)
        .apply()
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

  private companion object {
    const val PREFERENCES_NAME = "launcher"
    const val HIDDEN_PACKAGES_KEY = "hidden_packages"
    const val FOLDERS_KEY = "folders"
  }
}
