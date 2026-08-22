package com.nilian.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nilian.app.data.local.dao.DailyRitualDao
import com.nilian.app.data.local.dao.DayTemplateDao
import com.nilian.app.data.local.dao.EventDao
import com.nilian.app.data.local.dao.GoalDao
import com.nilian.app.data.local.dao.HabitDao
import com.nilian.app.data.local.dao.InboxNoteDao
import com.nilian.app.data.local.dao.TaskDao
import com.nilian.app.data.local.dao.TimeBlockDao
import com.nilian.app.data.local.entity.DailyRitualEntity
import com.nilian.app.data.local.entity.DayTemplateEntity
import com.nilian.app.data.local.entity.EventEntity
import com.nilian.app.data.local.entity.GoalEntity
import com.nilian.app.data.local.entity.HabitEntity
import com.nilian.app.data.local.entity.HabitLogEntity
import com.nilian.app.data.local.entity.InboxNoteEntity
import com.nilian.app.data.local.entity.TaskEntity
import com.nilian.app.data.local.entity.TemplateBlockEntity
import com.nilian.app.data.local.entity.TimeBlockEntity

/**
 * Main Room database for Nilian.
 * Stores Tasks, Events, Habits, Habit Logs, Time Blocks, Goals,
 * Inbox Notes, Day Templates, Template Blocks, and Daily Rituals.
 */
@Database(
    entities = [
        TaskEntity::class,
        EventEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        TimeBlockEntity::class,
        GoalEntity::class,
        InboxNoteEntity::class,
        DayTemplateEntity::class,
        TemplateBlockEntity::class,
        DailyRitualEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NilianDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun eventDao(): EventDao
    abstract fun habitDao(): HabitDao
    abstract fun timeBlockDao(): TimeBlockDao
    abstract fun goalDao(): GoalDao
    abstract fun inboxNoteDao(): InboxNoteDao
    abstract fun dayTemplateDao(): DayTemplateDao
    abstract fun dailyRitualDao(): DailyRitualDao

    companion object {
        const val DATABASE_NAME = "nilian_database.db"

        @Volatile
        private var instance: NilianDatabase? = null

        /**
         * Returns the singleton instance of NilianDatabase.
         */
        fun getInstance(context: Context): NilianDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NilianDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
