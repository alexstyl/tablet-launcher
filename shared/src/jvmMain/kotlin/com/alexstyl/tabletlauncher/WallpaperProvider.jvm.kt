package com.alexstyl.tabletlauncher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

@Composable
actual fun rememberWallpaperProvider(): WallpaperProvider = remember { EmptyWallpaperProvider }

private data object EmptyWallpaperProvider : WallpaperProvider {
  override fun wallpaper(): ImageBitmap? = null
}
