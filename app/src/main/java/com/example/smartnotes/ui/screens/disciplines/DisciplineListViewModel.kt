package com.example.smartnotes.ui.screens.disciplines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.db.entity.DisciplineEntity
import com.example.smartnotes.data.repository.DisciplineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DisciplineWithStats(
    val discipline: DisciplineEntity,
    val topicCount: Int = 0,
    val lectureCount: Int = 0
)

@HiltViewModel
class DisciplineListViewModel @Inject constructor(
    private val disciplineRepo: DisciplineRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val disciplines = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) disciplineRepo.getAll()
            else disciplineRepo.search(query)
        }
        .map { list ->
            list.map { entity ->
                DisciplineWithStats(
                    discipline = entity,
                    topicCount = 0,
                    lectureCount = 0
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun createDiscipline(name: String, color: Int, icon: String) {
        viewModelScope.launch {
            disciplineRepo.insert(
                DisciplineEntity(name = name, color = color, icon = icon)
            )
        }
    }

    fun deleteDiscipline(discipline: DisciplineEntity) {
        viewModelScope.launch {
            disciplineRepo.delete(discipline)
        }
    }
}
