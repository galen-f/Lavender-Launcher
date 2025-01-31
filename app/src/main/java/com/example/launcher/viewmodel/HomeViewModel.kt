package com.example.launcher.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.model.AppFolderEntity
import com.example.launcher.model.FolderDao
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
    private val folderDao: FolderDao ,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _message = MutableStateFlow("Welcome to the Home Screen!")
    val message: StateFlow<String> = _message

    private val _folders = MutableStateFlow<List<String>>(emptyList())
    val folders: StateFlow<List<String>> = _folders

    private val _appsInFolder = MutableStateFlow<List<String>>(emptyList())
    val appsInFolder: StateFlow<List<String>> = _appsInFolder

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            folderDao.getAllFolders().collect { folderEntities ->
                _folders.value = folderEntities.map { it.name }
            }
        }
    }

    // TODO: Refuse to add duplicate folders
    fun addFolder(folderName: String) {
        viewModelScope.launch {
            folderDao.insertFolder(FolderEntity(name = folderName))
            loadFolders()
        }
    }

    // TODO: Refuse to add duplicate apps
    fun addAppToFolder(packageName: String, folderName: String) {
        viewModelScope.launch {
            val folder = folderDao.getFolderByName(folderName)
            if (folder != null) {
                folderDao.insertAppIntoFolder(
                    AppFolderEntity(
                        folderId = folder.id,
                        packageName = packageName
                    )
                )
                Log.d(
                    "HomeViewModel",
                    "App added to folder: \n Folder: $folderName,\n Package: $packageName,\n Folder ID: ${folder.id}"
                )
            } else {
                Log.d("HomeViewModel", "Folder not found: $folderName")
            }
        }
    }

    fun displayAppsInFolder(folderName: String) {
        viewModelScope.launch {
            val folder = folderDao.getFolderByName(folderName)
            if (folder != null) {
                folderDao.getAppsInFolder(folder.id).collect { appEntities ->
                    _appsInFolder.value = appEntities.map { it.packageName }
                    Log.d("HomeViewModel", "Apps in folder with folder $folderName")
                }
            } else {
                Log.d("HomeViewModel", "Folder not found: $folderName")
            }

        }
    }

    fun launchApp(packageName: String) {
        val pm: PackageManager = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            Log.d("HomeViewModel", "App not found: $packageName")
        }

    }
}
