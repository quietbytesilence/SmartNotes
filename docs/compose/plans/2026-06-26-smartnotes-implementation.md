# SmartNotes — Implementation Plan

> [!NOTE]
> This document may not reflect the current implementation.
> See the final report for up-to-date state:
> [Final Report](../reports/smartnotes-app.md)

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Build MVP of Android lecture transcription app with discipline/topic/lecture organization, real-time speech recognition, and configurable AI summarization.

**Architecture:** Simplified MVVM (ViewModel → Repository → Room/Retrofit/DataStore). Hilt DI. Jetpack Compose + Material 3. Single Activity + Navigation Compose. SpeechRecognizer for real-time transcription.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, Hilt, Retrofit, OkHttp, DataStore, Navigation Compose, Coroutines, WorkManager

## Global Constraints

- compileSdk = 34, minSdk = 26, targetSdk = 34
- Kotlin 1.9.22, Compose BOM 2024.02.00, AGP 8.2.2
- Package name: com.example.smartnotes
- All strings in `strings.xml` (Portuguese-Brazilian)
- All icons via Material Icons (not drawable resources)
- minifyEnabled = true for release build
- Room schemas exported to `schemas/` directory (kapt)
- All entities use `Long` for IDs (auto-generate), timestamps as `Long` (epoch ms)
- Status field on Lecture: `RECORDING`, `TRANSCRIBED`, `SENT_TO_AI`, `SUMMARY_DONE`

---

### Task 1: Project Scaffolding

**Covers:** [S10]

**Files:**
- Create: `build.gradle.kts` (project-level)
- Create: `app/build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml` (version catalog)
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/smartnotes/SmartNotesApp.kt`
- Create: `app/src/main/java/com/example/smartnotes/MainActivity.kt`
- Create: `app/proguard-rules.pro`

- [ ] **Step 1: Create project-level build.gradle.kts**

```kotlin
// project build.gradle.kts
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
```

- [ ] **Step 2: Create version catalog `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.2.2"
kotlin = "1.9.22"
ksp = "1.9.22-1.0.17"
compose-bom = "2024.02.00"
hilt = "2.50"
room = "2.6.1"
retrofit = "2.9.0"
okhttp = "4.12.0"
datastore = "1.0.0"
navigation-compose = "2.7.7"
lifecycle = "2.7.0"
activity-compose = "1.8.2"
coroutines = "1.7.3"
gson = "2.10.1"
work = "2.9.0"
security-crypto = "1.1.0-alpha06"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.1.0" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "security-crypto" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 3: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "SmartNotes"
include(":app")
```

- [ ] **Step 4: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.smartnotes"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smartnotes"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime)
    implementation(libs.coroutines.android)
    implementation(libs.security.crypto)
    debugImplementation(libs.compose.ui.tooling)
}
```

- [ ] **Step 5: Create `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".SmartNotesApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.SmartNotes">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SmartNotes">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".recording.RecordingService"
            android:foregroundServiceType="microphone"
            android:exported="false" />
    </application>
</manifest>
```

- [ ] **Step 6: Create `SmartNotesApp.kt`**

```kotlin
package com.example.smartnotes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartNotesApp : Application()
```

- [ ] **Step 7: Create `MainActivity.kt`**

```kotlin
package com.example.smartnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.smartnotes.ui.navigation.NavGraph
import com.example.smartnotes.ui.theme.SmartNotesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartNotesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
```

- [ ] **Step 8: Create `res/values/strings.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">SmartNotes</string>
</resources>
```

- [ ] **Step 9: Create `res/values/themes.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.SmartNotes" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 10: Create `proguard-rules.pro`**

```
-keep class com.example.smartnotes.data.remote.model.** { *; }
```

---

### Task 2: Room Database — Entities and DAOs

**Covers:** [S4]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/entity/DisciplineEntity.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/entity/TopicEntity.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/entity/LectureEntity.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/entity/AISummaryEntity.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/dao/DisciplineDao.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/dao/TopicDao.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/dao/LectureDao.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/dao/AISummaryDao.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/db/AppDatabase.kt`

**Interfaces:**
- Produces: `AppDatabase` (Room database with all DAOs)

- [ ] **Step 1: Create `DisciplineEntity.kt`**

