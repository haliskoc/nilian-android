package com.nilian.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

val Priority.label: String
    get() = when (this) {
        Priority.HIGH -> "High"
        Priority.MEDIUM -> "Medium"
        Priority.LOW -> "Low"
    }

val EventCategory.label: String
    get() = when (this) {
        EventCategory.LECTURE -> "Lecture"
        EventCategory.MEETING -> "Meeting"
        EventCategory.PERSONAL -> "Personal"
        EventCategory.WORK -> "Work"
        EventCategory.STUDY -> "Study"
        EventCategory.GENERAL -> "General"
        EventCategory.OTHER -> "Other"
    }

val BlockType.label: String
    get() = when (this) {
        BlockType.SLEEP -> "Sleep & Recharge"
        BlockType.WORKOUT -> "Workout / Movement"
        BlockType.STUDY -> "Study / Lecture"
        BlockType.DEEP_WORK -> "Deep Work / Sprint"
        BlockType.REST -> "Rest & Reflection"
        BlockType.BUFFER -> "Buffer / Transition"
        BlockType.GENERAL -> "General Focus"
        BlockType.OTHER -> "Other"
    }

// -----------------------------------------------------------------------------------------
// Core Pure Kotlin Domain Models
// -----------------------------------------------------------------------------------------

data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val estimatedDurationMinutes: Int = 30,
    val dueDate: LocalDate? = null,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val goalId: Long? = null,
    val autoRollover: Boolean = true
)

data class Event(
    val id: Long = 0L,
    val title: String,
    val locationOrLink: String? = null,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val category: EventCategory = EventCategory.GENERAL,
    val colorHex: String? = null
)

data class Habit(
    val id: Long = 0L,
    val title: String,
    val goalId: Long? = null,
    val targetDaysOfWeek: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    ),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: LocalDate = LocalDate.now()
)

data class HabitLog(
    val habitId: Long,
    val date: LocalDate,
    val isCompleted: Boolean = true
)

data class HabitWithLogs(
    val habit: Habit,
    val logs: List<HabitLog> = emptyList()
)

data class TimeBlock(
    val id: Long = 0L,
    val title: String,
    val blockType: BlockType = BlockType.GENERAL,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val date: LocalDate = LocalDate.now(),
    val linkedTaskId: Long? = null
)

data class Goal(
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val targetDate: LocalDate? = null,
    val progressPercent: Float = 0f,
    val isArchived: Boolean = false
)

// -----------------------------------------------------------------------------------------
// Domain Logic Engine Result Models
// -----------------------------------------------------------------------------------------

data class StreakResult(
    val currentStreak: Int,
    val bestStreak: Int,
    val isCompletedToday: Boolean
)

enum class ConflictSourceType {
    EVENT,
    TIME_BLOCK
}

data class ConflictSourceItem(
    val id: Long,
    val title: String,
    val type: ConflictSourceType,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime
)

data class ScheduleConflict(
    val itemA: ConflictSourceItem,
    val itemB: ConflictSourceItem,
    val overlapStart: LocalDateTime,
    val overlapEnd: LocalDateTime,
    val overlapDurationMinutes: Long
)

data class FreeTimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Long,
    val date: LocalDate,
    val suggestedActivity: String
)

enum class WorkloadLevel {
    LIGHT,
    BALANCED,
    MODERATE,
    HEAVY
}

data class WorkloadAssessment(
    val date: LocalDate,
    val taskMinutes: Int,
    val eventMinutes: Int,
    val blockMinutes: Int,
    val totalCommittedMinutes: Int,
    val workloadLevel: WorkloadLevel,
    val isHeavyLoad: Boolean,
    val suggestion: String
)

data class BackupData(
    val version: Int = 1,
    val exportTimestamp: LocalDateTime = LocalDateTime.now(),
    val tasks: List<Task> = emptyList(),
    val events: List<Event> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val habitLogs: List<HabitLog> = emptyList(),
    val timeBlocks: List<TimeBlock> = emptyList(),
    val goals: List<Goal> = emptyList()
)

// -----------------------------------------------------------------------------------------
// UI Presentation Layer Models (Used in Compose UI Screens)
// -----------------------------------------------------------------------------------------

data class TaskItem(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val estimatedDurationMinutes: Int = 30,
    val dueDate: LocalDate? = LocalDate.now(),
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val goalId: Long? = null,
    val goalTitle: String? = null,
    val autoRollover: Boolean = true,
    val isRollover: Boolean = false
)

data class EventItem(
    val id: Long = 0,
    val title: String,
    val locationOrLink: String? = null,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val category: EventCategory = EventCategory.GENERAL,
    val colorHex: String? = null,
    val hasConflict: Boolean = false
)

