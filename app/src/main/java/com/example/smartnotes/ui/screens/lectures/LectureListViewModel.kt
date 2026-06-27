package com.example.smartnotes.ui.screens.lectures

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.db.entity.LectureEntity
import com.example.smartnotes.data.repository.LectureRepository
import com.example.smartnotes.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lectureRepo: LectureRepository,
    private val topicRepo: TopicRepository
) : ViewModel() {

    val topicId: Long = savedStateHandle["topicId"] ?: -1L

    val lectures: StateFlow<List<LectureEntity>> = lectureRepo.getByTopic(topicId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startNewLecture(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val lecture = LectureEntity(
                topicId = topicId,
                title = "Aula ${System.currentTimeMillis()}"
            )
            val id = lectureRepo.insert(lecture)
            onStarted(id)
        }
    }
}
