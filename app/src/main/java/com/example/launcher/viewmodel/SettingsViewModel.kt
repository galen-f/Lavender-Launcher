package com.example.launcher.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Expose current settings using flows from the repository
    val isGreyScaleIconsEnabled: Flow<Boolean> = settingsRepository.isGreyScaleIconsEnabled
    val maxDockSize: Flow<Int> = settingsRepository.maxDockSize

    val greyScaledApps: StateFlow<Boolean> = settingsRepository.isGreyScaleIconsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val darkMode: StateFlow<Boolean> = settingsRepository.isDarkModeEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // For dark mode, assume you add a similar DataStore key and methods in your repository.
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    fun isDarkModeEnabled() {

    }

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
                val current = preferences[DARK_MODE_KEY] ?: false
                preferences[DARK_MODE_KEY] = !current
            }
        }
    }
}