package com.alexstyl.tabletlauncher

import androidx.compose.ui.window.singleWindowApplication

fun main() =
    singleWindowApplication(title = "Tablet Launcher") {
      HomeScreen(
          apps = emptyList(),
          hiddenApps = emptyList(),
          onAppClick = {},
          onAppLongClick = {},
          onHideApp = {},
          onRestoreApp = {},
      )
    }
