package com.example.smartnotes.ui.screens.lectures

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    topicId: Long,
    lectureId: Long,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val partialText by viewModel.partialText.collectAsState()
    val finalText by viewModel.finalText.collectAsState()
    val elapsedMs by viewModel.elapsedMs.collectAsState()
    val wordCount by viewModel.wordCount.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initialize(topicId, lectureId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gravando Aula") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(elapsedMs),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isListening) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$wordCount palavras",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (finalText.isNotEmpty()) {
                        Text(
                            text = finalText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isListening && partialText.isNotEmpty()) {
                        Text(
                            text = partialText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (finalText.isEmpty() && partialText.isEmpty()) {
                        Text(
                            text = "Fale algo para começar a transcrição...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = {
                    viewModel.cancel()
                    onCancel()
                }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar")
                }
                OutlinedButton(onClick = {
                    if (isPaused) viewModel.resume() else viewModel.pause()
                    isPaused = !isPaused
                }) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPaused) "Continuar" else "Pausar")
                }
                Button(onClick = {
                    viewModel.finish()
                    onFinish()
                }) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar")
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
