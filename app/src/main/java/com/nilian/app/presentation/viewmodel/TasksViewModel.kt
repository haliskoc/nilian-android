package com.nilian.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TaskItem
import com.nilian.app.domain.model.toItem
import com.nilian.app.domain.model.toDomain
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.presentation.tasks.TaskFilter
import com.nilian.app.presentation.tasks.TasksUiState
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

class TasksViewModel(
    private val taskRepository: TaskRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    private val _selectedGoalFilterId = MutableStateFlow<Long?>(null)
    private val _isAddEditModalVisible = MutableStateFlow(false)
    private val _editingTask = MutableStateFlow<TaskItem?>(null)

    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.getAllTasks(),
        goalRepository.getAllGoals(),
        _selectedFilter,
        _selectedGoalFilterId,
        _isAddEditModalVisible,
        _editingTask
    ) { params ->
        val allTasks = params[0] as List<Task>
        val allGoals = params[1] as List<Goal>
        val filter = params[2] as TaskFilter
        val goalFilterId = params[3] as Long?
        val isModalVisible = params[4] as Boolean
        val editing = params[5] as TaskItem?

        val goalMap = allGoals.associateBy { it.id }

        val taskItems = allTasks.map { task ->
            val goalTitle = task.goalId?.let { goalMap[it]?.title }
            task.toItem(goalTitle = goalTitle)
        }

        val goalItems = allGoals.map { goal ->
            goal.toItem()
        }

        TasksUiState(
            tasks = taskItems,
            availableGoals = goalItems,
            selectedFilter = filter,
            selectedGoalFilterId = goalFilterId,
            isAddEditModalVisible = isModalVisible,
            editingTask = editing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState()
    )

    fun onFilterSelect(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun onGoalFilterSelect(goalId: Long?) {
        _selectedGoalFilterId.value = goalId
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

    fun onAddTaskClick() {
        _editingTask.value = null
        _isAddEditModalVisible.value = true
    }

    fun onEditTaskClick(taskItem: TaskItem) {
        _editingTask.value = taskItem
        _isAddEditModalVisible.value = true
    }

    fun onDeleteTaskClick(taskItem: TaskItem) {
        viewModelScope.launch {
            taskRepository.deleteTaskById(taskItem.id)
        }
    }

    fun onDismissAddEditModal() {
        _isAddEditModalVisible.value = false
        _editingTask.value = null
    }

    fun onSaveTask(
        id: Long,
        title: String,
        description: String?,
        priority: Priority,
        durationMinutes: Int,
        dueDate: LocalDate?,
        goalId: Long?,
        autoRollover: Boolean
    ) {
        _isAddEditModalVisible.value = false
        _editingTask.value = null
        viewModelScope.launch {
            if (id == 0L) {
                taskRepository.insertTask(
                    Task(
                        title = title,
                        description = description,
                        priority = priority,
                        estimatedDurationMinutes = durationMinutes,
                        dueDate = dueDate,
                        goalId = goalId,
                        autoRollover = autoRollover
                    )
                )
            } else {
                val existing = taskRepository.getTaskById(id)
                taskRepository.updateTask(
                    Task(
                        id = id,
                        title = title,
                        description = description,
                        priority = priority,
                        estimatedDurationMinutes = durationMinutes,
                        dueDate = dueDate,
                        isCompleted = existing?.isCompleted ?: false,
                        completedAt = existing?.completedAt,
                        goalId = goalId,
                        autoRollover = autoRollover
                    )
                )
            }
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val goalRepository: GoalRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(taskRepository, goalRepository) as T
        }
    }
}
