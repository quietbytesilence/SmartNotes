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

    @Query("SELECT COUNT(*) FROM lectures WHERE topicId IN (SELECT id FROM topics WHERE disciplineId = :disciplineId)")
    fun countByDisciplineFlow(disciplineId: Long): Flow<Int>

    @Query("SELECT SUM(durationMs) FROM lectures")
    fun totalDurationFlow(): Flow<Long?>

    @Query("SELECT SUM(wordCount) FROM lectures")
    fun totalWordsFlow(): Flow<Long?>
}
