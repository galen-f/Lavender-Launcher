package com.example.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}
