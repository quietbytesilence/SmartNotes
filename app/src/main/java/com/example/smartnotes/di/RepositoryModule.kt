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
