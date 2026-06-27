package com.example.smartnotes.ui.screens.lectures

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.db.entity.LectureEntity
import com.example.smartnotes.data.repository.LectureRepository
import com.example.smartnotes.recording.SpeechRecognizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    application: Application,
    private val lectureRepo: LectureRepository,
    private val speechManager: SpeechRecognizerManager
) : AndroidViewModel(application) {

    private var lectureId: Long = 0
    private var startTime = 0L

    val partialText: StateFlow<String> = speechManager.partialText
    val finalText: StateFlow<String> = speechManager.finalText
    val isListening: StateFlow<Boolean> = speechManager.isListening

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs

    private val _wordCount = MutableStateFlow(0)
    val wordCount: StateFlow<Int> = _wordCount

    fun initialize(topicId: Long, lectureId: Long) {
        this.lectureId = lectureId
        startTime = System.currentTimeMillis()

        val filePath = getTranscriptFilePath(lectureId)

        viewModelScope.launch {
            speechManager.finalText.collect { text ->
                val words = text.trim().split("\\s+".toRegex()).size
                _wordCount.value = words
            }
        }

        speechManager.startListening(getApplication(), filePath)

        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _elapsedMs.value = System.currentTimeMillis() - startTime
                if (_elapsedMs.value % 30000 < 1000) {
                    updateLectureProgress()
                }
            }
        }
    }

    fun pause() {
        speechManager.stopListening()
    }

    fun resume() {
        val filePath = getTranscriptFilePath(lectureId)
        speechManager.startListening(getApplication(), filePath)
    }

    fun finish() {
        speechManager.stopListening()
        updateLectureProgress(final = true)
    }

    fun cancel() {
        speechManager.cancel()
        viewModelScope.launch {
            lectureRepo.deleteById(lectureId)
        }
    }

    private fun updateLectureProgress(final: Boolean = false) {
        viewModelScope.launch {
            val lecture = lectureRepo.getById(lectureId) ?: return@launch
            val transcript = speechManager.getFullTranscript()
            val words = transcript.trim().split("\\s+".toRegex()).size

            lectureRepo.update(
                lecture.copy(
                    durationMs = System.currentTimeMillis() - startTime,
                    wordCount = words,
                    status = if (final) "TRANSCRIBED" else lecture.status
                )
            )
        }
    }

    private fun getTranscriptFilePath(lectureId: Long): String {
        val dir = getApplication<Application>().filesDir
        return "${dir.absolutePath}/transcripts/lecture_$lectureId.txt"
    }
}