data class HabitItem(
    val id: Long = 0,
    val title: String,
    val goalId: Long? = null,
    val goalTitle: String? = null,
    val targetDaysOfWeek: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    ),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: LocalDate = LocalDate.now(),
    val isCompletedToday: Boolean = false,
    val weeklyHistory: List<Boolean> = listOf(false, false, false, false, false, false, false)
)

data class TimeBlockItem(
    val id: Long = 0,
    val title: String,
    val blockType: BlockType = BlockType.GENERAL,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val date: LocalDate = LocalDate.now(),
    val linkedTaskId: Long? = null,
    val hasConflict: Boolean = false
)

data class GoalItem(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val targetDate: LocalDate? = null,
    val progressPercent: Float = 0f,
    val isArchived: Boolean = false,
    val linkedTaskCount: Int = 0,
    val linkedHabitCount: Int = 0
)

data class FreeSlotItem(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Int
)

data class ConflictItem(
    val id: String = UUID.randomUUID().toString(),
    val titleA: String,
    val titleB: String,
    val timeDescription: String
)

// -----------------------------------------------------------------------------------------
// Mapping Extension Functions (Domain <-> UI Presentation)
// -----------------------------------------------------------------------------------------

fun Task.toItem(goalTitle: String? = null, isRollover: Boolean = false): TaskItem = TaskItem(
    id = id,
    title = title,
    description = description,
    priority = priority,
    estimatedDurationMinutes = estimatedDurationMinutes,
    dueDate = dueDate,
    isCompleted = isCompleted,
    completedAt = completedAt,
    goalId = goalId,
    goalTitle = goalTitle,
    autoRollover = autoRollover,
    isRollover = isRollover
)

fun TaskItem.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    priority = priority,
    estimatedDurationMinutes = estimatedDurationMinutes,
    dueDate = dueDate,
    isCompleted = isCompleted,
    completedAt = completedAt,
    goalId = goalId,
    autoRollover = autoRollover
)

fun Event.toItem(hasConflict: Boolean = false): EventItem = EventItem(
    id = id,
    title = title,
    locationOrLink = locationOrLink,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    category = category,
    colorHex = colorHex,
    hasConflict = hasConflict
)

fun EventItem.toDomain(): Event = Event(
    id = id,
    title = title,
    locationOrLink = locationOrLink,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    category = category,
    colorHex = colorHex
)

fun Habit.toItem(
    goalTitle: String? = null,
    isCompletedToday: Boolean = false,
    weeklyHistory: List<Boolean> = listOf(false, false, false, false, false, false, false)
): HabitItem = HabitItem(
    id = id,
    title = title,
    goalId = goalId,
    goalTitle = goalTitle,
    targetDaysOfWeek = targetDaysOfWeek,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    createdAt = createdAt,
    isCompletedToday = isCompletedToday,
    weeklyHistory = weeklyHistory
)

fun HabitItem.toDomain(): Habit = Habit(
    id = id,
    title = title,
    goalId = goalId,
    targetDaysOfWeek = targetDaysOfWeek,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    createdAt = createdAt
)

fun TimeBlock.toItem(hasConflict: Boolean = false): TimeBlockItem = TimeBlockItem(
    id = id,
    title = title,
    blockType = blockType,
    startTime = startTime,
    endTime = endTime,
    date = date,
    linkedTaskId = linkedTaskId,
    hasConflict = hasConflict
)

fun TimeBlockItem.toDomain(): TimeBlock = TimeBlock(
    id = id,
    title = title,
    blockType = blockType,
    startTime = startTime,
    endTime = endTime,
    date = date,
    linkedTaskId = linkedTaskId
)

fun Goal.toItem(linkedTaskCount: Int = 0, linkedHabitCount: Int = 0): GoalItem = GoalItem(
    id = id,
    title = title,
    description = description,
    targetDate = targetDate,
    progressPercent = progressPercent,
    isArchived = isArchived,
    linkedTaskCount = linkedTaskCount,
    linkedHabitCount = linkedHabitCount
)

fun GoalItem.toDomain(): Goal = Goal(
    id = id,
    title = title,
    description = description,
    targetDate = targetDate,
    progressPercent = progressPercent,
    isArchived = isArchived
)

fun ScheduleConflict.toUiConflictItem(): ConflictItem {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return ConflictItem(
        id = "${itemA.id}_${itemB.id}_${overlapStart}",
        titleA = itemA.title,
        titleB = itemB.title,
        timeDescription = "${overlapStart.format(formatter)} - ${overlapEnd.format(formatter)} (${overlapDurationMinutes} min)"
    )
}

fun FreeTimeSlot.toUiItem(): FreeSlotItem = FreeSlotItem(
    startTime = startTime,
    endTime = endTime,
    durationMinutes = durationMinutes.toInt()
)