```kotlin
package com.example.smartnotes.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disciplines")
data class DisciplineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val icon: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Create `TopicEntity.kt`**

```kotlin
package com.example.smartnotes.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topics",
    foreignKeys = [ForeignKey(
        entity = DisciplineEntity::class,
        parentColumns = ["id"],
        childColumns = ["disciplineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("disciplineId")]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val disciplineId: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 3: Create `LectureEntity.kt`**

```kotlin
package com.example.smartnotes.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lectures",
    foreignKeys = [ForeignKey(
        entity = TopicEntity::class,
        parentColumns = ["id"],
        childColumns = ["topicId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("topicId")]
)
data class LectureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val wordCount: Int = 0,
    val status: String = "RECORDING",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 4: Create `AISummaryEntity.kt`**

```kotlin
package com.example.smartnotes.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_summaries",
    foreignKeys = [ForeignKey(
        entity = LectureEntity::class,
        parentColumns = ["id"],
        childColumns = ["lectureId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("lectureId")]
)
data class AISummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lectureId: Long,
    val summary: String = "",
    val keyTopics: String = "[]",
    val keyConcepts: String = "[]",
    val keywords: String = "[]",
    val reviewQuestions: String = "[]",
    val rawResponse: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 5: Create `DisciplineDao.kt`**

```kotlin
package com.example.smartnotes.data.local.db.dao

import androidx.room.*
import com.example.smartnotes.data.local.db.entity.DisciplineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplineDao {
    @Query("SELECT * FROM disciplines ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<DisciplineEntity>>

    @Query("SELECT * FROM disciplines WHERE id = :id")
    suspend fun getById(id: Long): DisciplineEntity?

    @Query("SELECT * FROM disciplines WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchFlow(query: String): Flow<List<DisciplineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(discipline: DisciplineEntity): Long

    @Update
    suspend fun update(discipline: DisciplineEntity)

    @Delete
    suspend fun delete(discipline: DisciplineEntity)

    @Query("DELETE FROM disciplines WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM disciplines")
    fun countFlow(): Flow<Int>
}
```

- [ ] **Step 6: Create `TopicDao.kt`**

```kotlin
package com.example.smartnotes.data.local.db.dao

import androidx.room.*
import com.example.smartnotes.data.local.db.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE disciplineId = :disciplineId ORDER BY updatedAt DESC")
    fun getByDisciplineFlow(disciplineId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getById(id: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity): Long

    @Update
    suspend fun update(topic: TopicEntity)

    @Delete
    suspend fun delete(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM topics WHERE disciplineId = :disciplineId")
    fun countByDisciplineFlow(disciplineId: Long): Flow<Int>
}
```

- [ ] **Step 7: Create `LectureDao.kt`**

```kotlin
package com.example.smartnotes.data.local.db.dao

import androidx.room.*
import com.example.smartnotes.data.local.db.entity.LectureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureDao {
    @Query("SELECT * FROM lectures WHERE topicId = :topicId ORDER BY date DESC")
    fun getByTopicFlow(topicId: Long): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE id = :id")
    suspend fun getById(id: Long): LectureEntity?

    @Query("SELECT * FROM lectures WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<LectureEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lecture: LectureEntity): Long

    @Update
    suspend fun update(lecture: LectureEntity)

    @Delete
    suspend fun delete(lecture: LectureEntity)

    @Query("DELETE FROM lectures WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM lectures")
    fun countFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM lectures WHERE topicId = :topicId")
    fun countByTopicFlow(topicId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM lectures WHERE disciplineId IN (SELECT id FROM topics WHERE disciplineId = :disciplineId)")
    fun countByDisciplineFlow(disciplineId: Long): Flow<Int>

    @Query("SELECT SUM(durationMs) FROM lectures")
    fun totalDurationFlow(): Flow<Long?>

    @Query("SELECT SUM(wordCount) FROM lectures")
    fun totalWordsFlow(): Flow<Long?>
}
```

- [ ] **Step 8: Create `AISummaryDao.kt`**

```kotlin
package com.example.smartnotes.data.local.db.dao

import androidx.room.*
import com.example.smartnotes.data.local.db.entity.AISummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AISummaryDao {
    @Query("SELECT * FROM ai_summaries WHERE lectureId = :lectureId")
    suspend fun getByLecture(lectureId: Long): AISummaryEntity?

    @Query("SELECT * FROM ai_summaries WHERE lectureId = :lectureId")
    fun getByLectureFlow(lectureId: Long): Flow<AISummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: AISummaryEntity): Long

    @Update
    suspend fun update(summary: AISummaryEntity)

    @Query("SELECT COUNT(*) FROM ai_summaries")
    fun countFlow(): Flow<Int>
}
```

- [ ] **Step 9: Create `AppDatabase.kt`**

```kotlin
package com.example.smartnotes.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.smartnotes.data.local.db.dao.*
import com.example.smartnotes.data.local.db.entity.*

@Database(
    entities = [
        DisciplineEntity::class,
        TopicEntity::class,
        LectureEntity::class,
        AISummaryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun disciplineDao(): DisciplineDao
    abstract fun topicDao(): TopicDao
    abstract fun lectureDao(): LectureDao
    abstract fun aiSummaryDao(): AISummaryDao
}
```

---

### Task 3: DataStore Preferences

**Covers:** [S7]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/data/local/datastore/ThemePreferences.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/local/datastore/ApiConfigPreferences.kt`

**Interfaces:**
- Produces: `ThemePreferences` (theme mode), `ApiConfigPreferences` (API endpoint, key, model, temp, maxTokens, customPrompt)

- [ ] **Step 1: Create `ThemePreferences.kt`**

```kotlin
package com.example.smartnotes.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    val themeFlow: Flow<String> = context.themeStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "system"
    }

    suspend fun setTheme(mode: String) {
        context.themeStore.edit { prefs ->
            prefs[THEME_KEY] = mode
        }
    }
}
```

- [ ] **Step 2: Create `ApiConfigPreferences.kt`**

```kotlin
package com.example.smartnotes.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.apiConfigStore: DataStore<Preferences> by preferencesDataStore(name = "api_config")

data class ApiConfig(
    val endpoint: String = "https://api.openai.com",
    val apiKey: String = "",
    val model: String = "gpt-3.5-turbo",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val customPrompt: String = ""
)

class ApiConfigPreferences(private val context: Context) {
    companion object {
        private val ENDPOINT_KEY = stringPreferencesKey("api_endpoint")
        private val API_KEY = stringPreferencesKey("api_key")
        private val MODEL_KEY = stringPreferencesKey("model")
        private val TEMPERATURE_KEY = floatPreferencesKey("temperature")
        private val MAX_TOKENS_KEY = intPreferencesKey("max_tokens")
        private val CUSTOM_PROMPT_KEY = stringPreferencesKey("custom_prompt")
    }

    val configFlow: Flow<ApiConfig> = context.apiConfigStore.data.map { prefs ->
        ApiConfig(
            endpoint = prefs[ENDPOINT_KEY] ?: "https://api.openai.com",
            apiKey = prefs[API_KEY] ?: "",
            model = prefs[MODEL_KEY] ?: "gpt-3.5-turbo",
            temperature = prefs[TEMPERATURE_KEY] ?: 0.7f,
            maxTokens = prefs[MAX_TOKENS_KEY] ?: 4096,
            customPrompt = prefs[CUSTOM_PROMPT_KEY] ?: ""
        )
    }

    suspend fun save(config: ApiConfig) {
        context.apiConfigStore.edit { prefs ->
            prefs[ENDPOINT_KEY] = config.endpoint
            prefs[API_KEY] = config.apiKey
            prefs[MODEL_KEY] = config.model
            prefs[TEMPERATURE_KEY] = config.temperature
            prefs[MAX_TOKENS_KEY] = config.maxTokens
            prefs[CUSTOM_PROMPT_KEY] = config.customPrompt
        }
    }
}
```

---

### Task 4: Remote API Models

**Covers:** [S5]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/data/remote/model/ChatRequest.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/remote/model/ChatResponse.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/remote/AiApiService.kt`

**Interfaces:**
- Produces: `AiApiService` (Retrofit interface), `ChatRequest`, `ChatResponse` (Gson models)

- [ ] **Step 1: Create `ChatRequest.kt`**

```kotlin
package com.example.smartnotes.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    @SerializedName("max_tokens") val maxTokens: Int = 4096
) {
    data class Message(
        val role: String,
        val content: String
    )
}
```

- [ ] **Step 2: Create `ChatResponse.kt`**

```kotlin
package com.example.smartnotes.data.remote.model

data class ChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val error: ErrorDetail?
) {
    data class Choice(
        val index: Int,
        val message: Message
    ) {
        data class Message(
            val role: String,
            val content: String
        )
    }

    data class ErrorDetail(
        val message: String?,
        val type: String?
    )
}
```

- [ ] **Step 3: Create `AiApiService.kt`**

```kotlin
package com.example.smartnotes.data.remote

import com.example.smartnotes.data.remote.model.ChatRequest
import com.example.smartnotes.data.remote.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {
    @POST("/v1/chat/completions")
    suspend fun chatCompletion(@Body request: ChatRequest): Response<ChatResponse>
}
```

---

### Task 5: Hilt DI Modules

**Covers:** [S2, S3]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/di/DatabaseModule.kt`
- Create: `app/src/main/java/com/example/smartnotes/di/NetworkModule.kt`
- Create: `app/src/main/java/com/example/smartnotes/di/RepositoryModule.kt`

**Interfaces:**
- Produces: Hilt bindings for Database, DAOs, Retrofit, Repositories, DataStore, Preferences

- [ ] **Step 1: Create `DatabaseModule.kt`**

```kotlin
package com.example.smartnotes.di

import android.content.Context
import androidx.room.Room
import com.example.smartnotes.data.local.db.AppDatabase
import com.example.smartnotes.data.local.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smartnotes.db"
        ).build()
    }

    @Provides fun provideDisciplineDao(db: AppDatabase) = db.disciplineDao()
    @Provides fun provideTopicDao(db: AppDatabase) = db.topicDao()
    @Provides fun provideLectureDao(db: AppDatabase) = db.lectureDao()
    @Provides fun provideAiSummaryDao(db: AppDatabase) = db.aiSummaryDao()
}
```

- [ ] **Step 2: Create `NetworkModule.kt`**

```kotlin
package com.example.smartnotes.di

