package com.alexstyl.tabletlauncher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.composables.ui.theme.ComposablesTheme

@Composable
fun LauncherAppGrid(
    apps: List<LauncherApp>,
    onAppClick: (LauncherApp) -> Unit,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black,
    showWallpaper: Boolean = true,
) {
  ComposablesTheme {
    MaterialTheme {
      LauncherAppGrid(
          modifier = Modifier.fillMaxSize(),
          backgroundColor = backgroundColor,
          contentColor = contentColor,
          apps = apps,
          onAppClick = onAppClick,
          showWallpaper = showWallpaper,
      )
    }
  }
}
