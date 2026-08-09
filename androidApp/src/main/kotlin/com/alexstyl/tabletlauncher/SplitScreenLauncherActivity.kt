package com.alexstyl.tabletlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color

class SplitScreenLauncherActivity : ComponentActivity() {
  private var selectedApp: LauncherApp? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    selectedApp = OnyxSplitScreenLauncher.selectedAppFrom(intent)
    enableEdgeToEdge()
    setContent {
      App(
          backgroundColor = Color(0xFFF0F0F0),
          showWallpaper = false,
      )
    }
  }

  override fun onPostResume() {
    super.onPostResume()

    selectedApp?.let { app ->
      selectedApp = null
      OnyxSplitScreenLauncher.startSplit(this, app)
    }
  }
}
