package com.example.launcher.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val MAX_DOCK_SIZE_KEY = intPreferencesKey("max_dock_size")
        const val DEFAULT_DOCK_SIZE = 4 // Default limit

        // Greyscale app icons setting key
        val GREYSCALE_ICONS_KEY = booleanPreferencesKey("grey_scale_app_icons")

        // Dark-mode settings Key
        val DARK_MODE_KEY = booleanPreferencesKey(("dark_mode"))

        // High-friction mode settings Key
        val HIGH_FRICTION_KEY = booleanPreferencesKey("high_friction")

        // Check if the user has denied usage permissions
        val USAGE_PERMISSION_REQUESTED_KEY = booleanPreferencesKey("usage_permissions_requested")
    }

    val maxDockSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MAX_DOCK_SIZE_KEY] ?: DEFAULT_DOCK_SIZE
    }

    suspend fun setMaxDockSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_DOCK_SIZE_KEY] = size
        }
    }

    // Flow to observe the grey-scale app icons setting
    val isGreyScaleIconsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[GREYSCALE_ICONS_KEY] ?: false
    }

    // Function to update the grey-scale app icons setting
    suspend fun toggleGreyScaleIcons() {
        context.dataStore.edit { preferences ->
            val currentValue = preferences[GREYSCALE_ICONS_KEY] ?: false // Current value, default to false
            preferences[GREYSCALE_ICONS_KEY] = !currentValue // store Inverse current value
        }
    }

    val isHighFriction: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIGH_FRICTION_KEY] ?: false
    }

    suspend fun toggleHighFriction() {
        context.dataStore.edit { preferences ->
            val currentValue = preferences[HIGH_FRICTION_KEY] ?: false // Current value, default to false
            preferences[HIGH_FRICTION_KEY] = !currentValue // store Inverse current value
        }
    }

    val isUsagePermissionRequested: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USAGE_PERMISSION_REQUESTED_KEY] ?: false
    }

    suspend fun setUsagePermissionRequested(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ USAGE_PERMISSION_REQUESTED_KEY ] = value
        }
    }

    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }
}
