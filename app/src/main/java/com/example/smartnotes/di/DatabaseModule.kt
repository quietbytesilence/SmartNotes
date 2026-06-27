package com.example.smartnotes.di

import android.content.Context
import androidx.room.Room
import com.example.smartnotes.data.local.db.AppDatabase
import com.example.smartnotes.data.local.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smartnotes.db"
        ).build()
    }

    @Provides fun provideDisciplineDao(db: AppDatabase) = db.disciplineDao()
    @Provides fun provideTopicDao(db: AppDatabase) = db.topicDao()
    @Provides fun provideLectureDao(db: AppDatabase) = db.lectureDao()
    @Provides fun provideAiSummaryDao(db: AppDatabase) = db.aiSummaryDao()
}
