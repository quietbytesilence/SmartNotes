package com.example.smartnotes.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    @SerializedName("max_tokens") val maxTokens: Int = 4096
) {
    data class Message(
        val role: String,
        val content: String
    )
}