import com.example.smartnotes.data.remote.AiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideAiApiService(okHttpClient: OkHttpClient): AiApiService {
        return Retrofit.Builder()
            .baseUrl("https://placeholder.local/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

- [ ] **Step 3: Create `RepositoryModule.kt`**

```kotlin
package com.example.smartnotes.di

import android.content.Context
import com.example.smartnotes.data.local.datastore.ApiConfigPreferences
import com.example.smartnotes.data.local.datastore.ThemePreferences
import com.example.smartnotes.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context) = ThemePreferences(context)

    @Provides @Singleton
    fun provideApiConfigPreferences(@ApplicationContext context: Context) = ApiConfigPreferences(context)

    @Provides @Singleton
    fun provideDisciplineRepository(dao: com.example.smartnotes.data.local.db.dao.DisciplineDao) =
        DisciplineRepository(dao)

    @Provides @Singleton
    fun provideTopicRepository(dao: com.example.smartnotes.data.local.db.dao.TopicDao) =
        TopicRepository(dao)

    @Provides @Singleton
    fun provideLectureRepository(dao: com.example.smartnotes.data.local.db.dao.LectureDao) =
        LectureRepository(dao)

    @Provides @Singleton
    fun provideAiRepository(
        apiConfigPrefs: ApiConfigPreferences,
        aiSummaryDao: com.example.smartnotes.data.local.db.dao.AISummaryDao
    ) = AiRepository(apiConfigPrefs, aiSummaryDao)
}
```

---

### Task 6: Repositories

**Covers:** [S3]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/data/repository/DisciplineRepository.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/repository/TopicRepository.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/repository/LectureRepository.kt`
- Create: `app/src/main/java/com/example/smartnotes/data/repository/AiRepository.kt`

**Interfaces:**
- Consumes: DAOs from Task 2, ApiConfigPreferences from Task 3, AiApiService from Task 4
- Produces: Repository functions consumed by ViewModels

- [ ] **Step 1: Create `DisciplineRepository.kt`**

```kotlin
package com.example.smartnotes.data.repository

import com.example.smartnotes.data.local.db.dao.DisciplineDao
import com.example.smartnotes.data.local.db.entity.DisciplineEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisciplineRepository @Inject constructor(
    private val dao: DisciplineDao
) {
    fun getAll(): Flow<List<DisciplineEntity>> = dao.getAllFlow()
    fun search(query: String): Flow<List<DisciplineEntity>> = dao.searchFlow(query)
    suspend fun getById(id: Long): DisciplineEntity? = dao.getById(id)
    suspend fun insert(discipline: DisciplineEntity): Long = dao.insert(discipline)
    suspend fun update(discipline: DisciplineEntity) = dao.update(discipline)
    suspend fun delete(discipline: DisciplineEntity) = dao.delete(discipline)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun count(): Flow<Int> = dao.countFlow()
}
```

- [ ] **Step 2: Create `TopicRepository.kt`**

```kotlin
package com.example.smartnotes.data.repository

import com.example.smartnotes.data.local.db.dao.TopicDao
import com.example.smartnotes.data.local.db.entity.TopicEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepository @Inject constructor(
    private val dao: TopicDao
) {
    fun getByDiscipline(disciplineId: Long): Flow<List<TopicEntity>> = dao.getByDisciplineFlow(disciplineId)
    suspend fun getById(id: Long): TopicEntity? = dao.getById(id)
    suspend fun insert(topic: TopicEntity): Long = dao.insert(topic)
    suspend fun update(topic: TopicEntity) = dao.update(topic)
    suspend fun delete(topic: TopicEntity) = dao.delete(topic)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun countByDiscipline(disciplineId: Long): Flow<Int> = dao.countByDisciplineFlow(disciplineId)
}
```

- [ ] **Step 3: Create `LectureRepository.kt`**

```kotlin
package com.example.smartnotes.data.repository

import com.example.smartnotes.data.local.db.dao.LectureDao
import com.example.smartnotes.data.local.db.entity.LectureEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LectureRepository @Inject constructor(
    private val dao: LectureDao
) {
    fun getByTopic(topicId: Long): Flow<List<LectureEntity>> = dao.getByTopicFlow(topicId)
    suspend fun getById(id: Long): LectureEntity? = dao.getById(id)
    fun getByIdFlow(id: Long): Flow<LectureEntity?> = dao.getByIdFlow(id)
    suspend fun insert(lecture: LectureEntity): Long = dao.insert(lecture)
    suspend fun update(lecture: LectureEntity) = dao.update(lecture)
    suspend fun delete(lecture: LectureEntity) = dao.delete(lecture)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun count(): Flow<Int> = dao.countFlow()
    fun countByTopic(topicId: Long): Flow<Int> = dao.countByTopicFlow(topicId)
    fun countByDiscipline(disciplineId: Long): Flow<Int> = dao.countByDisciplineFlow(disciplineId)
    fun totalDuration(): Flow<Long?> = dao.totalDurationFlow()
    fun totalWords(): Flow<Long?> = dao.totalWordsFlow()
}
```

- [ ] **Step 4: Create `AiRepository.kt`**

```kotlin
package com.example.smartnotes.data.repository

import com.example.smartnotes.data.local.db.dao.AISummaryDao
import com.example.smartnotes.data.local.db.entity.AISummaryEntity
import com.example.smartnotes.data.local.datastore.ApiConfig
import com.example.smartnotes.data.local.datastore.ApiConfigPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val apiConfigPrefs: ApiConfigPreferences,
    private val aiSummaryDao: AISummaryDao
) {
    fun getConfig(): Flow<ApiConfig> = apiConfigPrefs.configFlow

    suspend fun saveConfig(config: ApiConfig) = apiConfigPrefs.save(config)

    suspend fun getSummaryByLecture(lectureId: Long): AISummaryEntity? = aiSummaryDao.getByLecture(lectureId)

    fun getSummaryByLectureFlow(lectureId: Long): Flow<AISummaryEntity?> = aiSummaryDao.getByLectureFlow(lectureId)

    suspend fun saveSummary(summary: AISummaryEntity): Long = aiSummaryDao.insert(summary)

    suspend fun updateSummary(summary: AISummaryEntity) = aiSummaryDao.update(summary)

    fun count(): Flow<Int> = aiSummaryDao.countFlow()
}
```

---

### Task 7: Theme (Material 3)

**Covers:** [Dark Mode requirement]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/ui/theme/Color.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/theme/Type.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/theme/Theme.kt`

- [ ] **Step 1: Create `Color.kt`**

```kotlin
package com.example.smartnotes.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme
val md_theme_light_primary = Color(0xFF1A6B52)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFA5F2D3)
val md_theme_light_onPrimaryContainer = Color(0xFF002117)
val md_theme_light_secondary = Color(0xFF4C6359)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCEE9DB)
val md_theme_light_onSecondaryContainer = Color(0xFF092017)
val md_theme_light_background = Color(0xFFFBFDF9)
val md_theme_light_onBackground = Color(0xFF191C1A)
val md_theme_light_surface = Color(0xFFFBFDF9)
val md_theme_light_onSurface = Color(0xFF191C1A)
val md_theme_light_surfaceVariant = Color(0xFFDBE5DE)
val md_theme_light_onSurfaceVariant = Color(0xFF404943)
val md_theme_light_outline = Color(0xFF707973)

// Dark theme
val md_theme_dark_primary = Color(0xFF8AD6B8)
val md_theme_dark_onPrimary = Color(0xFF00382A)
val md_theme_dark_primaryContainer = Color(0xFF00513D)
val md_theme_dark_onPrimaryContainer = Color(0xFFA5F2D3)
val md_theme_dark_secondary = Color(0xFFB3CCC0)
val md_theme_dark_onSecondary = Color(0xFF1F352C)
val md_theme_dark_secondaryContainer = Color(0xFF354B42)
val md_theme_dark_onSecondaryContainer = Color(0xFFCEE9DB)
val md_theme_dark_background = Color(0xFF191C1A)
val md_theme_dark_onBackground = Color(0xFFE1E3DF)
val md_theme_dark_surface = Color(0xFF191C1A)
val md_theme_dark_onSurface = Color(0xFFE1E3DF)
val md_theme_dark_surfaceVariant = Color(0xFF404943)
val md_theme_dark_onSurfaceVariant = Color(0xFFBFC9C2)
val md_theme_dark_outline = Color(0xFF89938D)
```

- [ ] **Step 2: Create `Type.kt`**

```kotlin
package com.example.smartnotes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)
```

- [ ] **Step 3: Create `Theme.kt`**

```kotlin
package com.example.smartnotes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline
)

@Composable
fun SmartNotesTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

### Task 8: Navigation

**Covers:** [S3, S6]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/ui/navigation/Routes.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/navigation/NavGraph.kt`

- [ ] **Step 1: Create `Routes.kt`**

```kotlin
package com.example.smartnotes.ui.navigation

sealed class Routes(val route: String) {
    data object Disciplines : Routes("disciplines")
    data object Settings : Routes("settings")

    data object Topics : Routes("topics/{disciplineId}") {
        fun createRoute(disciplineId: Long) = "topics/$disciplineId"
    }
    data object Lectures : Routes("lectures/{topicId}") {
        fun createRoute(topicId: Long) = "lectures/$topicId"
    }
    data object Recording : Routes("recording/{topicId}/{lectureId}") {
        fun createRoute(topicId: Long, lectureId: Long) = "recording/$topicId/$lectureId"
    }
    data object LectureDetail : Routes("lecture-detail/{lectureId}") {
        fun createRoute(lectureId: Long) = "lecture-detail/$lectureId"
    }
}
```

- [ ] **Step 2: Create `NavGraph.kt`**

```kotlin
package com.example.smartnotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartnotes.ui.screens.disciplines.DisciplineListScreen
import com.example.smartnotes.ui.screens.topics.TopicListScreen
import com.example.smartnotes.ui.screens.lectures.LectureListScreen
import com.example.smartnotes.ui.screens.lectures.RecordingScreen
import com.example.smartnotes.ui.screens.lectures.LectureDetailScreen
import com.example.smartnotes.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.Disciplines.route) {
        composable(Routes.Disciplines.route) {
            DisciplineListScreen(
                onDisciplineClick = { disciplineId ->
                    navController.navigate(Routes.Topics.createRoute(disciplineId))
                },
                onSettingsClick = {
                    navController.navigate(Routes.Settings.route)
                }
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.Topics.route,
            arguments = listOf(navArgument("disciplineId") { type = NavType.LongType })
        ) { backStackEntry ->
            val disciplineId = backStackEntry.arguments?.getLong("disciplineId") ?: return@composable
            TopicListScreen(
                disciplineId = disciplineId,
                onTopicClick = { topicId ->
                    navController.navigate(Routes.Lectures.createRoute(topicId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.Lectures.route,
            arguments = listOf(navArgument("topicId") { type = NavType.LongType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: return@composable
            LectureListScreen(
                topicId = topicId,
                onLectureClick = { lectureId ->
                    navController.navigate(Routes.LectureDetail.createRoute(lectureId))
                },
                onStartRecording = { lectureId ->
                    navController.navigate(Routes.Recording.createRoute(topicId, lectureId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.Recording.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.LongType },
                navArgument("lectureId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: return@composable
            val lectureId = backStackEntry.arguments?.getLong("lectureId") ?: return@composable
            RecordingScreen(
                topicId = topicId,
                lectureId = lectureId,
                onFinish = {
                    navController.popBackStack(Routes.Lectures.createRoute(topicId), false)
                },
                onCancel = {
                    navController.popBackStack(Routes.Lectures.createRoute(topicId), false)
                }
            )
        }
        composable(
            route = Routes.LectureDetail.route,
            arguments = listOf(navArgument("lectureId") { type = NavType.LongType })
        ) { backStackEntry ->
            val lectureId = backStackEntry.arguments?.getLong("lectureId") ?: return@composable
            LectureDetailScreen(
                lectureId = lectureId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

### Task 9: Discipline List Screen

**Covers:** [S6]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/disciplines/DisciplineListViewModel.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/disciplines/DisciplineListScreen.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/components/DisciplineCard.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/components/SearchBar.kt`

**Interfaces:**
- Consumes: `DisciplineRepository`, `LectureRepository`, `ThemePreferences`
- Produces: UI for discipline list, create discipline dialog, search

- [ ] **Step 1: Create `DisciplineListViewModel.kt`**

```kotlin
package com.example.smartnotes.ui.screens.disciplines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.db.entity.DisciplineEntity
import com.example.smartnotes.data.repository.DisciplineRepository
import com.example.smartnotes.data.repository.LectureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DisciplineWithStats(
    val discipline: DisciplineEntity,
    val topicCount: Int = 0,
    val lectureCount: Int = 0
)

@HiltViewModel
class DisciplineListViewModel @Inject constructor(
    private val disciplineRepo: DisciplineRepository,
    private val lectureRepo: LectureRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val disciplines = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) disciplineRepo.getAll()
            else disciplineRepo.search(query)
        }
        .map { list ->
            list.map { entity ->
                DisciplineWithStats(
                    discipline = entity,
                    topicCount = 0,
                    lectureCount = 0
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun createDiscipline(name: String, color: Int, icon: String) {
        viewModelScope.launch {
            disciplineRepo.insert(
                DisciplineEntity(name = name, color = color, icon = icon)
            )
        }
    }

    fun deleteDiscipline(discipline: DisciplineEntity) {
        viewModelScope.launch {
            disciplineRepo.delete(discipline)
        }
    }
}
```

- [ ] **Step 2: Create `DisciplineCard.kt`**

```kotlin
package com.example.smartnotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DisciplineCard(
    name: String,
    color: Int,
    topicCount: Int,
    lectureCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(color).copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(color),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$lectureCount aulas · $topicCount tópicos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

- [ ] **Step 3: Create `SearchBar.kt`**

```kotlin
package com.example.smartnotes.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Pesquisar...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpar")
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}
```

- [ ] **Step 4: Create `DisciplineListScreen.kt`**

```kotlin
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
                query = viewModel.searchQuery,
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
```

- [ ] **Step 5: Fix ViewModel to expose searchQuery for SearchBar**

In `DisciplineListViewModel`, make `_searchQuery` accessible as a State:

```kotlin
// Add to DisciplineListViewModel:
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
```

- [ ] **Step 6: Create `CreateDisciplineDialog.kt`**

```kotlin
package com.example.smartnotes.ui.screens.disciplines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val disciplineColors = listOf(
    0xFF1A6B52, 0xFF1565C0, 0xFF7B1FA2, 0xFFC62828,
    0xFFEF6C00, 0xFF00897B, 0xFF5C6BC0, 0xFFE91E63,
    0xFF00ACC1, 0xFF43A047, 0xFF8D6E63, 0xFF546E7A
)

