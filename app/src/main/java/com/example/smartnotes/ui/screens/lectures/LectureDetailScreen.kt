package com.example.smartnotes.ui.screens.lectures

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LectureDetailScreen(
    lectureId: Long,
    onBack: () -> Unit,
    viewModel: LectureDetailViewModel = hiltViewModel()
) {
    val lecture by viewModel.lecture.collectAsState()
    val aiSummary by viewModel.aiSummary.collectAsState()
    val transcription by viewModel.transcription.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lecture?.title ?: "Detalhes da Aula") },
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
            if (aiSummary == null && lecture?.status != "SUMMARY_DONE") {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.sendToAi() },
                    icon = {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        }
                    },
                    text = { Text("Processar com IA") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Transcrição",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (transcription.isNotEmpty()) {
                    Card {
                        Text(
                            text = transcription,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Nenhuma transcrição disponível.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            aiSummary?.let { summary ->
                item {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Resumo da IA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card {
                        Text(
                            text = summary.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                item {
                    val topics = parseJsonList(summary.keyTopics)
                    if (topics.isNotEmpty()) {
                        Text(
                            text = "Tópicos Importantes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        topics.forEach { topic ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    val questions = parseJsonList(summary.reviewQuestions)
                    if (questions.isNotEmpty()) {
                        Text(
                            text = "Questões para Revisão",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        questions.forEachIndexed { index, question ->
                            Card(
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. $question",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    val keywords = parseJsonList(summary.keywords)
                    if (keywords.isNotEmpty()) {
                        Text(
                            text = "Palavras-chave",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            keywords.forEach { keyword ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(keyword) }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

private fun parseJsonList(json: String): List<String> {
    return try {
        val type = object : TypeToken<List<String>>() {}.type
        Gson().fromJson(json, type)
    } catch (e: Exception) {
        emptyList()
    }
}
