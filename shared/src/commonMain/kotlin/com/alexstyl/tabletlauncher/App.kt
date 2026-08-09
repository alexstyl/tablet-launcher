package com.alexstyl.tabletlauncher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.composables.ui.theme.ComposablesTheme

@Composable
fun App(
    backgroundColor: Color = Color.White,
    showWallpaper: Boolean = true,
) {
  ComposablesTheme {
    MaterialTheme {
      HomeScreen(
          modifier = Modifier.fillMaxSize(),
          backgroundColor = backgroundColor,
          showWallpaper = showWallpaper,
      )
    }
  }
}

@Preview
@Composable
fun AppPreview() {
  App()
}
