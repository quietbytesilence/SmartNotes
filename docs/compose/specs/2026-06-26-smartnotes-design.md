# SmartNotes: Aplicativo de Transcrição Inteligente de Aulas

> [!NOTE]
> This document may not reflect the current implementation.
> See the final report for up-to-date state:
> [Final Report](../reports/smartnotes-app.md)

## [S1] Resumo

Aplicativo Android para estudantes que grava aulas, transcreve fala em tempo real (SpeechRecognizer nativo), envia a transcrição para API de IA configurável (OpenAI-compatible), e organiza tudo em disciplina → tópico → aula.

## [S2] Arquitetura

**Padrão:** Simplified MVVM (ViewModel → Repository → Room/Retrofit/DataStore)
**DI:** Hilt
**UI:** Jetpack Compose + Material Design 3
**Navegação:** Navigation Compose (Single Activity)
**Áudio:** SpeechRecognizer nativo + MediaRecorder (fallback)
**Rede:** Retrofit + OkHttp (cliente OpenAI-compatible)
**Prefs:** DataStore (tema, API config)
**Background:** WorkManager (envio pendente à IA)

## [S3] Estrutura de Pacotes

```
com.example.smartnotes/
├── SmartNotesApp.kt              (@HiltAndroidApp)
├── MainActivity.kt               (Single Activity, setContent)
├── di/
│   ├── DatabaseModule.kt         (Room provider)
│   ├── NetworkModule.kt          (Retrofit + OkHttp)
│   └── RepositoryModule.kt       (Repository bindings)
├── data/
│   ├── local/db/
│   │   ├── AppDatabase.kt
│   │   ├── entity/
│   │   │   ├── DisciplineEntity.kt
│   │   │   ├── TopicEntity.kt
│   │   │   ├── LectureEntity.kt
│   │   │   └── AISummaryEntity.kt
│   │   └── dao/
│   │       ├── DisciplineDao.kt
│   │       ├── TopicDao.kt
│   │       ├── LectureDao.kt
│   │       └── AISummaryDao.kt
│   ├── local/datastore/
│   │   ├── ThemePreferences.kt
│   │   └── ApiConfigPreferences.kt
│   ├── remote/
│   │   ├── AiApiService.kt       (Retrofit interface)
│   │   └── model/
│   │       ├── ChatRequest.kt
│   │       └── ChatResponse.kt
│   └── repository/
│       ├── DisciplineRepository.kt
│       ├── TopicRepository.kt
│       ├── LectureRepository.kt
│       └── AiRepository.kt
├── ui/
│   ├── navigation/
│   │   └── NavGraph.kt
│   │   └── Routes.kt
│   ├── theme/
│   │   ├── Theme.kt              (Material 3, Dark/Light/Auto)
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── screens/
│   │   ├── disciplines/
│   │   │   ├── DisciplineListScreen.kt
│   │   │   ├── DisciplineListViewModel.kt
│   │   │   └── CreateDisciplineDialog.kt
│   │   ├── topics/
│   │   │   ├── TopicListScreen.kt
│   │   │   ├── TopicListViewModel.kt
│   │   │   └── CreateTopicDialog.kt
│   │   ├── lectures/
│   │   │   ├── LectureListScreen.kt
│   │   │   ├── LectureListViewModel.kt
│   │   │   ├── RecordingScreen.kt
│   │   │   ├── RecordingViewModel.kt
│   │   │   ├── LectureDetailScreen.kt
│   │   │   └── LectureDetailViewModel.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt
│   │   │   └── SettingsViewModel.kt
│   │   └── ai/
│   │       └── AiResultScreen.kt
│   └── components/
│       ├── SearchBar.kt
│       ├── DisciplineCard.kt
│       ├── TopicCard.kt
│       └── LectureCard.kt
├── recording/
│   ├── SpeechRecognizerManager.kt
│   └── AudioRecorderManager.kt
└── ai/
    └── AiService.kt              (monta prompt, chama API, persiste)
```

## [S4] Modelo de Dados (Room)

### discipline_entity
| Coluna | Tipo | Restrição |
|--------|------|-----------|
| id | Long | PK auto |
| name | String | NOT NULL |
| color | Int | NOT NULL |
| icon | String | NOT NULL |
| createdAt | Long | |
| updatedAt | Long | |

### topic_entity
| Coluna | Tipo | Restrição |
|--------|------|-----------|
| id | Long | PK auto |
| disciplineId | Long | FK → discipline(id) ON DELETE CASCADE |
| name | String | NOT NULL |
| createdAt | Long | |
| updatedAt | Long | |

### lecture_entity
| Coluna | Tipo | Restrição |
|--------|------|-----------|
| id | Long | PK auto |
| topicId | Long | FK → topic(id) ON DELETE CASCADE |
| title | String | |
| date | Long | timestamp início |
| durationMs | Long | duração em ms |
| wordCount | Int | |
| status | String | RECORDING / TRANSCRIBED / SENT_TO_AI / SUMMARY_DONE |
| transcriptionPath | String | caminho arquivo transcrição |
| createdAt | Long | |
| updatedAt | Long | |

