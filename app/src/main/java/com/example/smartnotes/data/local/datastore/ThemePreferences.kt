package com.example.smartnotes.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    val themeFlow: Flow<String> = context.themeStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "system"
    }

    suspend fun setTheme(mode: String) {
        context.themeStore.edit { prefs ->
            prefs[THEME_KEY] = mode
        }
    }
}
