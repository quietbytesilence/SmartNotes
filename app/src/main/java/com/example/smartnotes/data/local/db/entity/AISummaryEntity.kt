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
