package com.nilian.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.GoalItem
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitItem
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TaskItem
import com.nilian.app.domain.model.toItem
import com.nilian.app.domain.model.toDomain
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.HabitRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.presentation.goals.GoalWithDetails
import com.nilian.app.presentation.goals.GoalsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _showArchived = MutableStateFlow(false)
    private val _isAddEditModalVisible = MutableStateFlow(false)
    private val _editingGoal = MutableStateFlow<GoalItem?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.getAllGoals(),
        taskRepository.getAllTasks(),
        habitRepository.getAllHabits(),
        _showArchived,
        _isAddEditModalVisible,
        _editingGoal
    ) { params ->
        val allGoals = params[0] as List<Goal>
        val allTasks = params[1] as List<Task>
        val allHabits = params[2] as List<Habit>
        val showArchived = params[3] as Boolean
        val isModalVisible = params[4] as Boolean
        val editing = params[5] as GoalItem?

        val activeList = mutableListOf<GoalWithDetails>()
        val archivedList = mutableListOf<GoalWithDetails>()

        for (goal in allGoals) {
            val linkedTasks = allTasks.filter { it.goalId == goal.id }.map { it.toItem() }
            val linkedHabits = allHabits.filter { it.goalId == goal.id }.map { it.toItem() }

            val totalTasks = linkedTasks.size
            val completedTasks = linkedTasks.count { it.isCompleted }
            val computedProgress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else goal.progressPercent

            val goalItem = goal.toItem(
                linkedTaskCount = linkedTasks.size,
                linkedHabitCount = linkedHabits.size
            ).copy(progressPercent = computedProgress)

            val details = GoalWithDetails(
                goal = goalItem,
                linkedTasks = linkedTasks,
                linkedHabits = linkedHabits
            )

            if (goal.isArchived) {
                archivedList.add(details)
            } else {
                activeList.add(details)
            }
        }

        GoalsUiState(
            activeGoals = activeList,
            archivedGoals = archivedList,
            showArchived = showArchived,
            isAddEditModalVisible = isModalVisible,
            editingGoal = editing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState()
    )

    fun onToggleShowArchived() {
        _showArchived.update { !it }
    }

    fun onAddGoalClick() {
        _editingGoal.value = null
        _isAddEditModalVisible.value = true
    }

    fun onEditGoalClick(goalItem: GoalItem) {
        _editingGoal.value = goalItem
        _isAddEditModalVisible.value = true
    }

    fun onArchiveGoalClick(goalItem: GoalItem) {
        viewModelScope.launch {
            val existing = goalRepository.getGoalById(goalItem.id) ?: return@launch
            goalRepository.updateGoal(existing.copy(isArchived = !existing.isArchived))
        }
    }

    fun onDeleteGoalClick(goalItem: GoalItem) {
        viewModelScope.launch {
            goalRepository.deleteGoalById(goalItem.id)
        }
    }

    fun onDismissAddEditModal() {
        _isAddEditModalVisible.value = false
        _editingGoal.value = null
    }

    fun onSaveGoal(
        id: Long,
        title: String,
        description: String?,
        targetDate: LocalDate?
    ) {
        _isAddEditModalVisible.value = false
        _editingGoal.value = null
        viewModelScope.launch {
            if (id == 0L) {
                goalRepository.insertGoal(
                    Goal(
                        title = title,
                        description = description,
                        targetDate = targetDate
                    )
                )
            } else {
                val existing = goalRepository.getGoalById(id)
                goalRepository.updateGoal(
                    Goal(
                        id = id,
                        title = title,
                        description = description,
                        targetDate = targetDate,
                        progressPercent = existing?.progressPercent ?: 0f,
                        isArchived = existing?.isArchived ?: false
                    )
                )
            }
        }
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
            val today = LocalDate.now()
            habitRepository.toggleHabitLog(habitItem.id, today, !habitItem.isCompletedToday)
        }
    }

    class Factory(
        private val goalRepository: GoalRepository,
        private val taskRepository: TaskRepository,
        private val habitRepository: HabitRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoalsViewModel(goalRepository, taskRepository, habitRepository) as T
        }
    }
}
