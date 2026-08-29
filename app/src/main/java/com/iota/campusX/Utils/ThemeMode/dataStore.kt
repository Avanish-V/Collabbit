// ThemePreference.kt
package com.iota.campusX.Utils.ThemeMode

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.iota.campusX.Screens.Setting.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

object ThemePreference {
    private val THEME_KEY = intPreferencesKey("theme_mode")

    fun getThemeMode(context: Context): Flow<ThemeMode> {
        return context.dataStore.data.map { prefs ->
            when (prefs[THEME_KEY]) {
                ThemeMode.LIGHT.ordinal -> ThemeMode.LIGHT
                ThemeMode.DARK.ordinal -> ThemeMode.DARK
                else -> ThemeMode.DARK
            }
        }
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.ordinal
        }
    }
}
