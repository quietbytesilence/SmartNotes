package com.example.smartnotes.ai

import com.example.smartnotes.data.local.db.entity.AISummaryEntity
import com.example.smartnotes.data.remote.AiApiService
import com.example.smartnotes.data.remote.model.ChatRequest
import com.example.smartnotes.data.repository.AiRepository
import com.example.smartnotes.data.repository.LectureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiService @Inject constructor(
    private val aiRepository: AiRepository,
    private val lectureRepository: LectureRepository,
    private val okHttpClient: OkHttpClient
) {

    private val defaultPrompt = """
Você é um assistente especializado em transformar transcrições de aulas em material de estudo.

Analise a transcrição abaixo.

Retorne APENAS um objeto JSON válido (sem formatação markdown, sem ```, sem texto extra) com os seguintes campos:
{
  "summary": "resumo detalhado da aula",
  "keyTopics": ["tópico 1", "tópico 2"],
  "keyConcepts": ["conceito 1", "conceito 2"],
  "keywords": ["palavra1", "palavra2"],
  "reviewQuestions": ["pergunta 1?", "pergunta 2?"]
}

Transcrição:
{transcription}
    """.trimIndent()

    suspend fun processLecture(lectureId: Long, transcription: String): Result<AISummaryEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val config = aiRepository.getConfig().first()

                val baseUrl = config.endpoint.let { endpoint ->
                    if (endpoint.endsWith("/v1/chat/completions")) {
                        endpoint.substringBefore("/v1/chat/completions") + "/"
                    } else if (!endpoint.endsWith("/")) {
                        "$endpoint/"
                    } else endpoint
                }

                val extendedClient = okHttpClient.newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${config.apiKey}")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .build()

                val apiService = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(extendedClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AiApiService::class.java)

                val promptText = config.customPrompt.ifBlank { defaultPrompt }
                    .replace("{transcription}", transcription)

                val request = ChatRequest(
                    model = config.model,
                    messages = listOf(
                        ChatRequest.Message(role = "system", content = "Você é um assistente especializado em material de estudo."),
                        ChatRequest.Message(role = "user", content = promptText)
                    ),
                    temperature = config.temperature,
                    maxTokens = config.maxTokens
                )

                val response = apiService.chatCompletion(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.choices?.firstOrNull()?.message?.content ?: ""
                    val aiSummary = parseAiResponse(lectureId, content)

                    aiRepository.saveSummary(aiSummary)

                    val lecture = lectureRepository.getById(lectureId)
                    lecture?.let {
                        lectureRepository.update(it.copy(status = "SUMMARY_DONE"))
                    }

                    Result.success(aiSummary)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    Result.failure(Exception("API error: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseAiResponse(lectureId: Long, jsonContent: String): AISummaryEntity {
        val cleaned = jsonContent
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val gson = com.google.gson.Gson()
            val json = gson.fromJson(cleaned, Map::class.java) as Map<String, Any>

            AISummaryEntity(
                lectureId = lectureId,
                summary = json["summary"] as? String ?: "",
                keyTopics = gson.toJson(json["keyTopics"] ?: emptyList<String>()),
                keyConcepts = gson.toJson(json["keyConcepts"] ?: emptyList<String>()),
                keywords = gson.toJson(json["keywords"] ?: emptyList<String>()),
                reviewQuestions = gson.toJson(json["reviewQuestions"] ?: emptyList<String>()),
                rawResponse = jsonContent
            )
        } catch (e: Exception) {
            AISummaryEntity(
                lectureId = lectureId,
                summary = cleaned,
                rawResponse = jsonContent
            )
        }
    }
}
