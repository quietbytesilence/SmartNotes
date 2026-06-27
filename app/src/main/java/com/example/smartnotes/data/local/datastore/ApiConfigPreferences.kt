package com.example.smartnotes.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.apiConfigStore: DataStore<Preferences> by preferencesDataStore(name = "api_config")

data class ApiConfig(
    val endpoint: String = "https://api.openai.com",
    val apiKey: String = "",
    val model: String = "gpt-3.5-turbo",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val customPrompt: String = ""
)

class ApiConfigPreferences(private val context: Context) {
    companion object {
        private val ENDPOINT_KEY = stringPreferencesKey("api_endpoint")
        private val API_KEY = stringPreferencesKey("api_key")
        private val MODEL_KEY = stringPreferencesKey("model")
        private val TEMPERATURE_KEY = floatPreferencesKey("temperature")
        private val MAX_TOKENS_KEY = intPreferencesKey("max_tokens")
        private val CUSTOM_PROMPT_KEY = stringPreferencesKey("custom_prompt")
    }

    val configFlow: Flow<ApiConfig> = context.apiConfigStore.data.map { prefs ->
        ApiConfig(
            endpoint = prefs[ENDPOINT_KEY] ?: "https://api.openai.com",
            apiKey = prefs[API_KEY] ?: "",
            model = prefs[MODEL_KEY] ?: "gpt-3.5-turbo",
            temperature = prefs[TEMPERATURE_KEY] ?: 0.7f,
            maxTokens = prefs[MAX_TOKENS_KEY] ?: 4096,
            customPrompt = prefs[CUSTOM_PROMPT_KEY] ?: ""
        )
    }

    suspend fun save(config: ApiConfig) {
        context.apiConfigStore.edit { prefs ->
            prefs[ENDPOINT_KEY] = config.endpoint
            prefs[API_KEY] = config.apiKey
            prefs[MODEL_KEY] = config.model
            prefs[TEMPERATURE_KEY] = config.temperature
            prefs[MAX_TOKENS_KEY] = config.maxTokens
            prefs[CUSTOM_PROMPT_KEY] = config.customPrompt
        }
    }
}
