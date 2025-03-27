package com.example.launcher.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.model.AppDao
import com.example.launcher.model.AppEntity
import com.example.launcher.model.AppFolderEntity
import com.example.launcher.model.FolderEntity
import com.example.launcher.model.SettingsRepository
import com.example.launcher.utils.ScreentimeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appDao: AppDao,
    private val settingsRepository: SettingsRepository,
    val screentimeManager: ScreentimeManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _folders = MutableStateFlow<List<String>>(emptyList())
    val folders: StateFlow<List<String>> = _folders

    private val _appsInFolder = MutableStateFlow<List<AppEntity>>(emptyList())
    val appsInFolder: StateFlow<List<AppEntity>> = _appsInFolder

    private val _dockApps = MutableStateFlow<List<AppEntity>>(emptyList())
    val dockApps: StateFlow<List<AppEntity>> = _dockApps

    // Put the app the user wants to launch here, used for high friction mode
    val _pendingLaunchApp = MutableStateFlow<String?>(null)
    val pendingLaunchApp: StateFlow<String?> = _pendingLaunchApp

    val greyScaledApps: StateFlow<Boolean> = settingsRepository.isGreyScaleIconsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _screenTime = MutableStateFlow(0L)
    val screenTime: StateFlow<Long> = _screenTime

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog

    fun setPermissionDialogState(state: Boolean) {
        _showPermissionDialog.value = state
    }

    init {
        loadFolders()
        loadDockApps()
        fetchScreenTime() // Check the screen-time every time the home screen starts up
    }

    private fun loadFolders() {
        viewModelScope.launch {
            appDao.getAllFolders().collect { folderEntities ->
                _folders.value = folderEntities.map { it.name }
            }
        }
    }

    private fun loadDockApps() {
        viewModelScope.launch {
            appDao.getDockApps().collect { dockAppsList ->
                _dockApps.value = dockAppsList
            }
        }
    }

    fun addFolder(folderName: String) {
        viewModelScope.launch {
            appDao.insertFolder(FolderEntity(name = folderName))
            loadFolders()
        }
    }

    fun addAppToFolder(packageName: String, folderName: String) {
        viewModelScope.launch {
            val folder = appDao.getFolderByName(folderName)
            if (folder != null) {
                appDao.insertAppIntoFolder(
                    AppFolderEntity(
                        folderId = folder.id,
                        packageName = packageName
                    )
                )
            } else {  Log.e("HomeViewModel", "Tried to add app to folder $folderName, but folder was not found")  }
        }
    }

    fun removeAppFromFolder(packageName: String, folderName: String){
        viewModelScope.launch {
            val folder = appDao.getFolderByName(folderName)
            if (folder != null) {
                appDao.removeAppFromFolder(packageName, folder.id)
            } else {Log.e("HomeViewModel", "Tried to remove app from folder, but folder was not found: $folderName") }
        }
    }

    fun removeFolder(folderName: String) {
        viewModelScope.launch {
            val folder = appDao.getFolderByName(folderName)
            if (folder != null) {
                appDao.deleteFolder(folder.id)
                appDao.deleteFolderApps(folder.id) // Clear memory and delete app relations in the folder
            } else {Log.e("HomeViewModel", "Tried to remove folder $folderName, but folder was not found") }
        }
    }

    fun displayAppsInFolder(folderName: String) {
        viewModelScope.launch {
            val folder = appDao.getFolderByName(folderName)
            if (folder != null) {
                appDao.getAppsInFolder(folder.id).collect { appEntities ->
                    Log.d("HomeViewModel", "Apps fetched: ${appEntities.map { it.label }}")
                    _appsInFolder.value = appEntities
                }
            } else {Log.e("HomeViewModel", "Tried to display apps in folder $folderName, but folder was not found") }
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


    fun launchApp(packageName: String) {
        val pm: PackageManager = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {Log.e("HomeViewModel", "Tried to launch app $packageName, but app was not found") }
    }

    // App dock logic

    fun addToDock(packageName: String) {
        viewModelScope.launch {
            val maxSize = settingsRepository.maxDockSize.first() // Get the dock size from the settings system
            val dockSize = appDao.getDockAppCount()

            if (dockSize >= maxSize ) {
                Log.d("HomeViewModel", "Current app dock size exceeds maximum: $dockSize")
                Toast.makeText(context, "App dock is full", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("HomeViewModel", "App $packageName added to app dock, current size: $dockSize")
                appDao.addToDock(packageName)
            }
        }
    }

    fun removeFromDock(packageName: String) {
        viewModelScope.launch {
            appDao.removeFromDock(packageName)
        }
    }

    fun deleteApp(packageName: String) {
        viewModelScope.launch {
            /*
            Delete the database entry regardless of if the app actually gets uninstalled. As the uninstall api does not return a
            result code, we cannot know if it actually did get uninstalled without a broadcast receiver.
            The app will be added back to the database next recomposition if it is not uninstalled.
            */

            // Delete the database entry for the app
            appDao.deleteApp(packageName)

            // Open the system dialog to handle the uninstall
            val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.fromParts("package", packageName, null)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(uninstallIntent)
        }
    }

    // Screen-time Tracker Logic
    private fun fetchScreenTime() {
        viewModelScope.launch {
            if (!screentimeManager.hasUsageAccess(context)) {
                // Check if we've already asked for permission
                val hasBeenAsked = settingsRepository.isUsagePermissionRequested.first()
                if (!hasBeenAsked) {
                    // Mark prompted and then show the dialog
                    settingsRepository.setUsagePermissionRequested(true)
                    _showPermissionDialog.value = true
                    return@launch
                }
            }
            // If permission is granted (or already asked), update screen time
            _screenTime.value = screentimeManager.getTotalScreenTime(context)
        }
    }
}
