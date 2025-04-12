package com.example.launcher.utils

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@ViewModelScoped
class ScreentimeManager @Inject constructor() {

    fun getTotalScreenTime(context: Context): Long {
        /*
        Usage stats can be extremely touchy. On certain devices it may include services times, as a result
        you get phantom screen-time notes where the screen-time is duplicated (sometimes I have seen up to 9 copies of an app register)
        Usually the smallest screen-time is the one which is active foregrounding, but it is impossible to tell with the data
        given by the manager. As a result there can be some significant inaccuracies but this is the best we can do.

        Also while im ranting, the java.time library doesn't seem to have a .toEpochMillis function
        anymore, that's why ive gotten millis by just multiplying seconds by 1000 lol
         */
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // I dont actually use this in the current build but in case I want to filter out launcher apps, this does that
        // During development I did this but im not sure it was the best feature, kept it in in case I change my mind
        // Get the launcher app package info
        val launcherPackage = getHomePackages(context)

        val zoneId = ZoneId.systemDefault()
        val startTime = ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId).toEpochSecond()*1000
        val endTime = System.currentTimeMillis()

//        logApps(context)

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return 0

        val accurateUsage = usageStatsList.groupBy { it.packageName }.mapValues { (_, stats) ->
            //pick the record of that package with the smallest screen-time (filters out phantom screen-times in some devices)
            stats.minByOrNull { it.totalTimeInForeground }?.totalTimeInForeground ?: 0L
        }

        return accurateUsage
            .values.sum()
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

    // Function which returns a list of the top 50 apps, commented out but kept in case I want to display the top used apps (mostly for debugging)
    private fun logApps(context: Context) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val zoneId = ZoneId.systemDefault()
        val startTime = ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId).toEpochSecond()*1000
        val endTime = System.currentTimeMillis()

        // Query usage stats
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return

        val formattedStartDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(startTime))
        val formattedEndDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(endTime))

        val accurateUsage = usageStatsList.groupBy { it.packageName }.mapValues { (_, stats) ->
            //pick the record of that package with the smallest screen-time (filters out phantom screen-times in some devices)
            stats.minByOrNull { it.totalTimeInForeground }?.totalTimeInForeground ?: 0L
        }


        // Sort by foreground time in descending order
        val sortedUsageStats = accurateUsage.toList().sortedByDescending { (_, time) -> time }
//            .sortedByDescending { it.totalTimeInForeground } // Sort from most to least used

        // Get the top 3 apps
        val topApps = sortedUsageStats
            .take(50)

        // Log the top 3 apps
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        Log.d("ScreentimeManager","Checking screentime between $formattedStartDate and $formattedEndDate")
        Log.d("ScreentimeManager", "Top apps used today:")
        topApps.forEachIndexed { index, (packageName, timeInForeground) ->
            val minutesUsed = timeInForeground / (6000) // Convert milliseconds to minutes
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