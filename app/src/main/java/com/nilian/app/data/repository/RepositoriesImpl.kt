package com.nilian.app.data.repository

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
import com.nilian.app.domain.model.DailyRitual
import com.nilian.app.domain.model.DayTemplate
import com.nilian.app.domain.model.DayTemplateWithBlocks
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.HabitWithLogs
import com.nilian.app.domain.model.InboxNote
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TemplateBlock
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.repository.EventRepository
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.HabitRepository
import com.nilian.app.domain.repository.InboxRepository
import com.nilian.app.domain.repository.RitualRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.domain.repository.TemplateRepository
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

/**
 * Concrete implementation of [InboxRepository] using Room [InboxNoteDao].
 */
class InboxRepositoryImpl(
    private val inboxNoteDao: InboxNoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : InboxRepository {

    override fun getActiveNotes(): Flow<List<InboxNote>> {
        return inboxNoteDao.getActiveNotes()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getArchivedNotes(): Flow<List<InboxNote>> {
        return inboxNoteDao.getArchivedNotes()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getAllNotes(): Flow<List<InboxNote>> {
        return inboxNoteDao.getAllNotes()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getNoteById(id: Long): InboxNote? = withContext(ioDispatcher) {
        inboxNoteDao.getNoteById(id)?.toDomain()
    }

    override suspend fun insertNote(note: InboxNote): Long = withContext(ioDispatcher) {
        inboxNoteDao.insert(InboxNoteEntity.fromDomain(note))
    }

    override suspend fun insertNotes(notes: List<InboxNote>): List<Long> = withContext(ioDispatcher) {
        inboxNoteDao.insertAll(notes.map { InboxNoteEntity.fromDomain(it) })
    }

    override suspend fun updateNote(note: InboxNote) = withContext(ioDispatcher) {
        inboxNoteDao.update(InboxNoteEntity.fromDomain(note))
        Unit
    }

    override suspend fun archiveNote(id: Long, isArchived: Boolean) = withContext(ioDispatcher) {
        inboxNoteDao.setArchived(id, isArchived)
        Unit
    }

    override suspend fun deleteNote(note: InboxNote) = withContext(ioDispatcher) {
        inboxNoteDao.delete(InboxNoteEntity.fromDomain(note))
        Unit
    }

    override suspend fun deleteNoteById(id: Long) = withContext(ioDispatcher) {
        inboxNoteDao.deleteById(id)
        Unit
    }

    override suspend fun getActiveNotesSync(): List<InboxNote> = withContext(ioDispatcher) {
        inboxNoteDao.getActiveNotesSync().map { it.toDomain() }
    }

    override suspend fun getAllNotesSync(): List<InboxNote> = withContext(ioDispatcher) {
        inboxNoteDao.getAllNotesSync().map { it.toDomain() }
    }

    override suspend fun deleteAllNotes() = withContext(ioDispatcher) {
        inboxNoteDao.deleteAllNotes()
        Unit
    }
}

/**
 * Concrete implementation of [TemplateRepository] using Room [DayTemplateDao].
 */
class TemplateRepositoryImpl(
    private val dayTemplateDao: DayTemplateDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<DayTemplate>> {
        return dayTemplateDao.getAllTemplates()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getTemplatesWithBlocks(): Flow<List<DayTemplateWithBlocks>> {
        return dayTemplateDao.getTemplatesWithBlocks()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getTemplateWithBlocks(id: Long): Flow<DayTemplateWithBlocks?> {
        return dayTemplateDao.getTemplateWithBlocks(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun getBlocksForTemplate(templateId: Long): Flow<List<TemplateBlock>> {
        return dayTemplateDao.getBlocksForTemplate(templateId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getTemplateById(id: Long): DayTemplate? = withContext(ioDispatcher) {
        dayTemplateDao.getTemplateById(id)?.toDomain()
    }

    override suspend fun getTemplateWithBlocksSync(id: Long): DayTemplateWithBlocks? = withContext(ioDispatcher) {
        dayTemplateDao.getTemplateWithBlocksSync(id)?.toDomain()
    }

    override suspend fun insertTemplate(template: DayTemplate): Long = withContext(ioDispatcher) {
        dayTemplateDao.insertTemplate(DayTemplateEntity.fromDomain(template))
    }

    override suspend fun insertTemplateWithBlocks(
        template: DayTemplate,
        blocks: List<TemplateBlock>
    ): Long = withContext(ioDispatcher) {
        dayTemplateDao.insertTemplateWithBlocks(
            DayTemplateEntity.fromDomain(template),
            blocks.map { TemplateBlockEntity.fromDomain(it) }
        )
    }

    override suspend fun insertTemplateBlock(block: TemplateBlock): Long = withContext(ioDispatcher) {
        dayTemplateDao.insertTemplateBlock(TemplateBlockEntity.fromDomain(block))
    }

    override suspend fun insertTemplateBlocks(blocks: List<TemplateBlock>): List<Long> = withContext(ioDispatcher) {
        dayTemplateDao.insertTemplateBlocks(blocks.map { TemplateBlockEntity.fromDomain(it) })
    }

    override suspend fun updateTemplate(template: DayTemplate) = withContext(ioDispatcher) {
        dayTemplateDao.updateTemplate(DayTemplateEntity.fromDomain(template))
        Unit
    }

    override suspend fun deleteTemplate(template: DayTemplate) = withContext(ioDispatcher) {
        dayTemplateDao.deleteTemplate(DayTemplateEntity.fromDomain(template))
        Unit
    }

    override suspend fun deleteTemplateById(id: Long) = withContext(ioDispatcher) {
        dayTemplateDao.deleteTemplateById(id)
        Unit
    }

    override suspend fun deleteTemplateBlock(block: TemplateBlock) = withContext(ioDispatcher) {
        dayTemplateDao.deleteTemplateBlock(TemplateBlockEntity.fromDomain(block))
        Unit
    }

    override suspend fun deleteTemplateBlockById(id: Long) = withContext(ioDispatcher) {
        dayTemplateDao.deleteTemplateBlockById(id)
        Unit
    }

    override suspend fun getAllTemplatesSync(): List<DayTemplate> = withContext(ioDispatcher) {
        dayTemplateDao.getAllTemplatesSync().map { it.toDomain() }
    }

    override suspend fun getAllTemplatesWithBlocksSync(): List<DayTemplateWithBlocks> = withContext(ioDispatcher) {
        dayTemplateDao.getTemplatesWithBlocksSync().map { it.toDomain() }
    }

    override suspend fun deleteAllTemplates() = withContext(ioDispatcher) {
        dayTemplateDao.deleteAllTemplates()
        Unit
    }
}

/**
 * Concrete implementation of [RitualRepository] using Room [DailyRitualDao].
 */
class RitualRepositoryImpl(
    private val dailyRitualDao: DailyRitualDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RitualRepository {

    override fun getRitualForDate(date: LocalDate): Flow<DailyRitual?> {
        return dailyRitualDao.getRitualForDate(date)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun getAllRituals(): Flow<List<DailyRitual>> {
        return dailyRitualDao.getAllRituals()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getRitualForDateSync(date: LocalDate): DailyRitual? = withContext(ioDispatcher) {
        dailyRitualDao.getRitualForDateSync(date)?.toDomain()
    }

    override suspend fun getRitualById(id: Long): DailyRitual? = withContext(ioDispatcher) {
        dailyRitualDao.getRitualById(id)?.toDomain()
    }

    override suspend fun insertOrUpdateRitual(ritual: DailyRitual): Long = withContext(ioDispatcher) {
        dailyRitualDao.insertOrUpdate(DailyRitualEntity.fromDomain(ritual))
    }

    override suspend fun insertRituals(rituals: List<DailyRitual>): List<Long> = withContext(ioDispatcher) {
        dailyRitualDao.insertAll(rituals.map { DailyRitualEntity.fromDomain(it) })
    }

    override suspend fun updateRitual(ritual: DailyRitual) = withContext(ioDispatcher) {
        dailyRitualDao.update(DailyRitualEntity.fromDomain(ritual))
        Unit
    }

    override suspend fun deleteRitual(ritual: DailyRitual) = withContext(ioDispatcher) {
        dailyRitualDao.delete(DailyRitualEntity.fromDomain(ritual))
        Unit
    }

    override suspend fun deleteRitualForDate(date: LocalDate) = withContext(ioDispatcher) {
        dailyRitualDao.deleteForDate(date)
        Unit
    }

    override suspend fun deleteRitualById(id: Long) = withContext(ioDispatcher) {
        dailyRitualDao.deleteById(id)
        Unit
    }

    override suspend fun getAllRitualsSync(): List<DailyRitual> = withContext(ioDispatcher) {
        dailyRitualDao.getAllRitualsSync().map { it.toDomain() }
    }

    override suspend fun deleteAllRituals() = withContext(ioDispatcher) {
        dailyRitualDao.deleteAllRituals()
        Unit
    }
}