@Composable
fun CreateDisciplineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(disciplineColors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Disciplina") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da disciplina") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cor", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(disciplineColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (color == selectedColor) {
                                        Modifier
                                            .clickable { selectedColor = color }
                                    } else {
                                        Modifier.clickable { selectedColor = color }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, selectedColor, "MenuBook") },
                enabled = name.isNotBlank()
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
```

---

### Task 10: Topic List Screen

**Covers:** [S6]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/topics/TopicListViewModel.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/topics/TopicListScreen.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/components/TopicCard.kt`

**Interfaces:**
- Consumes: `TopicRepository`, `LectureRepository`, `DisciplineRepository`
- Produces: Topic list UI, create topic dialog

- [ ] **Step 1: Create `TopicListViewModel.kt`**

```kotlin
package com.example.smartnotes.ui.screens.topics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.db.entity.TopicEntity
import com.example.smartnotes.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicWithStats(
    val topic: TopicEntity,
    val lectureCount: Int = 0
)

@HiltViewModel
class TopicListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val topicRepo: TopicRepository
) : ViewModel() {

    val disciplineId: Long = savedStateHandle["disciplineId"] ?: -1L

    val topics: StateFlow<List<TopicWithStats>> = topicRepo.getByDiscipline(disciplineId)
        .map { list ->
            list.map { entity ->
                TopicWithStats(topic = entity)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createTopic(name: String) {
        viewModelScope.launch {
            topicRepo.insert(
                TopicEntity(disciplineId = disciplineId, name = name)
            )
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            topicRepo.delete(topic)
        }
    }
}
```

