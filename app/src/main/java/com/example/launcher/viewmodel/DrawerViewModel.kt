package com.example.launcher.viewmodel

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.model.AppDao
import com.example.launcher.model.AppInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.example.launcher.utils.BitmapConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: there is a lot of data management in this file, should be refactored

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val appDao: AppDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // Designate as apps, call from AppDrawer.kt to render them.
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    init {
        loadAppsFromDatabase()
        fetchInstalledApps()
    }

    private fun loadAppsFromDatabase() {
        viewModelScope.launch {
            appDao.getAllApps().collectLatest { storedApps ->
                _apps.value = storedApps
            }
        }
    }

    private fun fetchInstalledApps() {
        // TODO: this function needs a lot of work I hate it, maybe could be split into two, retrieveApps and storeApps
        viewModelScope.launch {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val userHandle = android.os.Process.myUserHandle()
            val currentPackageName = context.packageName

            val installedApps = launcherApps.getActivityList(null, userHandle).mapNotNull {
                if (it.applicationInfo.packageName != currentPackageName) {
                     // TODO: Why are we doing this??
                    val iconDrawable = it.applicationInfo.loadIcon(context.packageManager)
                    val iconBitmap = drawableToBitmap(iconDrawable)
                    val iconByteArray = BitmapConverter.fromBitmap(iconBitmap) // Convert to ByteArray

                    AppInfo(
                        label = it.label.toString(),
                        packageName = it.applicationInfo.packageName,
                        icon = iconByteArray // Can fetch icon separately
                    )
                } else {
                    null
                }
            }

            installedApps.forEach { appDao.insertApp(it) }
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

    // TODO: May be unnecessary
    // Convert app icon to Bitmap, allows the app icon to be displayed in image in the compose of AppDrawer.kt
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