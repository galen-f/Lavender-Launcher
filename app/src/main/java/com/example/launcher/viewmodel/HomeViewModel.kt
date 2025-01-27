package com.example.launcher.viewmodel

import android.app.WallpaperManager
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.AppInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Canvas
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    // Designate as apps, call from HomeScreen.kt to render them.
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    init {
        loadInstalledApps()
    }

    // Get all the installed apps on startup along with all important info, icon, name, etc
    private fun loadInstalledApps() {
        viewModelScope.launch {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val userHandle = android.os.Process.myUserHandle()
            val currentPackageName = context.packageName
            val appList = launcherApps.getActivityList(null, userHandle).mapNotNull { // Map to AppInfo, ignore Null values (Like this app)
                if (it.applicationInfo.packageName != currentPackageName) { // Filter out the launcher app
                    AppInfo(
                        label = it.label.toString(),
                        packageName = it.applicationInfo.packageName,
                        icon = drawableToBitmap(it.applicationInfo.loadIcon(context.packageManager))
                    )
                } else {
                    null // Skip this current app
                }
            }
            _apps.value = appList.sortedBy { it.label }
        }
    }

    // Start apps if they get clicked
    fun launchApp(packageName: String) {
        val pm: PackageManager = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        intent?.let {
            context.startActivity(it)
        }
    }

    // Convert app icon to Bitmap, allows the app icon to be displayed in image in the compose of HomeScreen.kt
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}