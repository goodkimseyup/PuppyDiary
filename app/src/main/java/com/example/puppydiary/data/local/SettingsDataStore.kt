package com.example.puppydiary.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    val openAIApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[OPENAI_API_KEY] ?: ""
        }

    suspend fun getOpenAIApiKey(): String {
        return context.dataStore.data.map { it[OPENAI_API_KEY] ?: "" }.first()
    }

    suspend fun setOpenAIApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENAI_API_KEY] = apiKey
        }
    }
}
