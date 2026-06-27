# SmartNotes

Aplicativo Android para transcrição inteligente de aulas com IA.

Organize seus estudos em **Disciplinas → Tópicos → Aulas**, grave e transcreva automaticamente, e receba resumos gerados por IA.

## Funcionalidades

- **Organização em cadernos**: Disciplinas → Tópicos → Aulas
- **Transcrição em tempo real**: usando o SpeechRecognizer nativo do Android
- **Notificação persistente**: continua gravando em segundo plano
- **IA configurável**: suporta OpenAI, OpenRouter, Gemini, DeepSeek, Ollama, LM Studio, vLLM
- **Resumo estruturado**: resumo, tópicos, conceitos, palavras-chave e questões de revisão
- **Tema escuro/claro/automático**: Material Design 3
- **Pesquisa**: busque por nome de disciplina
- **100% offline**: transcrição local, apenas IA requer internet

## Pré-requisitos

| Ferramenta | Versão |
|-----------|--------|
| Android Studio | Hedgehog (2023.1.1) ou superior |
| JDK | 17+ |
| Android SDK | API 34 |
| Gradle | 8.5 (via wrapper) |

## Como compilar o APK

### Opção 1: Android Studio (recomendado)

```bash
# 1. Clone o repositório
git clone https://github.com/quietbytesilence/SmartNotes.git
cd SmartNotes

# 2. Abra no Android Studio
#    File → Open → selecione a pasta SmartNotes

# 3. Aguarde o Gradle sync (baixará dependências)

# 4. Gere o APK de debug:
#    Build → Build Bundle(s) / APK(s) → Build APK(s)
#    APK em: app/build/outputs/apk/debug/app-debug.apk

# 5. (Opcional) APK de release:
#    Build → Generate Signed Bundle / APK → APK
#    Crie um keystore ou use existente
```

### Opção 2: Linha de comando

```bash
# 1. Clone
git clone https://github.com/quietbytesilence/SmartNotes.git
cd SmartNotes

# 2. Configure o Android SDK
#    Crie o arquivo local.properties com:
echo "sdk.dir=/caminho/para/Android/Sdk" > local.properties

# 3. Compile o APK de debug
./gradlew assembleDebug

# APK gerado em: app/build/outputs/apk/debug/app-debug.apk

# Para release (exige keystore):
./gradlew assembleRelease
# APK em: app/build/outputs/apk/release/app-release.apk
```

### Opção 3: Gerar APK universal (AAB)

```bash
./gradlew bundleDebug
# AAB em: app/build/outputs/bundle/debug/app-debug.aab
```

## Primeiros passos após instalar

1. **Abra o app** → tela de Disciplinas
2. **Crie uma disciplina**: FAB "+" → digite nome → escolha cor
3. **Toque na disciplina** → tela de Tópicos
4. **Crie um tópico**: FAB "+"
5. **Toque no tópico** → tela de Aulas
6. **Inicie uma aula**: FAB "Iniciar Aula"
7. **Grave**: fale → transcrição em tempo real → "Finalizar"
8. **Toque na aula** → "Processar com IA" (precisa configurar API)

## Configurar API de IA

Acesse **Configurações** (ícone engrenagem) e preencha:

- **Endpoint**: URL da API (ex: `https://api.openai.com/v1/chat/completions`)
- **API Key**: sua chave
- **Modelo**: `gpt-3.5-turbo`, `gemini-pro`, `deepseek-chat`, etc.
- **Temperatura**: 0.0-2.0 (0.7 padrão)
- **Max Tokens**: limite da resposta (4096 padrão)
- **Prompt personalizado**: opcional
- **Testar Conexão**: verifica se a API responde
- **Salvar**: persiste as configurações

### Exemplos de endpoints

| Provedor | Endpoint |
|----------|----------|
| OpenAI | `https://api.openai.com/v1/chat/completions` |
| OpenRouter | `https://openrouter.ai/api/v1/chat/completions` |
| Gemini (via OpenAI proxy) | `https://generativelanguage.googleapis.com/v1beta/openai/chat/completions` |
| DeepSeek | `https://api.deepseek.com/v1/chat/completions` |
| Ollama (local) | `http://192.168.1.100:11434/v1/chat/completions` |

## Estrutura do projeto

```
SmartNotes/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/                    # Recursos (ícones, strings, temas)
│       └── java/com/example/smartnotes/
│           ├── SmartNotesApp.kt         # @HiltAndroidApp
│           ├── MainActivity.kt          # Single Activity + Compose
│           ├── di/                      # Hilt modules
│           ├── data/
│           │   ├── local/db/            # Room (entities, DAOs, database)
│           │   ├── local/datastore/     # Preferences (tema, API config)
│           │   ├── remote/              # Retrofit API + models
│           │   └── repository/          # Repositories
│           ├── ui/
│           │   ├── theme/              # Material 3 (Color, Type, Theme)
│           │   ├── navigation/         # Routes + NavGraph
│           │   ├── components/         # Cards, SearchBar
│           │   └── screens/            # Telas + ViewModels
│           ├── recording/              # SpeechRecognizer + Service
│           └── ai/                     # AI client + prompt
├── docs/compose/                       # Documentação do design
└── build.gradle.kts                    # Projeto Gradle
```

## Stack tecnológica

- **Kotlin** — linguagem principal
- **Jetpack Compose + Material 3** — UI declarativa
- **Room** — SQLite local
- **Hilt** — injeção de dependência
- **Retrofit + OkHttp** — HTTP client
- **DataStore** — preferências
- **Navigation Compose** — navegação
- **Coroutines + StateFlow** — assíncrono
- **SpeechRecognizer** — transcrição nativa Android

## Licença

MIT
