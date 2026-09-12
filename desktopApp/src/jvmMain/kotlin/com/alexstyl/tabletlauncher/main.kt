package com.alexstyl.tabletlauncher

import androidx.compose.ui.window.singleWindowApplication

fun main() =
    singleWindowApplication(title = "Tablet Launcher") {
      HomeScreen(
          apps = emptyList(),
          hiddenApps = emptyList(),
          folders = emptyList(),
          onAppClick = {},
          onAppLongClick = {},
          onHideApp = {},
          onRestoreApp = {},
          onSaveFolder = { _, _, _ -> },
          onDeleteFolder = {},
      )
    }
