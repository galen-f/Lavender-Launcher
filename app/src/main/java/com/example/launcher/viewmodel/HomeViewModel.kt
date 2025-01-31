package com.example.launcher.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.model.AppFolderEntity
import com.example.launcher.model.FolderDao
import com.example.launcher.model.FolderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor( private val folderDao: FolderDao ) : ViewModel() {

    private val _message = MutableStateFlow("Welcome to the Home Screen!")
    val message: StateFlow<String> = _message

    private val _folders = MutableStateFlow<List<String>>(emptyList())
    val folders: StateFlow<List<String>> = _folders

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

    fun addFolder(folderName: String) {
        viewModelScope.launch {
            folderDao.insertFolder(FolderEntity(name = folderName))
            loadFolders()
        }
    }

    fun addAppToFolder(packageName: String, folderName: String) {
        viewModelScope.launch {
            val folder = folderDao.getFolderByName(folderName)
            if (folder != null) {
                folderDao.insertAppIntoFolder(AppFolderEntity(folderId = folder.id, packageName = packageName))
                Log.d("HomeViewModel", "App added to folder: \n Folder: $folderName,\n Package: $packageName,\n Folder ID: ${folder.id}")
            }
        }
    }

    fun displayAppsInFolder(folderName: String) {
        Log.d("HomeViewModel", "Displaying apps in folder: $folderName")
    }

}
