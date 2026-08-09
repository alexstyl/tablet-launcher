package com.alexstyl.tabletlauncher

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

interface WallpaperProvider {
  fun wallpaper(): ImageBitmap?
}

@Composable expect fun rememberWallpaperProvider(): WallpaperProvider
