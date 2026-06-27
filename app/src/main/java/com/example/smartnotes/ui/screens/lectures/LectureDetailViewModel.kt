package com.example.smartnotes.ui.screens.lectures

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.ai.AiService
import com.example.smartnotes.data.local.db.entity.AISummaryEntity
import com.example.smartnotes.data.local.db.entity.LectureEntity
import com.example.smartnotes.data.repository.AiRepository
import com.example.smartnotes.data.repository.LectureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LectureDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lectureRepo: LectureRepository,
    private val aiRepo: AiRepository,
    private val aiService: AiService,
    private val application: Application
) : AndroidViewModel(application) {

    val lectureId: Long = savedStateHandle["lectureId"] ?: -1L

    val lecture: StateFlow<LectureEntity?> = lectureRepo.getByIdFlow(lectureId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val aiSummary: StateFlow<AISummaryEntity?> = aiRepo.getSummaryByLectureFlow(lectureId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription

    init {
        loadTranscription()
    }

    private fun loadTranscription() {
        viewModelScope.launch {
            val filePath = "${application.filesDir.absolutePath}/transcripts/lecture_$lectureId.txt"
            val text = try {
                File(filePath).readText()
            } catch (e: Exception) { "" }
            _transcription.value = text
        }
    }

    fun sendToAi() {
        viewModelScope.launch {
            _isProcessing.value = true
            val transcript = _transcription.value
            if (transcript.isBlank()) {
                _isProcessing.value = false
                return@launch
            }
            aiService.processLecture(lectureId, transcript)
            _isProcessing.value = false
        }
    }
}
