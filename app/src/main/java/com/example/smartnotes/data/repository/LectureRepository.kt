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
