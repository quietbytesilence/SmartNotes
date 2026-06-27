package com.example.smartnotes.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.smartnotes.data.local.db.dao.*
import com.example.smartnotes.data.local.db.entity.*

@Database(
    entities = [
        DisciplineEntity::class,
        TopicEntity::class,
        LectureEntity::class,
        AISummaryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun disciplineDao(): DisciplineDao
    abstract fun topicDao(): TopicDao
    abstract fun lectureDao(): LectureDao
    abstract fun aiSummaryDao(): AISummaryDao
}
