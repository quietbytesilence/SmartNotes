package com.example.smartnotes.data.remote

import com.example.smartnotes.data.remote.model.ChatRequest
import com.example.smartnotes.data.remote.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {
    @POST("/v1/chat/completions")
    suspend fun chatCompletion(@Body request: ChatRequest): Response<ChatResponse>
}
