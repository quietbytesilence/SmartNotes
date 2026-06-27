package com.example.smartnotes.data.remote.model

data class ChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val error: ErrorDetail?
) {
    data class Choice(
        val index: Int,
        val message: Message
    ) {
        data class Message(
            val role: String,
            val content: String
        )
    }

    data class ErrorDetail(
        val message: String?,
        val type: String?
    )
}
