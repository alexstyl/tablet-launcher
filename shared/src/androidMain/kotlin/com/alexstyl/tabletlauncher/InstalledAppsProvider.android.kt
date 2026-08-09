package com.alexstyl.tabletlauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberInstalledAppsProvider(): InstalledAppsProvider {
  val applicationContext = LocalContext.current.applicationContext
  return remember(applicationContext) { AndroidInstalledAppsProvider(applicationContext) }
}

private class AndroidInstalledAppsProvider(
    private val context: Context,
) : InstalledAppsProvider {
  override fun installedApps(): List<LauncherApp> {
    val packageManager = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return packageManager
        .queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
        .map { resolveInfo ->
          val activityInfo = resolveInfo.activityInfo
          LauncherApp(
              packageName = activityInfo.packageName,
              activityName = activityInfo.name,
              name = resolveInfo.loadLabel(packageManager).toString(),
              icon = resolveInfo.loadIcon(packageManager).toImageBitmap(),
          )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.name.lowercase() }
  }

  override fun launch(app: LauncherApp) {
    context.startActivity(
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(app.packageName, app.activityName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
  }
}

internal fun Drawable.toImageBitmap(): ImageBitmap {
  val width = intrinsicWidth.coerceAtLeast(1)
  val height = intrinsicHeight.coerceAtLeast(1)
  val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val previousBounds = bounds

  try {
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))
  } finally {
    bounds = previousBounds
  }

  return bitmap.asImageBitmap()
}
