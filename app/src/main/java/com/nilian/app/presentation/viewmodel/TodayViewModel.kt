package com.nilian.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nilian.app.domain.model.ConflictItem
import com.nilian.app.domain.model.DayOfWeek
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.FreeSlotItem
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitItem
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TaskItem
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.model.toItem
import com.nilian.app.domain.model.toUiConflictItem
import com.nilian.app.domain.model.toUiItem
import com.nilian.app.domain.repository.EventRepository
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.HabitRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.domain.repository.TimeBlockRepository
import com.nilian.app.domain.usecase.DetectCollisionsUseCase
import com.nilian.app.domain.usecase.FreeSlotFinderUseCase
import com.nilian.app.domain.usecase.HabitStreakCalculatorUseCase
import com.nilian.app.domain.usecase.TaskRolloverUseCase
import com.nilian.app.domain.usecase.WorkloadStressUseCase
import com.nilian.app.presentation.today.DaySummary
import com.nilian.app.presentation.today.TodayUiState
import com.nilian.app.presentation.today.TodayViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

class TodayViewModel(
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val habitRepository: HabitRepository,
    private val timeBlockRepository: TimeBlockRepository,
    private val goalRepository: GoalRepository,
    private val detectCollisionsUseCase: DetectCollisionsUseCase = DetectCollisionsUseCase(),
    private val habitStreakCalculatorUseCase: HabitStreakCalculatorUseCase = HabitStreakCalculatorUseCase(),
    private val taskRolloverUseCase: TaskRolloverUseCase = TaskRolloverUseCase(taskRepository),
    private val freeSlotFinderUseCase: FreeSlotFinderUseCase = FreeSlotFinderUseCase(),
    private val workloadStressUseCase: WorkloadStressUseCase = WorkloadStressUseCase()
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _viewMode = MutableStateFlow(TodayViewMode.DAILY)
    private val _isQuickAddSheetVisible = MutableStateFlow(false)

    val uiState: StateFlow<TodayUiState> = combine(
        _selectedDate,
        _viewMode,
        _isQuickAddSheetVisible,
        taskRepository.getAllTasks(),
        eventRepository.getAllEvents(),
        habitRepository.getAllHabitsWithLogs(),
        timeBlockRepository.getAllTimeBlocks()
    ) { params ->
        val date = params[0] as LocalDate
        val mode = params[1] as TodayViewMode
        val isQuickAddVisible = params[2] as Boolean
        val allTasks = params[3] as List<Task>
        val allEvents = params[4] as List<Event>
        val allHabitsWithLogs = params[5] as List<com.nilian.app.domain.model.HabitWithLogs>
        val allTimeBlocks = params[6] as List<TimeBlock>

        // 1. Filter tasks for today or due today / overdue
        val todayTasks = allTasks.filter { task ->
            task.dueDate == null || task.dueDate == date || (!task.isCompleted && task.dueDate.isBefore(date))
        }.map { task ->
            task.toItem(isRollover = task.dueDate != null && task.dueDate.isBefore(date))
        }

        // 2. Filter events for today
        val todayEvents = allEvents.filter { event ->
            val eventStartDate = event.startDateTime.toLocalDate()
            val eventEndDate = event.endDateTime.toLocalDate()
            !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)
        }

        // 3. Filter time blocks for today
        val todayTimeBlocks = allTimeBlocks.filter { it.date == date }

        // 4. Map habits with streak calculation & today completion
        val habitItems = allHabitsWithLogs.map { hwl ->
            val streakResult = habitStreakCalculatorUseCase(hwl.habit, hwl.logs, date)
            val dayOfWeek = date.dayOfWeek
            val monday = date.minusDays(dayOfWeek.value.toLong() - 1)
            val weekHistory = (0..6).map { i ->
                val targetDay = monday.plusDays(i.toLong())
                hwl.logs.any { it.date == targetDay && it.isCompleted }
            }
            hwl.habit.toItem(
                isCompletedToday = streakResult.isCompletedToday,
                weeklyHistory = weekHistory
            ).copy(
                currentStreak = streakResult.currentStreak,
                bestStreak = streakResult.bestStreak
            )
        }

        // 5. Calculate collisions
        val collisions = detectCollisionsUseCase(todayEvents, todayTimeBlocks, date)
        val conflictUiItems = collisions.map { it.toUiConflictItem() }

        // 6. Calculate free slots
        val freeSlots = freeSlotFinderUseCase(todayEvents, todayTimeBlocks, date)
        val freeSlotItems = freeSlots.map { it.toUiItem() }

        // 7. Calculate workload stress
        val workload = workloadStressUseCase(allTasks, allEvents, allTimeBlocks, date)
        val workloadHours = (workload.totalCommittedMinutes / 60.0f)
        val workloadStatus = when (workload.workloadLevel) {
            com.nilian.app.domain.model.WorkloadLevel.LIGHT -> "Light & Restful"
            com.nilian.app.domain.model.WorkloadLevel.BALANCED -> "Balanced & Calm"
            com.nilian.app.domain.model.WorkloadLevel.MODERATE -> "Focused & Active"
            com.nilian.app.domain.model.WorkloadLevel.HEAVY -> "Heavy Load (Consider Rollover)"
        }

        // 8. Calculate Next-Up block or event
        val now = LocalTime.now()
        val nextBlock = todayTimeBlocks.filter { it.startTime.isAfter(now) || (it.startTime <= now && it.endTime > now) }
            .minByOrNull { it.startTime }

        val nextEvent = todayEvents.filter {
            it.startDateTime.toLocalDate() == date && (it.startDateTime.toLocalTime().isAfter(now) || (it.startDateTime.toLocalTime() <= now && it.endDateTime.toLocalTime() > now))
        }.minByOrNull { it.startDateTime }

        val nextUpRemainingMinutes: Int
        val nextUpProgress: Float
        if (nextBlock != null) {
            val start = nextBlock.startTime
            val end = nextBlock.endTime
            if (now.isBefore(start)) {
                nextUpRemainingMinutes = Duration.between(now, start).toMinutes().toInt()
                nextUpProgress = 0f
            } else {
                val totalMin = Duration.between(start, end).toMinutes().toFloat()
                val elapsedMin = Duration.between(start, now).toMinutes().toFloat()
                nextUpRemainingMinutes = Duration.between(now, end).toMinutes().toInt()
                nextUpProgress = if (totalMin > 0) (elapsedMin / totalMin).coerceIn(0f, 1f) else 0.5f
            }
        } else {
            nextUpRemainingMinutes = 0
            nextUpProgress = 0f
        }

        // 9. Calculate 7-Day summary for weekly view
        val monday = date.minusDays(date.dayOfWeek.value.toLong() - 1)
        val weekSummaries = (0..6).map { offset ->
            val dayDate = monday.plusDays(offset.toLong())
            val dayTasks = allTasks.filter { it.dueDate == dayDate }
            val dayEvents = allEvents.filter { it.startDateTime.toLocalDate() == dayDate }
            val ratio = if (dayTasks.isNotEmpty()) {
                dayTasks.count { it.isCompleted }.toFloat() / dayTasks.size.toFloat()
            } else 0f

            DaySummary(
                date = dayDate,
                dayName = dayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                taskCompletedRatio = ratio,
                isToday = dayDate == LocalDate.now(),
                eventCount = dayEvents.size
            )
        }

        TodayUiState(
            selectedDate = date,
            viewMode = mode,
            workloadScoreHours = Math.round(workloadHours * 10) / 10.0f,
            workloadStatus = workloadStatus,
            nextUpBlock = nextBlock?.toItem(),
            nextUpEvent = nextEvent?.toItem(),
            nextUpRemainingMinutes = nextUpRemainingMinutes,
            nextUpProgress = nextUpProgress,
            tasks = todayTasks,
            habits = habitItems,
            freeSlots = freeSlotItems,
            conflicts = conflictUiItems,
            weekSummaries = weekSummaries,
            isQuickAddSheetVisible = isQuickAddVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState()
    )

    init {
        // Automatic deterministic rollover on application launch
        viewModelScope.launch {
            taskRolloverUseCase.execute(LocalDate.now())
        }
    }

    fun onViewModeToggle(mode: TodayViewMode) {
        _viewMode.value = mode
    }

    fun onTaskToggle(taskItem: TaskItem) {
        viewModelScope.launch {
            val updated = taskItem.toDomain().copy(
                isCompleted = !taskItem.isCompleted,
                completedAt = if (!taskItem.isCompleted) LocalDateTime.now() else null
            )
            taskRepository.updateTask(updated)
        }
    }

    fun onHabitToggle(habitItem: HabitItem) {
        viewModelScope.launch {
            val newCompleted = !habitItem.isCompletedToday
            habitRepository.toggleHabitLog(habitItem.id, _selectedDate.value, newCompleted)
        }
    }

    fun onQuickAddClick() {
        _isQuickAddSheetVisible.value = true
    }

    fun onDismissQuickAddSheet() {
        _isQuickAddSheetVisible.value = false
    }

    fun onQuickAddOptionSelected(option: String) {
        _isQuickAddSheetVisible.value = false
        // Quick add templates
        viewModelScope.launch {
            when (option) {
                "task" -> {
                    taskRepository.insertTask(
                        Task(
                            title = "Yeni Görev",
                            dueDate = _selectedDate.value,
                            priority = Priority.MEDIUM
                        )
                    )
                }
                "block" -> {
                    val now = LocalTime.now()
                    timeBlockRepository.insertTimeBlock(
                        TimeBlock(
                            title = "Derin Odak Seansı",
                            blockType = com.nilian.app.domain.model.BlockType.DEEP_WORK,
                            startTime = now,
                            endTime = now.plusHours(1),
                            date = _selectedDate.value
                        )
                    )
                }
                "habit" -> {
                    habitRepository.insertHabit(
                        Habit(
                            title = "Yeni Alışkanlık",
                            createdAt = _selectedDate.value
                        )
                    )
                }
            }
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val eventRepository: EventRepository,
        private val habitRepository: HabitRepository,
        private val timeBlockRepository: TimeBlockRepository,
        private val goalRepository: GoalRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodayViewModel(
                taskRepository,
                eventRepository,
                habitRepository,
                timeBlockRepository,
                goalRepository
            ) as T
        }
    }
}
