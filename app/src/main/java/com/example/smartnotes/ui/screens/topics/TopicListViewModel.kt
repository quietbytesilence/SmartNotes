package com.example.smartnotes.ui.screens.topics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.db.entity.TopicEntity
import com.example.smartnotes.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicWithStats(
    val topic: TopicEntity,
    val lectureCount: Int = 0
)

@HiltViewModel
class TopicListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val topicRepo: TopicRepository
) : ViewModel() {

    val disciplineId: Long = savedStateHandle["disciplineId"] ?: -1L

    val topics: StateFlow<List<TopicWithStats>> = topicRepo.getByDiscipline(disciplineId)
        .map { list ->
            list.map { entity ->
                TopicWithStats(topic = entity)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createTopic(name: String) {
        viewModelScope.launch {
            topicRepo.insert(
                TopicEntity(disciplineId = disciplineId, name = name)
            )
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            topicRepo.delete(topic)
        }
    }
}
