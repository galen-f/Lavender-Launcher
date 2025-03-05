package com.example.launcher.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    }

    val maxDockSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MAX_DOCK_SIZE_KEY] ?: DEFAULT_DOCK_SIZE
    }

    suspend fun setMaxDockSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_DOCK_SIZE_KEY] = size
        }
    }
}
