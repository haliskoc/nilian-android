package com.nilian.app.data.repository

import com.nilian.app.data.local.dao.EventDao
import com.nilian.app.data.local.dao.GoalDao
import com.nilian.app.data.local.dao.HabitDao
import com.nilian.app.data.local.dao.TaskDao
import com.nilian.app.data.local.dao.TimeBlockDao
import com.nilian.app.data.local.entity.EventEntity
import com.nilian.app.data.local.entity.GoalEntity
import com.nilian.app.data.local.entity.HabitEntity
import com.nilian.app.data.local.entity.HabitLogEntity
import com.nilian.app.data.local.entity.TaskEntity
import com.nilian.app.data.local.entity.TimeBlockEntity
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.HabitWithLogs
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.repository.EventRepository
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.HabitRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.domain.repository.TimeBlockRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Concrete implementation of [TaskRepository] using Room [TaskDao].
 */
class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getTasksForDate(date: LocalDate): Flow<List<Task>> {
        return taskDao.getTasksForDate(date)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getTasksByGoal(goalId: Long): Flow<List<Task>> {
        return taskDao.getTasksByGoal(goalId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getPendingRolloverTasks(beforeDate: LocalDate): Flow<List<Task>> {
        return taskDao.getPendingRolloverTasks(beforeDate)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getPendingRolloverTasksList(beforeDate: LocalDate): List<Task> = withContext(ioDispatcher) {
        taskDao.getPendingRolloverTasksList(beforeDate).map { it.toDomain() }
    }

    override suspend fun getTaskById(id: Long): Task? = withContext(ioDispatcher) {
        taskDao.getTaskById(id)?.toDomain()
    }

    override suspend fun insertTask(task: Task): Long = withContext(ioDispatcher) {
        taskDao.insertTask(TaskEntity.fromDomain(task))
    }

    override suspend fun insertTasks(tasks: List<Task>): List<Long> = withContext(ioDispatcher) {
        taskDao.insertTasks(tasks.map { TaskEntity.fromDomain(it) })
    }

    override suspend fun updateTask(task: Task) = withContext(ioDispatcher) {
        taskDao.updateTask(TaskEntity.fromDomain(task))
        Unit
    }

    override suspend fun updateTasks(tasks: List<Task>) = withContext(ioDispatcher) {
        taskDao.updateTasks(tasks.map { TaskEntity.fromDomain(it) })
        Unit
    }

    override suspend fun deleteTask(task: Task) = withContext(ioDispatcher) {
        taskDao.deleteTask(TaskEntity.fromDomain(task))
        Unit
    }

    override suspend fun deleteTaskById(id: Long) = withContext(ioDispatcher) {
        taskDao.deleteTaskById(id)
        Unit
    }

    override suspend fun rolloverPendingTasks(targetDate: LocalDate) = withContext(ioDispatcher) {
        taskDao.rolloverTasks(targetDate, targetDate)
        Unit
    }

    override suspend fun getAllTasksSync(): List<Task> = withContext(ioDispatcher) {
        taskDao.getAllTasksSync().map { it.toDomain() }
    }

    override suspend fun deleteAllTasks() = withContext(ioDispatcher) {
        taskDao.deleteAllTasks()
        Unit
    }
}

/**
 * Concrete implementation of [EventRepository] using Room [EventDao].
 */
class EventRepositoryImpl(
    private val eventDao: EventDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : EventRepository {

    override fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getEventsForDate(date: LocalDate): Flow<List<Event>> {
        return eventDao.getEventsForDate(date)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getEventsBetween(start: LocalDateTime, end: LocalDateTime): Flow<List<Event>> {
        return eventDao.getEventsBetween(start, end)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getEventById(id: Long): Event? = withContext(ioDispatcher) {
        eventDao.getEventById(id)?.toDomain()
    }

    override suspend fun getAllEventsSync(): List<Event> = withContext(ioDispatcher) {
        eventDao.getAllEventsSync().map { it.toDomain() }
    }

    override suspend fun insertEvent(event: Event): Long = withContext(ioDispatcher) {
        eventDao.insertEvent(EventEntity.fromDomain(event))
    }

    override suspend fun insertEvents(events: List<Event>): List<Long> = withContext(ioDispatcher) {
        eventDao.insertEvents(events.map { EventEntity.fromDomain(it) })
    }

    override suspend fun updateEvent(event: Event) = withContext(ioDispatcher) {
        eventDao.updateEvent(EventEntity.fromDomain(event))
        Unit
    }

    override suspend fun deleteEvent(event: Event) = withContext(ioDispatcher) {
        eventDao.deleteEvent(EventEntity.fromDomain(event))
        Unit
    }

    override suspend fun deleteEventById(id: Long) = withContext(ioDispatcher) {
        eventDao.deleteEventById(id)
        Unit
    }

    override suspend fun deleteAllEvents() = withContext(ioDispatcher) {
        eventDao.deleteAllEvents()
        Unit
    }
}

/**
 * Concrete implementation of [HabitRepository] using Room [HabitDao].
 */
class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs?> {
        return habitDao.getHabitWithLogs(habitId)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>> {
        return habitDao.getAllHabitsWithLogs()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getLogsForDate(date: LocalDate): Flow<List<HabitLog>> {
        return habitDao.getLogsForDate(date)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> {
        return habitDao.getLogsForHabit(habitId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getHabitById(id: Long): Habit? = withContext(ioDispatcher) {
        habitDao.getHabitById(id)?.toDomain()
    }

    override suspend fun getAllHabitsSync(): List<Habit> = withContext(ioDispatcher) {
        habitDao.getAllHabitsSync().map { it.toDomain() }
    }

    override suspend fun getAllHabitLogsSync(): List<HabitLog> = withContext(ioDispatcher) {
        habitDao.getAllHabitLogsSync().map { it.toDomain() }
    }

    override suspend fun insertHabit(habit: Habit): Long = withContext(ioDispatcher) {
        habitDao.insertHabit(HabitEntity.fromDomain(habit))
    }

    override suspend fun insertHabits(habits: List<Habit>): List<Long> = withContext(ioDispatcher) {
        habitDao.insertHabits(habits.map { HabitEntity.fromDomain(it) })
    }

    override suspend fun updateHabit(habit: Habit) = withContext(ioDispatcher) {
        habitDao.updateHabit(HabitEntity.fromDomain(habit))
        Unit
    }

    override suspend fun deleteHabit(habit: Habit) = withContext(ioDispatcher) {
        habitDao.deleteHabit(HabitEntity.fromDomain(habit))
        Unit
    }

    override suspend fun deleteHabitById(id: Long) = withContext(ioDispatcher) {
        habitDao.deleteHabitById(id)
        Unit
    }

    override suspend fun toggleHabitLog(habitId: Long, date: LocalDate, isCompleted: Boolean) = withContext(ioDispatcher) {
        if (isCompleted) {
            habitDao.insertLog(HabitLogEntity(habitId = habitId, date = date, isCompleted = true))
        } else {
            habitDao.deleteLogForDate(habitId = habitId, date = date)
        }
        Unit
    }

    override suspend fun deleteHabitLog(habitId: Long, date: LocalDate) = withContext(ioDispatcher) {
        habitDao.deleteLogForDate(habitId = habitId, date = date)
        Unit
    }

    override suspend fun insertHabitLogs(logs: List<HabitLog>) = withContext(ioDispatcher) {
        habitDao.insertLogs(logs.map { HabitLogEntity.fromDomain(it) })
    }

    override suspend fun deleteAllHabits() = withContext(ioDispatcher) {
        habitDao.deleteAllHabits()
        Unit
    }

    override suspend fun deleteAllHabitLogs() = withContext(ioDispatcher) {
        habitDao.deleteAllHabitLogs()
        Unit
    }
}

/**
 * Concrete implementation of [TimeBlockRepository] using Room [TimeBlockDao].
 */
class TimeBlockRepositoryImpl(
    private val timeBlockDao: TimeBlockDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TimeBlockRepository {

    override fun getAllTimeBlocks(): Flow<List<TimeBlock>> {
        return timeBlockDao.getAllTimeBlocks()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getTimeBlocksForDate(date: LocalDate): Flow<List<TimeBlock>> {
        return timeBlockDao.getTimeBlocksForDate(date)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getTimeBlockById(id: Long): TimeBlock? = withContext(ioDispatcher) {
        timeBlockDao.getTimeBlockById(id)?.toDomain()
    }

    override suspend fun getAllTimeBlocksSync(): List<TimeBlock> = withContext(ioDispatcher) {
        timeBlockDao.getAllTimeBlocksSync().map { it.toDomain() }
    }

    override suspend fun insertTimeBlock(timeBlock: TimeBlock): Long = withContext(ioDispatcher) {
        timeBlockDao.insert(TimeBlockEntity.fromDomain(timeBlock))
    }

    override suspend fun insertTimeBlocks(timeBlocks: List<TimeBlock>): List<Long> = withContext(ioDispatcher) {
        timeBlockDao.insertAll(timeBlocks.map { TimeBlockEntity.fromDomain(it) })
    }

    override suspend fun updateTimeBlock(timeBlock: TimeBlock) = withContext(ioDispatcher) {
        timeBlockDao.update(TimeBlockEntity.fromDomain(timeBlock))
        Unit
    }

    override suspend fun deleteTimeBlock(timeBlock: TimeBlock) = withContext(ioDispatcher) {
        timeBlockDao.delete(TimeBlockEntity.fromDomain(timeBlock))
        Unit
    }

    override suspend fun deleteTimeBlockById(id: Long) = withContext(ioDispatcher) {
        timeBlockDao.deleteById(id)
        Unit
    }

    override suspend fun deleteAllTimeBlocks() = withContext(ioDispatcher) {
        timeBlockDao.deleteAllTimeBlocks()
        Unit
    }
}

/**
 * Concrete implementation of [GoalRepository] using Room [GoalDao].
 */
class GoalRepositoryImpl(
    private val goalDao: GoalDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getActiveGoals(): Flow<List<Goal>> {
        return goalDao.getActiveGoals()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getGoalById(id: Long): Goal? = withContext(ioDispatcher) {
        goalDao.getGoalById(id)?.toDomain()
    }

    override suspend fun getAllGoalsSync(): List<Goal> = withContext(ioDispatcher) {
        goalDao.getAllGoalsSync().map { it.toDomain() }
    }

    override suspend fun insertGoal(goal: Goal): Long = withContext(ioDispatcher) {
        goalDao.insert(GoalEntity.fromDomain(goal))
    }

    override suspend fun insertGoals(goals: List<Goal>): List<Long> = withContext(ioDispatcher) {
        goalDao.insertAll(goals.map { GoalEntity.fromDomain(it) })
    }

    override suspend fun updateGoal(goal: Goal) = withContext(ioDispatcher) {
        goalDao.update(GoalEntity.fromDomain(goal))
        Unit
    }

    override suspend fun deleteGoal(goal: Goal) = withContext(ioDispatcher) {
        goalDao.delete(GoalEntity.fromDomain(goal))
        Unit
    }

    override suspend fun deleteGoalById(id: Long) = withContext(ioDispatcher) {
        goalDao.deleteById(id)
        Unit
    }

    override suspend fun deleteAllGoals() = withContext(ioDispatcher) {
        goalDao.deleteAllGoals()
        Unit
    }
}
