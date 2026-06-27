#!/bin/bash
# ============================================================
# gerar_apk.sh — Gera o APK do SmartNotes automaticamente
# ============================================================
# Uso: bash gerar_apk.sh
# ============================================================
set -e

VERDE='\033[0;32m'; AMARELO='\033[1;33m'; VERMELHO='\033[0;31m'; RESET='\033[0m'
info()  { echo -e "${VERDE}[INFO]${RESET} $1"; }
warn()  { echo -e "${AMARELO}[AVISO]${RESET} $1"; }
erro()  { echo -e "${VERMELHO}[ERRO]${RESET} $1"; }

# ─── 1. Verificar Java ─────────────────────────────────────
info "Verificando Java..."
if ! command -v java &>/dev/null; then
    warn "Java não encontrado. Instale o JDK 17+ primeiro:"
    echo "  sudo apt install openjdk-17-jdk   # Debian/Ubuntu"
    echo "  sudo dnf install java-17-openjdk  # Fedora"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1 | sed 's/.*version "\([0-9]*\).*/\1/')
if [ "$JAVA_VER" -lt 17 ]; then
    erro "Java 17+ necessário. Versão atual: $(java -version 2>&1 | head -1)"
    exit 1
fi
info "Java OK ($(java -version 2>&1 | head -1))"

# ─── 2. Descobrir diretório do script ──────────────────────
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# ─── 3. Baixar Gradle Wrapper JAR (se faltar) ──────────────
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    info "Baixando Gradle Wrapper JAR..."
    WRAPPER_JAR="https://raw.githubusercontent.com/gradle/gradle/v8.5/gradle/wrapper/gradle-wrapper.jar"
    if command -v curl &>/dev/null; then
        curl -sL "$WRAPPER_JAR" -o gradle/wrapper/gradle-wrapper.jar
    elif command -v wget &>/dev/null; then
        wget -q "$WRAPPER_JAR" -O gradle/wrapper/gradle-wrapper.jar
    else
        # Fallback: baixar o Gradle completo e extrair só o JAR
        warn "Baixando Gradle completo para extrair o wrapper..."
        curl -sL "https://services.gradle.org/distributions/gradle-8.5-bin.zip" -o /tmp/gradle.zip
        unzip -q -o /tmp/gradle.zip -d /tmp/
        cp /tmp/gradle-8.5/lib/gradle-wrapper-*.jar gradle/wrapper/gradle-wrapper.jar 2>/dev/null || true
        rm -f /tmp/gradle.zip
        rm -rf /tmp/gradle-8.5
    fi
    chmod +x gradlew
fi

if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    erro "Não foi possível baixar o Gradle Wrapper. Verifique sua internet."
    exit 1
fi
info "Gradle Wrapper OK"

# ─── 4. Configurar Android SDK ─────────────────────────────
if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
    SDK_DIR="$ANDROID_HOME"
    info "Android SDK encontrado em: $SDK_DIR"
else
    SDK_DIR="$HOME/Android/Sdk"
    export ANDROID_HOME="$SDK_DIR"
    
    if [ ! -d "$SDK_DIR" ]; then
        info "Instalando Android SDK Command Line Tools em $SDK_DIR..."
        
        # Detectar arquitetura
        ARCH="linux"
        [[ "$(uname)" == "Darwin" ]] && ARCH="mac"
        
        CMDLINE_URL="https://dl.google.com/android/repository/commandlinetools-${ARCH}-11076708_latest.zip"
        
        mkdir -p "$SDK_DIR"
        cd /tmp
        info "Baixando Android Command Line Tools..."
        curl -sL "$CMDLINE_URL" -o cmdline-tools.zip
        unzip -q cmdline-tools.zip -d "$SDK_DIR"
        rm -f cmdline-tools.zip
        
        # Organizar no formato que o sdkmanager espera
        mkdir -p "$SDK_DIR/cmdline-tools"
        if [ -d "$SDK_DIR/cmdline-tools/bin" ]; then
            mv "$SDK_DIR/cmdline-tools" "$SDK_DIR/cmdline-tools/latest" 2>/dev/null || true
        elif [ -d "$SDK_DIR/latest/bin" ]; then
            mv "$SDK_DIR/latest" "$SDK_DIR/cmdline-tools/latest" 2>/dev/null || true
        else
            mkdir -p "$SDK_DIR/cmdline-tools/latest"
            mv "$SDK_DIR/bin" "$SDK_DIR/cmdline-tools/latest/" 2>/dev/null || true
            mv "$SDK_DIR/lib" "$SDK_DIR/cmdline-tools/latest/" 2>/dev/null || true
        fi
        
        cd "$SCRIPT_DIR"
    fi
    
    export PATH="$SDK_DIR/cmdline-tools/latest/bin:$PATH"
fi

# Escrever local.properties
echo "sdk.dir=$SDK_DIR" > local.properties
info "local.properties criado: sdk.dir=$SDK_DIR"

# ─── 5. Aceitar licenças e instalar plataformas ────────────
if command -v sdkmanager &>/dev/null; then
    info "Aceitando licenças do Android SDK..."
    yes | sdkmanager --licenses >/dev/null 2>&1 || true
    
    info "Instalando plataformas necessárias..."
    sdkmanager "platforms;android-34" "build-tools;34.0.0" >/dev/null 2>&1 || true
else
    warn "sdkmanager não encontrado. Tentando localizar..."
    SDKMANAGER=$(find "$SDK_DIR" -name sdkmanager -type f 2>/dev/null | head -1)
    if [ -n "$SDKMANAGER" ]; then
        yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
        "$SDKMANAGER" "platforms;android-34" "build-tools;34.0.0" >/dev/null 2>&1 || true
    fi
fi

# ─── 6. Build ──────────────────────────────────────────────
info "Compilando APK de debug..."
echo "----------------------------------------"
./gradlew assembleDebug
echo "----------------------------------------"

# ─── 7. Resultado ──────────────────────────────────────────
APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
    TAMANHO=$(du -h "$APK" | cut -f1)
    info "APK gerado com sucesso!"
    echo "  📱 $APK"
    echo "  📦 Tamanho: $TAMANHO"
    echo ""
    echo "Para instalar no celular:"
    echo "  1. Conecte o celular via USB com depuração USB ativada"
    echo "  2. Execute: adb install $APK"
    echo ""
    echo "Ou copie o APK para o celular e instale manualmente."
else
    erro "Falha ao gerar APK. Verifique os logs acima."
    exit 1
fi
