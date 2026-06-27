package com.example.smartnotes.ui.screens.lectures

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartnotes.ui.components.LectureCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureListScreen(
    topicId: Long,
    onLectureClick: (Long) -> Unit,
    onStartRecording: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: LectureListViewModel = hiltViewModel()
) {
    val lectures by viewModel.lectures.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aulas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startNewLecture(onStartRecording) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Iniciar Aula") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lectures, key = { it.id }) { lecture ->
                LectureCard(
                    title = lecture.title,
                    date = lecture.date,
                    durationMs = lecture.durationMs,
                    wordCount = lecture.wordCount,
                    status = lecture.status,
                    onClick = { onLectureClick(lecture.id) }
                )
            }
        }
    }
}
