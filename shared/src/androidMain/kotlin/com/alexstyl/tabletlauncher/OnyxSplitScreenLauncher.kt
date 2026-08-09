package com.alexstyl.tabletlauncher

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

object OnyxSplitScreenLauncher {
  val isSupported: Boolean = Build.MANUFACTURER.equals("ONYX", ignoreCase = true)

  fun open(context: Context, app: LauncherApp) {
    context.startActivity(
        splitLauncherIntent(context)
            .putExtra(selectedAppPackageName, app.packageName)
            .putExtra(selectedAppActivityName, app.activityName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK),
    )
  }

  fun selectedAppFrom(intent: Intent): LauncherApp? {
    val packageName = intent.getStringExtra(selectedAppPackageName) ?: return null
    val activityName = intent.getStringExtra(selectedAppActivityName) ?: return null

    return LauncherApp(packageName, activityName, name = "", icon = null)
  }

  fun startSplit(activity: Activity, selectedApp: LauncherApp) {
    val selectedAppIntent =
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(selectedApp.packageName, selectedApp.activityName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    activity.sendBroadcast(
        Intent(startMultiWindowAction)
            .putExtra(primaryTaskId, activity.taskId)
            .putExtra(primaryTaskCreateMode, splitScreenCreateModeTopOrLeft)
            .putExtra(secondaryTaskBundle, selectedAppIntent.toOnyxTaskBundle())
            .putExtra(splitScreenLayoutType, splitScreenLayoutLeftRight),
    )
  }

  private fun splitLauncherIntent(context: Context): Intent =
      Intent().setComponent(ComponentName(context.packageName, splitScreenLauncherActivityName))

  private fun Intent.toOnyxTaskBundle(): Bundle =
      Bundle(extras ?: Bundle()).apply {
        putParcelable(componentKey, component)
        putString(actionKey, action)
        putInt(flagsKey, flags)
        putParcelable(dataKey, data)
      }

  private const val splitScreenLauncherActivityName =
      "com.alexstyl.tabletlauncher.SplitScreenLauncherActivity"
  private const val selectedAppPackageName = "selected_app_package_name"
  private const val selectedAppActivityName = "selected_app_activity_name"

  private const val startMultiWindowAction = "com.onyx.action.START_MULTI_WINDOW"
  private const val primaryTaskId = "primary_task_id"
  private const val primaryTaskCreateMode = "args_primary_task_create_mode"
  private const val secondaryTaskBundle = "compat_m_args_secondary_task_bundle"
  private const val splitScreenLayoutType = "args_spilt_screen_layout_type"

  private const val componentKey = "compat_m_args_component"
  private const val actionKey = "compat_m_args_action"
  private const val flagsKey = "compat_m_args_flags"
  private const val dataKey = "compat_m_args_data"

  private const val splitScreenCreateModeTopOrLeft = 0
  private const val splitScreenLayoutLeftRight = 0
}
