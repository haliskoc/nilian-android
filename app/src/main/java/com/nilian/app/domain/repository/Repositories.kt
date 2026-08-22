package com.nilian.app.domain.repository

import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.HabitWithLogs
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Domain repository interface for managing actionable tasks.
 */
interface TaskRepository {
    fun getTasksForDate(date: LocalDate): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByGoal(goalId: Long): Flow<List<Task>>
    fun getPendingRolloverTasks(beforeDate: LocalDate): Flow<List<Task>>

    suspend fun getPendingRolloverTasksList(beforeDate: LocalDate): List<Task>
    suspend fun getTaskById(id: Long): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun insertTasks(tasks: List<Task>): List<Long>
    suspend fun updateTask(task: Task)
    suspend fun updateTasks(tasks: List<Task>) {
        tasks.forEach { updateTask(it) }
    }
    suspend fun deleteTask(task: Task)
    suspend fun deleteTaskById(id: Long)
    suspend fun rolloverPendingTasks(targetDate: LocalDate)

    suspend fun getAllTasksSync(): List<Task> = emptyList()
    suspend fun deleteAllTasks() {}
}

/**
 * Domain repository interface for managing scheduled calendar events.
 */
interface EventRepository {
    fun getEventsForDate(date: LocalDate): Flow<List<Event>>
    fun getEventsBetween(start: LocalDateTime, end: LocalDateTime): Flow<List<Event>>
    fun getAllEvents(): Flow<List<Event>>

    suspend fun getEventById(id: Long): Event?
    suspend fun insertEvent(event: Event): Long
    suspend fun insertEvents(events: List<Event>): List<Long>
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun deleteEventById(id: Long)

    suspend fun getAllEventsSync(): List<Event> = emptyList()
    suspend fun deleteAllEvents() {}
}

/**
 * Domain repository interface for managing habits and daily habit completion logs.
 */
interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs?>
    fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>>
    fun getLogsForDate(date: LocalDate): Flow<List<HabitLog>>
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    suspend fun getHabitById(id: Long): Habit?
    suspend fun insertHabit(habit: Habit): Long
    suspend fun insertHabits(habits: List<Habit>): List<Long> {
        return habits.map { insertHabit(it) }
    }
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun deleteHabitById(id: Long)

    suspend fun toggleHabitLog(habitId: Long, date: LocalDate, isCompleted: Boolean)
    suspend fun deleteHabitLog(habitId: Long, date: LocalDate)

    suspend fun getAllHabitsSync(): List<Habit> = emptyList()
    suspend fun getAllHabitLogsSync(): List<HabitLog> = emptyList()
    suspend fun insertHabitLogs(logs: List<HabitLog>) {}
    suspend fun deleteAllHabits() {}
    suspend fun deleteAllHabitLogs() {}
}

/**
 * Domain repository interface for managing 24-hour timeline blocks.
 */
interface TimeBlockRepository {
    fun getTimeBlocksForDate(date: LocalDate): Flow<List<TimeBlock>>
    fun getAllTimeBlocks(): Flow<List<TimeBlock>>

    suspend fun getTimeBlockById(id: Long): TimeBlock?
    suspend fun insertTimeBlock(timeBlock: TimeBlock): Long
    suspend fun insertTimeBlocks(timeBlocks: List<TimeBlock>): List<Long>
    suspend fun updateTimeBlock(timeBlock: TimeBlock)
    suspend fun deleteTimeBlock(timeBlock: TimeBlock)
    suspend fun deleteTimeBlockById(id: Long)

    suspend fun getAllTimeBlocksSync(): List<TimeBlock> = emptyList()
    suspend fun deleteAllTimeBlocks() {}
}

/**
 * Domain repository interface for managing long-term vision goals.
 */
interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    fun getActiveGoals(): Flow<List<Goal>>

    suspend fun getGoalById(id: Long): Goal?
    suspend fun insertGoal(goal: Goal): Long
    suspend fun insertGoals(goals: List<Goal>): List<Long>
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goal: Goal)
    suspend fun deleteGoalById(id: Long)

    suspend fun getAllGoalsSync(): List<Goal> = emptyList()
    suspend fun deleteAllGoals() {}
}
