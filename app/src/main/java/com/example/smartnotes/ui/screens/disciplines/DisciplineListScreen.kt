package com.example.smartnotes.ui.screens.disciplines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartnotes.ui.components.DisciplineCard
import com.example.smartnotes.ui.components.SmartSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplineListScreen(
    onDisciplineClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: DisciplineListViewModel = hiltViewModel()
) {
    val disciplines by viewModel.disciplines.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disciplinas") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova Disciplina") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SmartSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(disciplines, key = { it.discipline.id }) { item ->
                    DisciplineCard(
                        name = item.discipline.name,
                        color = item.discipline.color,
                        topicCount = item.topicCount,
                        lectureCount = item.lectureCount,
                        onClick = { onDisciplineClick(item.discipline.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateDisciplineDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, color, icon ->
                viewModel.createDiscipline(name, color, icon)
                showCreateDialog = false
            }
        )
    }
}
