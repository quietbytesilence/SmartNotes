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
