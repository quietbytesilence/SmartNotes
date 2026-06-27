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
