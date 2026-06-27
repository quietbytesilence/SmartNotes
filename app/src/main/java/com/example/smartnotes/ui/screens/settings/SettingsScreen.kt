package com.example.smartnotes.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val apiConfig by viewModel.apiConfig.collectAsState()
    val testResult by viewModel.testResult.collectAsState()

    var endpoint by remember(apiConfig) { mutableStateOf(apiConfig.endpoint) }
    var apiKey by remember(apiConfig) { mutableStateOf(apiConfig.apiKey) }
    var model by remember(apiConfig) { mutableStateOf(apiConfig.model) }
    var temperature by remember(apiConfig) { mutableStateOf(apiConfig.temperature.toString()) }
    var maxTokens by remember(apiConfig) { mutableStateOf(apiConfig.maxTokens.toString()) }
    var customPrompt by remember(apiConfig) { mutableStateOf(apiConfig.customPrompt) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Aparência", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tema", modifier = Modifier.weight(1f))
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = themeMode == "system",
                        onClick = { viewModel.setTheme("system") },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) { Text("Auto") }
                    SegmentedButton(
                        selected = themeMode == "light",
                        onClick = { viewModel.setTheme("light") },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) { Text("Claro") }
                    SegmentedButton(
                        selected = themeMode == "dark",
                        onClick = { viewModel.setTheme("dark") },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) { Text("Escuro") }
                }
            }

            HorizontalDivider()
            Text("API de IA", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = endpoint,
                onValueChange = { endpoint = it },
                label = { Text("Endpoint") },
                placeholder = { Text("https://api.openai.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Modelo") },
                    placeholder = { Text("gpt-3.5-turbo") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temp.") },
                    singleLine = true,
                    modifier = Modifier.width(80.dp)
                )
                OutlinedTextField(
                    value = maxTokens,
                    onValueChange = { maxTokens = it },
                    label = { Text("Tokens") },
                    singleLine = true,
                    modifier = Modifier.width(100.dp)
                )
            }

            OutlinedTextField(
                value = customPrompt,
                onValueChange = { customPrompt = it },
                label = { Text("Prompt personalizado") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        viewModel.testConnection(
                            apiConfig.copy(
                                endpoint = endpoint,
                                apiKey = apiKey,
                                model = model,
                                temperature = temperature.toFloatOrNull() ?: 0.7f,
                                maxTokens = maxTokens.toIntOrNull() ?: 4096,
                                customPrompt = customPrompt
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Testar Conexão")
                }

                Button(
                    onClick = {
                        viewModel.saveApiConfig(
                            ApiConfig(
                                endpoint = endpoint,
                                apiKey = apiKey,
                                model = model,
                                temperature = temperature.toFloatOrNull() ?: 0.7f,
                                maxTokens = maxTokens.toIntOrNull() ?: 4096,
                                customPrompt = customPrompt
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar")
                }
            }

            testResult?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (it.startsWith("Conexão"))
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