- [ ] **Step 2: Create `TopicCard.kt`**

```kotlin
package com.example.smartnotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TopicCard(
    name: String,
    lectureCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$lectureCount aulas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

- [ ] **Step 3: Create `TopicListScreen.kt`**

```kotlin
package com.example.smartnotes.ui.screens.topics

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
import com.example.smartnotes.ui.components.TopicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicListScreen(
    disciplineId: Long,
    onTopicClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: TopicListViewModel = hiltViewModel()
) {
    val topics by viewModel.topics.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tópicos") },
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
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Tópico") }
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
            items(topics, key = { it.topic.id }) { item ->
                TopicCard(
                    name = item.topic.name,
                    lectureCount = item.lectureCount,
                    onClick = { onTopicClick(item.topic.id) }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateTopicDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createTopic(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateTopicDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Tópico") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do tópico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
```

---

### Task 11: Lecture List Screen

**Covers:** [S6]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/lectures/LectureListViewModel.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/lectures/LectureListScreen.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/components/LectureCard.kt`

**Interfaces:**
- Consumes: `LectureRepository`, `TopicRepository`
- Produces: Lecture list UI, start lecture action

- [ ] **Step 1: Create `LectureListViewModel.kt`**

```kotlin
package com.example.smartnotes.ui.screens.lectures

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.db.entity.LectureEntity
import com.example.smartnotes.data.repository.LectureRepository
import com.example.smartnotes.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lectureRepo: LectureRepository,
    private val topicRepo: TopicRepository
) : ViewModel() {

    val topicId: Long = savedStateHandle["topicId"] ?: -1L

    val topicName = topicRepo.getById(topicId)?.let { /* handled via async */ }

    val lectures: StateFlow<List<LectureEntity>> = lectureRepo.getByTopic(topicId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startNewLecture(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val lecture = LectureEntity(
                topicId = topicId,
                title = "Aula ${System.currentTimeMillis()}"
            )
            val id = lectureRepo.insert(lecture)
            onStarted(id)
        }
    }
}
```

- [ ] **Step 2: Create `LectureCard.kt`**

```kotlin
package com.example.smartnotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun LectureCard(
    title: String,
    date: Long,
    durationMs: Long,
    wordCount: Int,
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR"))
        .format(java.util.Date(date))
    val durationText = "${durationMs / 60000}min"
    val statusLabel = when (status) {
        "SUMMARY_DONE" -> "Resumo disponível"
        "SENT_TO_AI" -> "Enviado para IA"
        "TRANSCRIBED" -> "Transcrito"
        else -> "Gravando"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$wordCount palavras",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (status == "SUMMARY_DONE") {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Create `LectureListScreen.kt`**

```kotlin
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
```

---

### Task 12: Recording — SpeechRecognizer + Screen + Foreground Service

**Covers:** [S8, S9]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/recording/SpeechRecognizerManager.kt`
- Create: `app/src/main/java/com/example/smartnotes/recording/RecordingService.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/lectures/RecordingViewModel.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/lectures/RecordingScreen.kt`

**Interfaces:**
- Consumes: `LectureRepository`
- Produces: UI for recording screen, transcription storage

- [ ] **Step 1: Create `SpeechRecognizerManager.kt`**

```kotlin
package com.example.smartnotes.recording

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognizerManager @Inject constructor() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var transcriptFile: File? = null

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText

    private val _finalText = MutableStateFlow("")
    val finalText: StateFlow<String> = _finalText

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private var fullTranscript = StringBuilder()

    fun startListening(context: Context, filePath: String) {
        if (SpeechRecognizer.isRecognitionAvailable(context).not()) return

        transcriptFile = File(filePath)
        transcriptFile?.parentFile?.mkdirs()

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("pt", "BR").toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { text ->
                    fullTranscript.append(" ").append(text)
                    _finalText.value = fullTranscript.toString()
                    saveTranscript()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { text ->
                    _partialText.value = text
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() { _isListening.value = true }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { _isListening.value = false }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }

    fun cancel() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }

    fun getFullTranscript(): String = fullTranscript.toString()

    private fun saveTranscript() {
        transcriptFile?.writeText(fullTranscript.toString())
    }

    fun loadTranscript(filePath: String): String {
        return try {
            File(filePath).readText()
        } catch (e: Exception) {
            ""
        }
    }
}
```

- [ ] **Step 2: Create `RecordingService.kt`**

```kotlin
package com.example.smartnotes.recording

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smartnotes.MainActivity
import com.example.smartnotes.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : Service() {

    @Inject lateinit var speechRecognizerManager: SpeechRecognizerManager

    companion object {
        const val CHANNEL_ID = "recording_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.smartnotes.STOP_RECORDING"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Gravação de Aula",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificação persistente durante gravação de aula"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this, 0, Intent(this, RecordingService::class.java).apply {
                action = ACTION_STOP
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Gravando aula...")
            .setContentText("Toque para abrir o aplicativo")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_media_pause, "Parar", stopIntent)
            .setOngoing(true)
            .build()
    }
}
```

- [ ] **Step 3: Create `RecordingViewModel.kt`**

```kotlin
package com.example.smartnotes.ui.screens.lectures

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.db.entity.LectureEntity
import com.example.smartnotes.data.repository.LectureRepository
import com.example.smartnotes.recording.SpeechRecognizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    application: Application,
    private val lectureRepo: LectureRepository,
    private val speechManager: SpeechRecognizerManager
) : AndroidViewModel(application) {

    private var lectureId: Long = 0
    private var startTime = 0L

    val partialText: StateFlow<String> = speechManager.partialText
    val finalText: StateFlow<String> = speechManager.finalText
    val isListening: StateFlow<Boolean> = speechManager.isListening

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs

    private val _wordCount = MutableStateFlow(0)
    val wordCount: StateFlow<Int> = _wordCount

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    fun initialize(topicId: Long, lectureId: Long) {
        this.lectureId = lectureId
        startTime = System.currentTimeMillis()

        val filePath = getTranscriptFilePath(lectureId)

        viewModelScope.launch {
            speechManager.finalText.collect { text ->
                val words = text.trim().split("\\s+".toRegex()).size
                _wordCount.value = words
            }
        }

        speechManager.startListening(getApplication(), filePath)

        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _elapsedMs.value = System.currentTimeMillis() - startTime

                // Save partial transcript every 30s
                if (_elapsedMs.value % 30000 < 1000) {
                    updateLectureProgress()
                }
            }
        }
    }

    fun pause() {
        speechManager.stopListening()
    }

    fun resume() {
        val filePath = getTranscriptFilePath(lectureId)
        speechManager.startListening(getApplication(), filePath)
    }

    fun finish() {
        speechManager.stopListening()
        updateLectureProgress(final = true)
    }

    fun cancel() {
        speechManager.cancel()
        viewModelScope.launch {
            lectureRepo.deleteById(lectureId)
        }
    }

    private fun updateLectureProgress(final: Boolean = false) {
        viewModelScope.launch {
            val lecture = lectureRepo.getById(lectureId) ?: return@launch
            val transcript = speechManager.getFullTranscript()
            val words = transcript.trim().split("\\s+".toRegex()).size

            lectureRepo.update(
                lecture.copy(
                    durationMs = System.currentTimeMillis() - startTime,
                    wordCount = words,
                    status = if (final) "TRANSCRIBED" else lecture.status
                )
            )
        }
    }

    private fun getTranscriptFilePath(lectureId: Long): String {
        val dir = getApplication<Application>().filesDir
        return "${dir.absolutePath}/transcripts/lecture_$lectureId.txt"
    }
}
```

- [ ] **Step 4: Create `RecordingScreen.kt`**

```kotlin
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
            // Timer
            Text(
                text = formatTime(elapsedMs),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Audio indicator
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

            // Live transcription
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

            // Controls
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
```

---

### Task 13: AI Integration

**Covers:** [S5]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/ai/AiService.kt`

**Interfaces:**
- Consumes: `AiApiService`, `ApiConfigPreferences`, `AiRepository`, `AiSummaryDao`
- Produces: AI summary call + persist result

- [ ] **Step 1: Create `AiService.kt`**

```kotlin
package com.example.smartnotes.ai

import com.example.smartnotes.data.local.db.entity.AISummaryEntity
import com.example.smartnotes.data.local.db.entity.LectureEntity
import com.example.smartnotes.data.remote.AiApiService
import com.example.smartnotes.data.remote.model.ChatRequest
import com.example.smartnotes.data.repository.AiRepository
import com.example.smartnotes.data.repository.LectureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiService @Inject constructor(
    private val aiRepository: AiRepository,
    private val lectureRepository: LectureRepository,
    private val okHttpClient: OkHttpClient
) {

    private val defaultPrompt = """
Você é um assistente especializado em transformar transcrições de aulas em material de estudo.

Analise a transcrição abaixo.

Retorne APENAS um objeto JSON válido (sem formatação markdown, sem ```, sem texto extra) com os seguintes campos:
{
  "summary": "resumo detalhado da aula",
  "keyTopics": ["tópico 1", "tópico 2"],
  "keyConcepts": ["conceito 1", "conceito 2"],
  "keywords": ["palavra1", "palavra2"],
  "reviewQuestions": ["pergunta 1?", "pergunta 2?"]
}

Transcrição:
{transcription}
    """.trimIndent()

    suspend fun processLecture(lectureId: Long, transcription: String): Result<AISummaryEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val config = aiRepository.getConfig()

                // Build API-specific Retrofit instance with dynamic base URL
                val baseUrl = config.endpoint.let { endpoint ->
                    if (endpoint.endsWith("/v1/chat/completions")) {
                        endpoint.substringBefore("/v1/chat/completions") + "/"
                    } else if (!endpoint.endsWith("/")) {
                        "$endpoint/"
                    } else endpoint
                }

                val extendedClient = okHttpClient.newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${config.apiKey}")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .build()

                val apiService = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(extendedClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AiApiService::class.java)

                val promptText = config.customPrompt.ifBlank { defaultPrompt }
                    .replace("{transcription}", transcription)

                val request = ChatRequest(
                    model = config.model,
                    messages = listOf(
                        ChatRequest.Message(role = "system", content = "Você é um assistente especializado em material de estudo."),
                        ChatRequest.Message(role = "user", content = promptText)
                    ),
                    temperature = config.temperature,
                    maxTokens = config.maxTokens
                )

                val response = apiService.chatCompletion(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.choices?.firstOrNull()?.message?.content ?: ""
                    val aiSummary = parseAiResponse(lectureId, content)

                    // Save summary
                    aiRepository.saveSummary(aiSummary)

                    // Update lecture status
                    val lecture = lectureRepository.getById(lectureId)
                    lecture?.let {
                        lectureRepository.update(it.copy(status = "SUMMARY_DONE"))
                    }

                    Result.success(aiSummary)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    Result.failure(Exception("API error: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseAiResponse(lectureId: Long, jsonContent: String): AISummaryEntity {
        val cleaned = jsonContent
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val gson = com.google.gson.Gson()
            val json = gson.fromJson(cleaned, Map::class.java) as Map<String, Any>

            AISummaryEntity(
                lectureId = lectureId,
                summary = json["summary"] as? String ?: "",
                keyTopics = gson.toJson(json["keyTopics"] ?: emptyList<String>()),
                keyConcepts = gson.toJson(json["keyConcepts"] ?: emptyList<String>()),
                keywords = gson.toJson(json["keywords"] ?: emptyList<String>()),
                reviewQuestions = gson.toJson(json["reviewQuestions"] ?: emptyList<String>()),
                rawResponse = jsonContent
            )
        } catch (e: Exception) {
            AISummaryEntity(
                lectureId = lectureId,
                summary = cleaned,
                rawResponse = jsonContent
            )
        }
    }
}
```

---

### Task 14: Lecture Detail Screen (Transcription + AI Result)

**Covers:** [S6]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/lectures/LectureDetailViewModel.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/lectures/LectureDetailScreen.kt`

**Interfaces:**
- Consumes: `LectureRepository`, `AiRepository`, `AiService`
- Produces: Lecture detail showing transcription and AI summary

- [ ] **Step 1: Create `LectureDetailViewModel.kt`**

```kotlin
package com.example.smartnotes.ui.screens.lectures

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.ai.AiService
import com.example.smartnotes.data.local.db.entity.AISummaryEntity
import com.example.smartnotes.data.local.db.entity.LectureEntity
import com.example.smartnotes.data.repository.AiRepository
import com.example.smartnotes.data.repository.LectureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LectureDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lectureRepo: LectureRepository,
    private val aiRepo: AiRepository,
    private val aiService: AiService
) : ViewModel() {

    val lectureId: Long = savedStateHandle["lectureId"] ?: -1L

    val lecture: StateFlow<LectureEntity?> = lectureRepo.getByIdFlow(lectureId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val aiSummary: StateFlow<AISummaryEntity?> = aiRepo.getSummaryByLectureFlow(lectureId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription

    init {
        loadTranscription()
    }

    private fun loadTranscription() {
        viewModelScope.launch {
            val lecture = lectureRepo.getById(lectureId) ?: return@launch
            val app = getApplication<android.app.Application>()
            val filePath = "${app.filesDir.absolutePath}/transcripts/lecture_$lectureId.txt"
            val text = try {
                File(filePath).readText()
            } catch (e: Exception) { "" }
            _transcription.value = text
        }
    }

    fun sendToAi() {
        viewModelScope.launch {
            _isProcessing.value = true
            val transcript = _transcription.value
            if (transcript.isBlank()) {
                _isProcessing.value = false
                return@launch
            }
            aiService.processLecture(lectureId, transcript)
            _isProcessing.value = false
        }
    }
}

// Need Application context for file path
private fun <T> getApplication(): android.app.Application {
    // Workaround: ViewModel needs AndroidViewModel
    throw IllegalStateException("Should use AndroidViewModel instead")
}
```

- [ ] **Step 2: Fix ViewModel to extend AndroidViewModel**

Replace `ViewModel` with `AndroidViewModel` in `LectureDetailViewModel`:

```kotlin
class LectureDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lectureRepo: LectureRepository,
    private val aiRepo: AiRepository,
    private val aiService: AiService,
    private val application: android.app.Application
) : AndroidViewModel(application) {
```

And fix `loadTranscription` to use `application`:

```kotlin
private fun loadTranscription() {
    viewModelScope.launch {
        val lecture = lectureRepo.getById(lectureId) ?: return@launch
        val filePath = "${application.filesDir.absolutePath}/transcripts/lecture_$lectureId.txt"
        val text = try {
            File(filePath).readText()
        } catch (e: Exception) { "" }
        _transcription.value = text
    }
}
```

- [ ] **Step 3: Create `LectureDetailScreen.kt`**

```kotlin
package com.example.smartnotes.ui.screens.lectures

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
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
            // Transcription section
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

            // AI Summary section
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

                // Key Topics
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

                // Review Questions
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

                // Keywords
                item {
                    val keywords = parseJsonList(summary.keywords)
                    if (keywords.isNotEmpty()) {
                        Text(
                            text = "Palavras-chave",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.layout.FlowRow(
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
```

---

### Task 15: Settings Screen

**Covers:** [S7]

**Files:**
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/settings/SettingsViewModel.kt`
- Create: `app/src/main/java/com/example/smartnotes/ui/screens/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes: `ApiConfigPreferences`, `ThemePreferences`, `AiApiService`
- Produces: Settings UI for API config, theme toggles

- [ ] **Step 1: Create `SettingsViewModel.kt`**

```kotlin
package com.example.smartnotes.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.local.datastore.ApiConfig
import com.example.smartnotes.data.local.datastore.ApiConfigPreferences
import com.example.smartnotes.data.local.datastore.ThemePreferences
import com.example.smartnotes.data.remote.AiApiService
import com.example.smartnotes.data.remote.model.ChatRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val themePrefs: ThemePreferences,
    private val apiConfigPrefs: ApiConfigPreferences
) : AndroidViewModel(application) {

    val themeMode: StateFlow<String> = themePrefs.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val apiConfig: StateFlow<ApiConfig> = apiConfigPrefs.configFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiConfig())

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult

    fun setTheme(mode: String) {
        viewModelScope.launch { themePrefs.setTheme(mode) }
    }

    fun saveApiConfig(config: ApiConfig) {
        viewModelScope.launch { apiConfigPrefs.save(config) }
    }

    fun testConnection(config: ApiConfig) {
        viewModelScope.launch {
            _testResult.value = null
            try {
                val baseUrl = config.endpoint.let { endpoint ->
                    if (endpoint.endsWith("/v1/chat/completions")) {
                        endpoint.substringBefore("/v1/chat/completions") + "/"
                    } else if (!endpoint.endsWith("/")) {
                        "$endpoint/"
                    } else endpoint
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${config.apiKey}")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                    .build()

                val api = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AiApiService::class.java)

                val request = ChatRequest(
                    model = config.model,
                    messages = listOf(
                        ChatRequest.Message(role = "user", content = "Teste de conexão. Responda apenas 'OK'.")
                    ),
                    temperature = 0.1f,
                    maxTokens = 10
                )

                val response = api.chatCompletion(request)
                if (response.isSuccessful) {
                    _testResult.value = "Conexão bem-sucedida! ✓"
                } else {
                    _testResult.value = "Erro: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _testResult.value = "Falha: ${e.localizedMessage ?: "Erro desconhecido"}"
            }
        }
    }
}
```

- [ ] **Step 2: Create `SettingsScreen.kt`**

```kotlin
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
            // Theme section
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

            // API Configuration
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
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
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
```
