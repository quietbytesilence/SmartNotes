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
    info "Baixando Gradle Wrapper JAR do Maven Central..."
    # URL confiável: Maven Central
    MAVEN_URL="https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/8.5/gradle-wrapper-8.5.jar"
    
    if command -v curl &>/dev/null; then
        HTTP_CODE=$(curl -sL -o gradle/wrapper/gradle-wrapper.jar -w "%{http_code}" "$MAVEN_URL")
    elif command -v wget &>/dev/null; then
        wget -q "$MAVEN_URL" -O gradle/wrapper/gradle-wrapper.jar
        HTTP_CODE=$?
    else
        erro "Nem curl nem wget disponíveis. Instale um deles."
        exit 1
    fi
    
    # Verificar se baixou algo válido (não HTML)
    if [ "$HTTP_CODE" != "200" ] || head -c 100 gradle/wrapper/gradle-wrapper.jar 2>/dev/null | grep -q "html"; then
        warn "Maven Central falhou. Baixando Gradle completo para extrair o wrapper..."
        curl -sL "https://services.gradle.org/distributions/gradle-8.5-bin.zip" -o /tmp/gradle.zip
        unzip -q -o /tmp/gradle.zip -d /tmp/
        cp /tmp/gradle-8.5/lib/gradle-wrapper-*.jar gradle/wrapper/gradle-wrapper.jar 2>/dev/null || true
        rm -rf /tmp/gradle-8.5 /tmp/gradle.zip
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
    
    if [ ! -f "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" ]; then
        warn "SDK incompleto ou estrutura incorreta. Reinstalando..."
        rm -rf "$SDK_DIR/cmdline-tools" 2>/dev/null || true
        info "Instalando Android SDK Command Line Tools em $SDK_DIR..."
        
        ARCH="linux"
        [[ "$(uname)" == "Darwin" ]] && ARCH="mac"
        
        CMDLINE_URL="https://dl.google.com/android/repository/commandlinetools-${ARCH}-11076708_latest.zip"
        
        mkdir -p "$SDK_DIR"
        cd /tmp
        info "Baixando Android Command Line Tools..."
        curl -sL "$CMDLINE_URL" -o cmdline-tools.zip
        unzip -q cmdline-tools.zip
        rm -f cmdline-tools.zip
        
        # O zip extrai para cmdline-tools/ — mover para cmdline-tools/latest/
        mkdir -p "$SDK_DIR/cmdline-tools"
        mv cmdline-tools "$SDK_DIR/cmdline-tools/latest"
        
        cd "$SCRIPT_DIR"
    fi
    
    export PATH="$SDK_DIR/cmdline-tools/latest/bin:$PATH"
fi

# Escrever local.properties
echo "sdk.dir=$SDK_DIR" > local.properties
info "local.properties criado: sdk.dir=$SDK_DIR"

# ─── 5. Aceitar licenças e instalar plataformas ────────────
if [ -f "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER="$SDK_DIR/cmdline-tools/latest/bin/sdkmanager"
    info "Aceitando licenças do Android SDK..."
    yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
    
    info "Instalando plataformas necessárias..."
    "$SDKMANAGER" "platforms;android-34" "build-tools;34.0.0" >/dev/null 2>&1 || true
else
    warn "sdkmanager não encontrado. Tentando continuar mesmo assim..."
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
