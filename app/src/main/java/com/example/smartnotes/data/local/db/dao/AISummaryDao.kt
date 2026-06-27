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
