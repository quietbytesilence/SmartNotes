---
feature: smartnotes-app
status: delivered
specs:
  - docs/compose/specs/2026-06-26-smartnotes-design.md
plans:
  - docs/compose/plans/2026-06-26-smartnotes-implementation.md
branch: main
commits: (no git history)
---

# SmartNotes — Final Report

## What Was Built

SmartNotes é um aplicativo Android para estudantes que organiza aulas em disciplina → tópico → aula, grava e transcreve fala em tempo real usando o SpeechRecognizer nativo do Android, e envia transcrições para APIs de IA configuráveis (OpenAI-compatible). Tudo fica salvo localmente via Room SQLite.

## Architecture

**Padrão:** Simplified MVVM (ViewModel → Repository → Room/Retrofit/DataStore)
**DI:** Hilt
**UI:** Jetpack Compose + Material Design 3
**Navegação:** Navigation Compose (Single Activity)
**Áudio:** SpeechRecognizer nativo com partial results em tempo real + foreground service
**Rede:** Retrofit + OkHttp (cliente OpenAI-compatible, suporta qualquer endpoint)
**Prefs:** DataStore (tema escuro/claro/auto, configuração de API)
**Backend:** Room SQLite (4 tabelas: disciplines, topics, lectures, ai_summaries)

### Pacotes (48 arquivos .kt, 2764 linhas)

- `data/` — Entidades Room (4), DAOs (4), Database, DataStore (2), Remote models (2), ApiService, Repositories (4)
- `di/` — DatabaseModule, NetworkModule, RepositoryModule (Hilt)
- `ui/` — Theme (Color/Type/Theme Material3), Navigation (Routes/NavGraph), Components (4 cards + SearchBar), Screens (8 screens + 5 ViewModels)
- `recording/` — SpeechRecognizerManager (singleton), RecordingService (foreground)
- `ai/` — AiService (prompt construction + API call + JSON parsing)

### Fluxo de Dados

1. Usuário cria Disciplina → Topic → Aula
2. RecordingScreen → SpeechRecognizerManager transcreve em tempo real → salva em arquivo no filesDir
3. Ao finalizar, status da aula vira TRANSCRIBED
4. LectureDetailScreen → botão "Processar com IA" → AiService envia transcrição para endpoint configurado
5. Resposta JSON parseada → salva em ai_summaries → status SUMMARY_DONE

### Design Decisions

- **Simplified MVVM sem UseCases**: Menos abstrações para MVP, mais rápido de construir. ViewModel → Repository diretamente.
- **SpeechRecognizer nativo vs API cloud**: Gratuito, funciona offline se pacote de idioma baixado, streaming em tempo real.
- **Single Activity + Navigation Compose**: Padrão moderno Android, navegação declarativa.
- **Arquivo + Room para transcrição**: Transcrição salva em arquivo de texto no filesDir para preservação, LectureEntity no DB para metadados.
- **API dinâmica via endpoint configurável**: Suporta OpenAI, OpenRouter, Gemini, DeepSeek, Ollama, LM Studio, vLLM — qualquer endpoint compatível com formato Chat Completions.

## Usage

### Fluxo Principal
1. Abrir app → tela "Disciplinas"
2. FAB "+" → criar disciplina (nome + cor)
3. Tocar disciplina → tela "Tópicos"
4. FAB "+" → criar tópico
5. Tocar tópico → tela "Aulas"
6. FAB "Iniciar Aula" → tela de gravação
7. Falar → transcrição em tempo real → "Finalizar"
8. Tocar aula → LectureDetail → "Processar com IA"
9. Resumo, tópicos, questões de revisão exibidos

### Configuração
Settings (ícone engrenagem) → configurar:
- Endpoint da API (ex: `https://api.openai.com/v1/chat/completions`)
- API Key
- Modelo, temperatura, max tokens
- Prompt personalizado
- Botão "Testar Conexão"

### Tema
Auto / Claro / Escuro via segment buttons nas configurações.

## Verification

Build manual do ambiente Android não disponível (sem Android SDK).
Verificação estrutural concluída:
- 48 arquivos Kotlin criados, 2764 linhas de código
- Estrutura de pacotes corresponde ao spec
- Dependências do Gradle declaradas (Compose BOM 2024.02, Hilt 2.50, Room 2.6.1, Retrofit 2.9.0)
- Room entities com FKs e índices
- Navigation Compose com 5 rotas (disciplines, topics, lectures, recording, lecture-detail, settings)
- Hilt modules para Database, Network, Repository
- Permissões declaradas no manifest (RECORD_AUDIO, INTERNET, FOREGROUND_SERVICE)
- SpeechRecognizer com partial results configurado

### Para Compilar
```bash
# Requer: Android SDK 34, Java 17+, Gradle 8.5
export ANDROID_HOME=/path/to/sdk
./gradlew assembleDebug
```

## Journey Log

- [dead end] `TopicListViewModel` tentou usar `DisciplineRepository.getById()` (suspend) como Flow direct — corrigido removendo a propriedade não utilizada.
- [lesson] Uso de `AndroidViewModel` necessário em RecordingViewModel e LectureDetailViewModel para acesso ao `filesDir` e `Application` context via Hilt.
- [lesson] `getConfig()` do DataStore retorna `Flow<ApiConfig>` — necessário `.first()` para obter valor único em `AiService.processLecture()`.

## Source Materials

| File | Role | Notes |
|------|------|-------|
| docs/compose/specs/2026-06-26-smartnotes-design.md | Design spec | 12 seções, aprovado pelo usuário |
| docs/compose/plans/2026-06-26-smartnotes-implementation.md | Implementation plan | 15 tarefas, código completo |
