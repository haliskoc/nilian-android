package com.nilian.app.domain.repository

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

/**
 * Domain repository interface for Quick Brain Dump Inbox Notes.
 */
interface InboxRepository {
    fun getActiveNotes(): Flow<List<InboxNote>>
    fun getArchivedNotes(): Flow<List<InboxNote>>
    fun getAllNotes(): Flow<List<InboxNote>>

    suspend fun getNoteById(id: Long): InboxNote?
    suspend fun insertNote(note: InboxNote): Long
    suspend fun insertNotes(notes: List<InboxNote>): List<Long>
    suspend fun updateNote(note: InboxNote)
    suspend fun archiveNote(id: Long, isArchived: Boolean)
    suspend fun deleteNote(note: InboxNote)
    suspend fun deleteNoteById(id: Long)

    suspend fun getActiveNotesSync(): List<InboxNote> = emptyList()
    suspend fun getAllNotesSync(): List<InboxNote> = emptyList()
    suspend fun deleteAllNotes() {}
}

/**
 * Domain repository interface for Day Templates and Template Blocks.
 */
interface TemplateRepository {
    fun getAllTemplates(): Flow<List<DayTemplate>>
    fun getTemplatesWithBlocks(): Flow<List<DayTemplateWithBlocks>>
    fun getTemplateWithBlocks(id: Long): Flow<DayTemplateWithBlocks?>
    fun getBlocksForTemplate(templateId: Long): Flow<List<TemplateBlock>>

    suspend fun getTemplateById(id: Long): DayTemplate?
    suspend fun getTemplateWithBlocksSync(id: Long): DayTemplateWithBlocks?
    suspend fun insertTemplate(template: DayTemplate): Long
    suspend fun insertTemplateWithBlocks(template: DayTemplate, blocks: List<TemplateBlock>): Long
    suspend fun insertTemplateBlock(block: TemplateBlock): Long
    suspend fun insertTemplateBlocks(blocks: List<TemplateBlock>): List<Long>
    suspend fun updateTemplate(template: DayTemplate)
    suspend fun deleteTemplate(template: DayTemplate)
    suspend fun deleteTemplateById(id: Long)
    suspend fun deleteTemplateBlock(block: TemplateBlock)
    suspend fun deleteTemplateBlockById(id: Long)

    suspend fun getAllTemplatesSync(): List<DayTemplate> = emptyList()
    suspend fun getAllTemplatesWithBlocksSync(): List<DayTemplateWithBlocks> = emptyList()
    suspend fun deleteAllTemplates() {}
}

/**
 * Domain repository interface for Daily Rituals (Morning/Evening reviews).
 */
interface RitualRepository {
    fun getRitualForDate(date: LocalDate): Flow<DailyRitual?>
    fun getAllRituals(): Flow<List<DailyRitual>>

    suspend fun getRitualForDateSync(date: LocalDate): DailyRitual?
    suspend fun getRitualById(id: Long): DailyRitual?
    suspend fun insertOrUpdateRitual(ritual: DailyRitual): Long
    suspend fun insertRituals(rituals: List<DailyRitual>): List<Long>
    suspend fun updateRitual(ritual: DailyRitual)
    suspend fun deleteRitual(ritual: DailyRitual)
    suspend fun deleteRitualForDate(date: LocalDate)
    suspend fun deleteRitualById(id: Long)

    suspend fun getAllRitualsSync(): List<DailyRitual> = emptyList()
    suspend fun deleteAllRituals() {}
}
