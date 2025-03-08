package com.example.launcher.utils

import android.app.AlertDialog
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.log

@ViewModelScoped
class ScreentimeManager @Inject constructor() {

    fun getTotalScreenTime(context: Context): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Launcher apps make screentime look way higher than it actually is because they run in the background so we have to ignore it
        // Get the launcher app package info
        val launcherPackage = getHomePackages(context)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis // milliseconds from 1970 to midnight the day of today
//        timeConverter(startTime, "startTime") // Logs the start time

        val endTime = System.currentTimeMillis() // Milliseconds from 1970 to the current time
//        timeConverter(endTime, "endTime") // Logs the end time

//        logApps(context)

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        ) ?: return 0

        return usageStatsList
            .filter {it.packageName !in launcherPackage } // Ignore the launcher app
            .sumOf { it.totalTimeInForeground } // Sum up the time
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


    fun timeConverter(timeInMillis: Long, label: String) {

        // Convert to Date object
        val date = Date(timeInMillis)

        // Define the format
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Format the date
        val formattedTime = sdf.format(date)

        Log.d("screentimeManager","$label - Formatted Time: $formattedTime")
    }

    // Function which returns a list of the top 50 apps, commented out but kept in case I want to display the top used apps
    private fun logApps(context: Context) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager


        // Get start time (midnight today)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        // Query usage stats
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        ) ?: return

        // Sort by foreground time in descending order
        val sortedUsageStats = usageStatsList
            .sortedByDescending { it.totalTimeInForeground } // Sort from most to least used

        // Get the top 3 apps
        val topApps = sortedUsageStats.take(50)

        // Log the top 3 apps
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        Log.d("ScreentimeManager", "Top apps used today:")
        topApps.forEachIndexed { index, usageStats ->
            val packageName = usageStats.packageName
            val minutesUsed = usageStats.totalTimeInForeground / (1000 * 60) // Convert milliseconds to minutes
            Log.d("ScreentimeManager", "#${index + 1}: $packageName - $minutesUsed min")
        }
    }

    // Launcher apps run up screen-time faster than make sense, this method returns
    // a list of all launcher apps to allow us to filter them out
    private fun getHomePackages(context: Context): List<String> {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
        return resolveInfoList.mapNotNull { it.activityInfo?.packageName }.distinct()
    }
}