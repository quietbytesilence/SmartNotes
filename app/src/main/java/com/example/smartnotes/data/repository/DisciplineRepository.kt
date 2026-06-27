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
