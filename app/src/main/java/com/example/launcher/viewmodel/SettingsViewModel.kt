package com.example.launcher.viewmodel

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Expose current settings using flows from the repository
    val maxDockSize: Flow<Int> = settingsRepository.maxDockSize

    val greyScaledApps: StateFlow<Boolean> = settingsRepository.isGreyScaleIconsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val darkMode: StateFlow<Boolean> = settingsRepository.isDarkModeEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // For dark mode, assume you add a similar DataStore key and methods in your repository.
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    fun setMaxDockSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.setMaxDockSize(size)
        }
    }

    fun toggleGreyScaleApps() {
        viewModelScope.launch {
            settingsRepository.toggleGreyScaleIcons()
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                val current = preferences[DARK_MODE_KEY] ?: false // Check state, default to false
                preferences[DARK_MODE_KEY] = !current // Inverse current
                Log.d("Settings", "Dark Mode Enabled: $current")
            }
        }
    }
}