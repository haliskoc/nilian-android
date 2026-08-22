package com.nilian.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.DayOfWeek
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitItem
import com.nilian.app.domain.model.toItem
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.HabitRepository
import com.nilian.app.domain.usecase.HabitStreakCalculatorUseCase
import com.nilian.app.presentation.habits.HabitsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitsViewModel(
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
    private val habitStreakCalculatorUseCase: HabitStreakCalculatorUseCase = HabitStreakCalculatorUseCase()
) : ViewModel() {

    private val _isAddEditModalVisible = MutableStateFlow(false)
    private val _editingHabit = MutableStateFlow<HabitItem?>(null)

    val uiState: StateFlow<HabitsUiState> = combine(
        habitRepository.getAllHabitsWithLogs(),
        goalRepository.getAllGoals(),
        _isAddEditModalVisible,
        _editingHabit
    ) { params ->
        val habitsWithLogs = params[0] as List<com.nilian.app.domain.model.HabitWithLogs>
        val allGoals = params[1] as List<Goal>
        val isModalVisible = params[2] as Boolean
        val editing = params[3] as HabitItem?

        val goalMap = allGoals.associateBy { it.id }
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val monday = today.minusDays(dayOfWeek.value.toLong() - 1)

        val habitItems = habitsWithLogs.map { hwl ->
            val streakResult = habitStreakCalculatorUseCase(hwl.habit, hwl.logs, today)
            val weekHistory = (0..6).map { i ->
                val targetDay = monday.plusDays(i.toLong())
                hwl.logs.any { it.date == targetDay && it.isCompleted }
            }
            val goalTitle = hwl.habit.goalId?.let { goalMap[it]?.title }

            hwl.habit.toItem(
                goalTitle = goalTitle,
                isCompletedToday = streakResult.isCompletedToday,
                weeklyHistory = weekHistory
            ).copy(
                currentStreak = streakResult.currentStreak,
                bestStreak = streakResult.bestStreak
            )
        }

        val goalItems = allGoals.map { it.toItem() }

        HabitsUiState(
            habits = habitItems,
            availableGoals = goalItems,
            isAddEditModalVisible = isModalVisible,
            editingHabit = editing,
            currentDayOfWeek = today.dayOfWeek
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState()
    )

    fun onToggleHabitToday(habitItem: HabitItem) {
        viewModelScope.launch {
            val today = LocalDate.now()
            habitRepository.toggleHabitLog(habitItem.id, today, !habitItem.isCompletedToday)
        }
    }

    fun onToggleHabitHistoryDay(habitItem: HabitItem, dayIndex: Int) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val monday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            val targetDate = monday.plusDays(dayIndex.toLong())
            val currentStatus = habitItem.weeklyHistory.getOrElse(dayIndex) { false }
            habitRepository.toggleHabitLog(habitItem.id, targetDate, !currentStatus)
        }
    }

    fun onAddHabitClick() {
        _editingHabit.value = null
        _isAddEditModalVisible.value = true
    }

    fun onEditHabitClick(habitItem: HabitItem) {
        _editingHabit.value = habitItem
        _isAddEditModalVisible.value = true
    }

    fun onDeleteHabitClick(habitItem: HabitItem) {
        viewModelScope.launch {
            habitRepository.deleteHabitById(habitItem.id)
        }
    }

    fun onDismissAddEditModal() {
        _isAddEditModalVisible.value = false
        _editingHabit.value = null
    }

    fun onSaveHabit(
        id: Long,
        title: String,
        targetDays: Set<DayOfWeek>,
        goalId: Long?
    ) {
        _isAddEditModalVisible.value = false
        _editingHabit.value = null
        viewModelScope.launch {
            if (id == 0L) {
                habitRepository.insertHabit(
                    Habit(
                        title = title,
                        targetDaysOfWeek = targetDays,
                        goalId = goalId,
                        createdAt = LocalDate.now()
                    )
                )
            } else {
                val existing = habitRepository.getHabitById(id)
                habitRepository.updateHabit(
                    Habit(
                        id = id,
                        title = title,
                        targetDaysOfWeek = targetDays,
                        goalId = goalId,
                        currentStreak = existing?.currentStreak ?: 0,
                        bestStreak = existing?.bestStreak ?: 0,
                        createdAt = existing?.createdAt ?: LocalDate.now()
                    )
                )
            }
        }
    }

    class Factory(
        private val habitRepository: HabitRepository,
        private val goalRepository: GoalRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HabitsViewModel(habitRepository, goalRepository) as T
        }
    }
}