### ai_summary_entity
| Coluna | Tipo | Restrição |
|--------|------|-----------|
| id | Long | PK auto |
| lectureId | Long | FK → lecture(id) ON DELETE CASCADE |
| summary | String | resumo detalhado |
| keyTopics | String | JSON array |
| keyConcepts | String | JSON array |
| keywords | String | JSON array |
| reviewQuestions | String | JSON array |
| flashcards | String | JSON array |
| rawResponse | String | resposta completa da IA |
| createdAt | Long | |

### Queries de Pesquisa (FTS)
Tabela virtual FTS4/5 para busca full-text sobre transcrições e resumos.

## [S5] Integração com IA

### Formato da API
Endpoint compatível com OpenAI Chat Completions:
```
POST {endpoint}/v1/chat/completions
Headers: Authorization: Bearer {apiKey}
Body: {
  "model": "{model}",
  "messages": [...],
  "temperature": {temp},
  "max_tokens": {maxTokens}
}
```

### Prompt Padrão
```
Você é um assistente especializado em transformar transcrições de aulas em material de estudo.

Analise a transcrição abaixo.

Retorne:
* resumo detalhado;
* tópicos importantes;
* conceitos principais;
* exemplos encontrados;
* definições;
* dúvidas que podem aparecer em prova;
* questões para revisão;
* palavras-chave;
* pontos que merecem revisão.

Transcrição:
{transcription}
```

### Parsing da Resposta
Resposta estruturada como JSON:
```json
{
  "summary": "...",
  "keyTopics": ["..."],
  "keyConcepts": ["..."],
  "keywords": ["..."],
  "reviewQuestions": ["..."]
}
```

## [S6] Telas (UI/UX Resumo)

1. **DisciplinaListScreen** — Grid de cards coloridos, cada card: nome, cor, ícone, qtd aulas. FAB "+", SearchBar no topo.
2. **TopicListScreen** — Lista de tópicos da disciplina, FAB "+", data atualização.
3. **LectureListScreen** — Lista de aulas do tópico, card: título, data, duração, palavras, status IA. FAB "Iniciar Aula".
4. **RecordingScreen** — Timer, indicador áudio animado, contagem palavras, transcrição rolável em tempo real. Botões: Pausar, Finalizar, Cancelar.
5. **LectureDetailScreen** — Transcrição completa + resultado IA (resumo, tópicos, conceitos, questões).
6. **SettingsScreen** — Tema, API endpoint, key, modelo, temperatura, max tokens, prompt customizado, Testar Conexão, Salvar.
7. **AiResultScreen** — Exibição formatada do resultado da IA (cards colapsáveis).

## [S7] Configurações (DataStore)

- tema: system / light / dark
- apiEndpoint: String
- apiKey: String (criptografada)
- model: String
- temperature: Float
- maxTokens: Int
- customPrompt: String

## [S8] Gravação e Transcrição

1. `SpeechRecognizerManager` — wrapper do `SpeechRecognizer` do Android SDK
2. Inicia listening com `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` + `EXTRA_PARTIAL_RESULTS`
3. Callback `onPartialResults` → atualiza transcrição na UI e salva no arquivo
4. Callback `onResults` → transcrição final
5. A cada N segundos (ex: 30s), salva a transcrição parcial no arquivo e no DB
6. `AudioRecorderManager` — grava o áudio em paralelo como fallback/ref (arquivo .mp4/.3gp)

## [S9] Notificação Persistente

Durante gravação ativa: foreground service com notificação "Gravando aula..." com ação de parar. Permite gravação em segundo plano.

## [S10] Configuração do Projeto

- compileSdk: 34
- minSdk: 26
- Kotlin 1.9+
- Gradle KTS
- Hilt, Room, Compose BOM, Retrofit, DataStore, Navigation Compose

Dependências principais:
```
compose-bom:2024.02.00
hilt:2.50
room:2.6.1
retrofit:2.9.0
okhttp:4.12.0
datastore-preferences:1.0.0
navigation-compose:2.7.7
lifecycle-viewmodel-compose:2.7.0
material3 (via BOM)
```

## [S11] Tratamento de Erros

- Timeout configurável nas chamadas HTTP
- Retry: 3 tentativas com backoff exponencial
- Se API falhar: status SENT_TO_AI não é atualizado, fila pendente
- Se gravação falhar: transcrição já salva parcialmente no arquivo
- WorkManager para re-tentar envios falhos

## [S12] MVP — O que NÃO está incluso (futuro)

- Exportação PDF/Markdown/TXT/JSON/DOCX
- Backup local/restauração
- Estatísticas
- Flashcards automáticos
- Mapas mentais
- Anexos (imagens, PDFs)
- Sincronização Google Drive/OneDrive
- Modo offline (transcrição offline já funciona; processamento IA diferido)
