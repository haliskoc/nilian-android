package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.FreeTimeSlot
import com.nilian.app.domain.model.MorningKickoffState
import com.nilian.app.domain.model.MorningKickoffSummary
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.repository.EventRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.domain.repository.TimeBlockRepository
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Deterministic morning kickoff engine for Nilian.
 *
 * Responsibilities:
 * 1. Evaluates overdue / rollover tasks from prior days that require attention.
 * 2. Analyzes today's commitments (Events and TimeBlocks) and calculates free focus windows.
 * 3. Recommends the Top 3 priority focus tasks based on priority, rollover status, and time budget.
 * 4. Generates a calm, mindful morning prompt.
 * 5. Manages and persists morning kickoff state.
 */
class MorningKickoffUseCase(
    private val taskRepository: TaskRepository? = null,
    private val eventRepository: EventRepository? = null,
    private val timeBlockRepository: TimeBlockRepository? = null,
    private val freeSlotFinderUseCase: FreeSlotFinderUseCase = FreeSlotFinderUseCase(),
    private val taskRolloverUseCase: TaskRolloverUseCase = TaskRolloverUseCase(taskRepository)
) {

    // In-memory cache for the latest kickoff state (keyed by date)
    private val kickoffStateCache = mutableMapOf<LocalDate, MorningKickoffState>()

    /**
     * Pure function to generate a [MorningKickoffSummary] from in-memory collections.
     */
    operator fun invoke(
        today: LocalDate = LocalDate.now(),
        allTasks: List<Task>,
        todayEvents: List<Event>,
        todayTimeBlocks: List<TimeBlock>,
        savedState: MorningKickoffState? = null
    ): MorningKickoffSummary {
        // 1. Identify Overdue Rollover Tasks
        val overdueRolloverTasks = taskRolloverUseCase.findTasksToRollover(allTasks, today)

        // 2. Identify Today's Tasks
        val todayTasks = allTasks.filter { task ->
            !task.isCompleted && (task.dueDate == today || (task.dueDate != null && task.dueDate.isBefore(today)))
        }

        // 3. Calculate Free Time Slots on the 24-hour timeline
        val freeSlots = freeSlotFinderUseCase(
            events = todayEvents,
            timeBlocks = todayTimeBlocks,
            date = today
        )
        val totalFreeMinutes = freeSlots.sumOf { it.durationMinutes }

        // 4. Calculate Scheduled Commitment Minutes
        var totalScheduledMinutes = 0L
        for (event in todayEvents) {
            val duration = java.time.Duration.between(event.startDateTime, event.endDateTime).toMinutes()
            if (duration > 0) totalScheduledMinutes += duration
        }
        for (block in todayTimeBlocks) {
            val duration = if (block.endTime.isAfter(block.startTime)) {
                java.time.Duration.between(block.startTime, block.endTime).toMinutes()
            } else {
                java.time.Duration.between(block.startTime, java.time.LocalTime.MAX).toMinutes()
            }
            if (duration > 0) totalScheduledMinutes += duration
        }

        // 5. Select or Recommend Top 3 Focus Tasks
        val recommendedTasks = recommendTopFocusTasks(todayTasks, maxCount = 3)
        val currentState = savedState ?: kickoffStateCache[today] ?: MorningKickoffState(date = today)

        val selectedFocusTasks = if (currentState.selectedFocusTaskIds.isNotEmpty()) {
            val idSet = currentState.selectedFocusTaskIds.toSet()
            val selected = allTasks.filter { it.id in idSet }
            if (selected.isNotEmpty()) selected else recommendedTasks
        } else {
            recommendedTasks
        }

        // 6. Generate Calm Morning Prompt
        val prompt = generateMorningPrompt(
            date = today,
            focusTasks = selectedFocusTasks,
            todayEvents = todayEvents,
            freeMinutes = totalFreeMinutes,
            overdueCount = overdueRolloverTasks.size
        )

        return MorningKickoffSummary(
            date = today,
            overdueRolloverTasks = overdueRolloverTasks,
            todayTasks = todayTasks,
            todayEvents = todayEvents,
            todayTimeBlocks = todayTimeBlocks,
            recommendedFocusTasks = recommendedTasks,
            selectedFocusTasks = selectedFocusTasks,
            freeSlots = freeSlots,
            totalFreeSlotMinutes = totalFreeMinutes,
            totalScheduledMinutes = totalScheduledMinutes,
            morningPrompt = prompt,
            state = currentState
        )
    }

    /**
     * Deterministic recommendation algorithm for Top Focus Tasks:
     * - Filters out completed tasks.
     * - Ranks by Priority (HIGH -> MEDIUM -> LOW).
     * - Prioritizes overdue / rollover tasks over newly added tasks.
     * - Ranks by estimated duration (favoring reasonable focus durations).
     */
    fun recommendTopFocusTasks(
        tasks: List<Task>,
        maxCount: Int = 3
    ): List<Task> {
        val pending = tasks.filter { !it.isCompleted }

        return pending.sortedWith(
            compareBy<Task> { task ->
                when (task.priority) {
                    Priority.HIGH -> 0
                    Priority.MEDIUM -> 1
                    Priority.LOW -> 2
                }
            }
                .thenBy { task ->
                    // Overdue tasks come first
                    if (task.dueDate != null && task.dueDate.isBefore(LocalDate.now())) 0 else 1
                }
                .thenBy { task ->
                    task.dueDate ?: LocalDate.MAX
                }
                .thenBy { task ->
                    // Prefer tasks with clear estimates (e.g. 15-90 min)
                    if (task.estimatedDurationMinutes in 15..120) 0 else 1
                }
                .thenBy { it.id }
        ).take(maxCount)
    }

    /**
     * Generates an empowering, calm morning greeting and strategic recommendation.
     */
    fun generateMorningPrompt(
        date: LocalDate,
        focusTasks: List<Task>,
        todayEvents: List<Event>,
        freeMinutes: Long,
        overdueCount: Int
    ): String {
        val freeHoursText = if (freeMinutes >= 60) {
            val hours = freeMinutes / 60
            val remMinutes = freeMinutes % 60
            if (remMinutes > 0) "$hours saat $remMinutes dk" else "$hours saat"
        } else {
            "$freeMinutes dk"
        }

        val eventSummary = when {
            todayEvents.isEmpty() -> "Bugün takviminde sabit bir toplantı yok."
            todayEvents.size == 1 -> "Bugün 1 sabit etkinliğin bulunuyor (${todayEvents.first().title})."
            else -> "Bugün ${todayEvents.size} sabit etkinliğin planlandı."
        }

        val rolloverSummary = if (overdueCount > 0) {
            " Dünden devreden $overdueCount görev bulunuyor; sabah enerjini bunları toparlamaya ayırabilirsin."
        } else {
            ""
        }

        val focusSummary = if (focusTasks.isNotEmpty()) {
            " Seçtiğin ${focusTasks.size} ana odak görevi için $freeHoursText serbest zamanın mevcut."
        } else {
            " Bugün için $freeHoursText serbest zamanın bulunuyor."
        }

        return "Günaydın. $eventSummary$focusSummary$rolloverSummary Sakin ve kararlı bir tempoda ilerle."
    }

    /**
     * Asynchronously loads today's data from repositories and generates the kickoff summary.
     */
    suspend fun getKickoffSummary(
        today: LocalDate = LocalDate.now(),
        savedState: MorningKickoffState? = null
    ): MorningKickoffSummary {
        val tasks = taskRepository?.getAllTasksSync() ?: emptyList()
        val events = eventRepository?.getAllEventsSync()?.filter { event ->
            event.startDateTime.toLocalDate() == today || event.endDateTime.toLocalDate() == today
        } ?: emptyList()
        val blocks = timeBlockRepository?.getAllTimeBlocksSync()?.filter { it.date == today } ?: emptyList()

        return invoke(
            today = today,
            allTasks = tasks,
            todayEvents = events,
            todayTimeBlocks = blocks,
            savedState = savedState
        )
    }

    /**
     * Executes morning kickoff, persists selected focus tasks and intention note,
     * and optionally auto-rolls over pending tasks to today.
     */
    suspend fun completeKickoff(
        today: LocalDate = LocalDate.now(),
        selectedTaskIds: List<Long>,
        intentionNote: String? = null,
        autoRolloverOverdue: Boolean = true
    ): MorningKickoffSummary {
        if (autoRolloverOverdue) {
            taskRolloverUseCase.execute(today)
        }

        val newState = MorningKickoffState(
            date = today,
            selectedFocusTaskIds = selectedTaskIds,
            intentionNote = intentionNote,
            isCompleted = true,
            completedAt = LocalDateTime.now()
        )

        kickoffStateCache[today] = newState
        return getKickoffSummary(today, savedState = newState)
    }

    /**
     * Retrieves the persisted state for a given date.
     */
    fun getState(date: LocalDate = LocalDate.now()): MorningKickoffState {
        return kickoffStateCache[date] ?: MorningKickoffState(date = date)
    }

    /**
     * Saves or updates kickoff state in memory.
     */
    fun saveState(state: MorningKickoffState) {
        kickoffStateCache[state.date] = state
    }
}
