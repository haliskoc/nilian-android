package com.nilian.app

import android.app.Application
import com.nilian.app.core.database.NilianDatabase
import com.nilian.app.core.datastore.SecurityPreferences
import com.nilian.app.data.repository.EventRepositoryImpl
import com.nilian.app.data.repository.GoalRepositoryImpl
import com.nilian.app.data.repository.HabitRepositoryImpl
import com.nilian.app.data.repository.TaskRepositoryImpl
import com.nilian.app.data.repository.TimeBlockRepositoryImpl
import com.nilian.app.domain.repository.EventRepository
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.HabitRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.domain.repository.TimeBlockRepository
import com.nilian.app.domain.usecase.DetectCollisionsUseCase
import com.nilian.app.domain.usecase.FreeSlotFinderUseCase
import com.nilian.app.domain.usecase.HabitStreakCalculatorUseCase
import com.nilian.app.domain.usecase.JsonBackupRestoreUseCase
import com.nilian.app.domain.usecase.TaskRolloverUseCase
import com.nilian.app.domain.usecase.WorkloadStressUseCase

/**
 * Main Application class initializing local database, secure preferences,
 * repositories, and deterministic use cases.
 */
class NilianApp : Application() {

    lateinit var database: NilianDatabase
        private set

    lateinit var securityPreferences: SecurityPreferences
        private set

    lateinit var taskRepository: TaskRepository
        private set

    lateinit var eventRepository: EventRepository
        private set

    lateinit var habitRepository: HabitRepository
        private set

    lateinit var timeBlockRepository: TimeBlockRepository
        private set

    lateinit var goalRepository: GoalRepository
        private set

    lateinit var detectCollisionsUseCase: DetectCollisionsUseCase
        private set

    lateinit var habitStreakCalculatorUseCase: HabitStreakCalculatorUseCase
        private set

    lateinit var taskRolloverUseCase: TaskRolloverUseCase
        private set

    lateinit var freeSlotFinderUseCase: FreeSlotFinderUseCase
        private set

    lateinit var workloadStressUseCase: WorkloadStressUseCase
        private set

    lateinit var jsonBackupRestoreUseCase: JsonBackupRestoreUseCase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize Room Local Database
        database = NilianDatabase.getInstance(this)

        // 2. Initialize DataStore Preferences
        securityPreferences = SecurityPreferences.getInstance(this)

        // 3. Initialize Repositories
        taskRepository = TaskRepositoryImpl(database.taskDao())
        eventRepository = EventRepositoryImpl(database.eventDao())
        habitRepository = HabitRepositoryImpl(database.habitDao())
        timeBlockRepository = TimeBlockRepositoryImpl(database.timeBlockDao())
        goalRepository = GoalRepositoryImpl(database.goalDao())

        // 4. Initialize Pure Deterministic UseCases
        detectCollisionsUseCase = DetectCollisionsUseCase()
        habitStreakCalculatorUseCase = HabitStreakCalculatorUseCase()
        taskRolloverUseCase = TaskRolloverUseCase(taskRepository)
        freeSlotFinderUseCase = FreeSlotFinderUseCase()
        workloadStressUseCase = WorkloadStressUseCase()

        jsonBackupRestoreUseCase = JsonBackupRestoreUseCase(
            taskRepository = taskRepository,
            eventRepository = eventRepository,
            habitRepository = habitRepository,
            timeBlockRepository = timeBlockRepository,
            goalRepository = goalRepository
        )
    }

    companion object {
        lateinit var instance: NilianApp
            private set
    }
}
