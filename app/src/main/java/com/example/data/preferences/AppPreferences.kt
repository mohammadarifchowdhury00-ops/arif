package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "arif_journal_prefs")

class AppPreferences(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val showRomajiKey = booleanPreferencesKey("show_romaji")
    private val showFuriganaKey = booleanPreferencesKey("show_furigana")

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[darkModeKey] ?: false }
    val showRomaji: Flow<Boolean> = context.dataStore.data.map { it[showRomajiKey] ?: true }
    val showFurigana: Flow<Boolean> = context.dataStore.data.map { it[showFuriganaKey] ?: true }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[darkModeKey] = enabled }
    }

    suspend fun setShowRomaji(enabled: Boolean) {
        context.dataStore.edit { it[showRomajiKey] = enabled }
    }

    suspend fun setShowFurigana(enabled: Boolean) {
        context.dataStore.edit { it[showFuriganaKey] = enabled }
    }
}
