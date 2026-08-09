package com.alexstyl.tabletlauncher

import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
  private var hasRequestedHomeRole = false

  private val requestHomeRole =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent { App() }
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
}
