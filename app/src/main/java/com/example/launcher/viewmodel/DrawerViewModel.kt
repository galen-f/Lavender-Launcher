package com.example.launcher.viewmodel

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.model.AppDao
import com.example.launcher.model.AppEntity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import com.example.launcher.viewmodel.SettingsRepository.Companion.GREYSCALE_ICONS_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: there is a lot of data management in this file, should be refactored

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val appDao: AppDao,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Designate as apps, call from AppDrawer.kt to render them.
    private val _apps = MutableStateFlow<List<AppEntity>>(emptyList())
    val apps: StateFlow<List<AppEntity>> = _apps

    val greyScaledApps: StateFlow<Boolean> = settingsRepository.isGreyScaleIconsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)



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
        viewModelScope.launch {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val userHandle = android.os.Process.myUserHandle()
            val currentPackageName = context.packageName

            val installedApps = launcherApps.getActivityList(null, userHandle).mapNotNull {
                if (it.applicationInfo.packageName != currentPackageName) {
                    AppEntity(
                        label = it.label.toString(),
                        packageName = it.applicationInfo.packageName,
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


    fun toggleGreyScaleApps() {
        viewModelScope.launch {
            settingsRepository.toggleGreyScaleIcons()
        }
    }
}