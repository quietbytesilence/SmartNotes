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
