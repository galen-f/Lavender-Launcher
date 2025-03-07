package com.example.launcher.utils

import android.app.AlertDialog
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.*
import javax.inject.Inject

@ViewModelScoped
class ScreentimeManager @Inject constructor() {

    fun getScreenTimeStats(context: Context): List<UsageStats> { // WIP method, gather screentime stats
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -1) // Get data for the last 24 hours
        val startTime = calendar.timeInMillis

        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: emptyList()
    }

    fun getTotalScreenTime(context: Context): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -1) // Get last 24 hours
        val startTime = calendar.timeInMillis

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return 0

        return usageStatsList.sumOf { it.totalTimeInForeground } // Sum up the time
    }

    fun hasUsageAccess(context: Context): Boolean { // Check if the app has usage permissions
        return try {
            val packageName = context.packageName
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),
                packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun requestUsageAccess(context: Context) { // If permission not granted, redirect to the settings screen
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

}