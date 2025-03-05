package com.example.launcher.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.launcher.model.AppDao
import com.example.launcher.model.AppEntity
import com.example.launcher.model.AppFolderEntity
import com.example.launcher.model.FolderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: Add Placeholder "tutorial" message

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appDao: AppDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _folders = MutableStateFlow<List<String>>(emptyList())
    val folders: StateFlow<List<String>> = _folders

    private val _appsInFolder = MutableStateFlow<List<AppEntity>>(emptyList())
    val appsInFolder: StateFlow<List<AppEntity>> = _appsInFolder

    private val _dockApps = MutableStateFlow<List<AppEntity>>(emptyList())
    val dockApps: StateFlow<List<AppEntity>> = _dockApps

    init {
        loadFolders()
        loadDockApps()
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

    // TODO: Refuse to add duplicate folders
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
            } else {
                Log.e("HomeViewModel", "Tried to add app to folder $folderName, but folder was not found")
            }
        }
    }

    fun removeAppFromFolder(packageName: String, folderName: String){
        viewModelScope.launch {
            val folder = appDao.getFolderByName(folderName)
            if (folder != null) {
                appDao.removeAppFromFolder(packageName, folder.id)
            } else {
                Log.e("HomeViewModel", "Tried to remove app from folder, but folder was not found: $folderName")
            }
        }
    }

    fun removeFolder(folderName: String) {
        viewModelScope.launch {
            val folder = appDao.getFolderByName(folderName)
            if (folder != null) {
                appDao.deleteFolder(folder.id)
                appDao.deleteFolderApps(folder.id) // Clear memory and delete app relations in the folder
            } else {
                Log.e("HomeViewModel", "Tried to remove folder $folderName, but folder was not found")
            }
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
            } else {
                Log.e("HomeViewModel", "Tried to display apps in folder $folderName, but folder was not found")
            }

        }
    }

    fun launchApp(packageName: String) {
        val pm: PackageManager = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            Log.e("HomeViewModel", "Tried to launch app $packageName, but app was not found")
        }

    }

    // App dock logic

    fun addToDock(packageName: String) {
        viewModelScope.launch {
            val dockSize = appDao.getDockAppCount()

            if (dockSize >= 4) {
                Log.d("HomeViewModel", "Current app dock size exceeds maximum: $dockSize")
                Toast.makeText(context, "App dock is full", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(
                    "HomeViewModel",
                    "App $packageName added to app dock, current size: $dockSize"
                )
                appDao.addToDock(packageName)
            }
        }
    }

    fun removeFromDock(packageName: String) {
        viewModelScope.launch {
            appDao.removeFromDock(packageName)
        }
    }
}
