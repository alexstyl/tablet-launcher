package com.alexstyl.tabletlauncher

import android.app.WallpaperManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberWallpaperProvider(): WallpaperProvider {
  val applicationContext = LocalContext.current.applicationContext
  return remember(applicationContext) { AndroidWallpaperProvider(applicationContext) }
}

private class AndroidWallpaperProvider(
    private val context: Context,
) : WallpaperProvider {
  override fun wallpaper(): ImageBitmap? =
      runCatching { WallpaperManager.getInstance(context).drawable?.toImageBitmap() }.getOrNull()
}
