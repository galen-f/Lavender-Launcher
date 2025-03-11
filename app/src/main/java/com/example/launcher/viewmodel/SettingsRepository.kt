package com.example.launcher.viewmodel

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
    }

    val maxDockSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MAX_DOCK_SIZE_KEY] ?: DEFAULT_DOCK_SIZE
    }

    suspend fun setMaxDockSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_DOCK_SIZE_KEY] = size
        }
    }

    // Grey-scale app icons settings

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
}
