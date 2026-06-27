package com.example.smartnotes.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.datastore.ApiConfig
import com.example.smartnotes.data.local.datastore.ApiConfigPreferences
import com.example.smartnotes.data.local.datastore.ThemePreferences
import com.example.smartnotes.data.remote.AiApiService
import com.example.smartnotes.data.remote.model.ChatRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val themePrefs: ThemePreferences,
    private val apiConfigPrefs: ApiConfigPreferences
) : AndroidViewModel(application) {

    val themeMode: StateFlow<String> = themePrefs.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val apiConfig: StateFlow<ApiConfig> = apiConfigPrefs.configFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiConfig())

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult

    fun setTheme(mode: String) {
        viewModelScope.launch { themePrefs.setTheme(mode) }
    }

    fun saveApiConfig(config: ApiConfig) {
        viewModelScope.launch { apiConfigPrefs.save(config) }
    }

    fun testConnection(config: ApiConfig) {
        viewModelScope.launch {
            _testResult.value = null
            try {
                val baseUrl = config.endpoint.let { endpoint ->
                    if (endpoint.endsWith("/v1/chat/completions")) {
                        endpoint.substringBefore("/v1/chat/completions") + "/"
                    } else if (!endpoint.endsWith("/")) {
                        "$endpoint/"
                    } else endpoint
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${config.apiKey}")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                    .build()

                val api = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AiApiService::class.java)

                val request = ChatRequest(
                    model = config.model,
                    messages = listOf(
                        ChatRequest.Message(role = "user", content = "Teste de conexão. Responda apenas 'OK'.")
                    ),
                    temperature = 0.1f,
                    maxTokens = 10
                )

                val response = api.chatCompletion(request)
                if (response.isSuccessful) {
                    _testResult.value = "Conexão bem-sucedida!"
                } else {
                    _testResult.value = "Erro: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _testResult.value = "Falha: ${e.localizedMessage ?: "Erro desconhecido"}"
            }
        }
    }
}
