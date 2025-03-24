package com.example.launcher.viewmodel

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.model.AppDao
import com.example.launcher.model.AppEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val appDao: AppDao,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Designate apps as apps, call from AppDrawer.kt to render them.
    private val _apps = MutableStateFlow<List<AppEntity>>(emptyList())
    val apps: StateFlow<List<AppEntity>> = _apps

    // Put the app the user wants to launch here, used for high friction mode
    val _pendingLaunchApp = MutableStateFlow<String?>(null)
    val pendingLaunchApp: StateFlow<String?> = _pendingLaunchApp

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
                } else { null }
            }

            // Remove "stale" apps
            /* This is necessary because in the case an app was uninstalled without the knowledge
            of the launcher, the launcher would crash catastrophically as it tried to spawn the
            package that didn't exist.
             */
            val installedPackages = installedApps.map {it.packageName }.toSet()
            val storedApps = appDao.getAllApps().first()
            storedApps.forEach { storedApps ->
                if (!installedPackages.contains(storedApps.packageName)) {
                    appDao.deleteApp(storedApps.packageName)
                }
            }
            installedApps.forEach { appDao.insertApp(it) }
        }
    }

    fun preLaunchApp(packageName: String) {
        viewModelScope.launch {
            // If high friction is enabled, forward to a dialog box
            if (settingsRepository.isHighFriction.first()) {
                _pendingLaunchApp.value = packageName
            } else {
                // if high friction is not enabled, just launch the app
                launchApp(packageName)
            }
        }
    }

    fun confirmLaunch() { // When suer confirms they want to open the app
        _pendingLaunchApp.value?.let { launchApp(it) }
        _pendingLaunchApp.value = null
    }

    fun cancelLaunch() { // Call when user decides not to open app
        _pendingLaunchApp.value = null
    }

    // Start apps if they get clicked
    fun launchApp(packageName: String) {
        val pm: PackageManager = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        intent?.let {
            context.startActivity(it)
        }
    }
}