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
