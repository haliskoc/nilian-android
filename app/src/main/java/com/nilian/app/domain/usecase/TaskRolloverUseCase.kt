package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.Task
import com.nilian.app.domain.repository.TaskRepository
import java.time.LocalDate

/**
 * Deterministic engine for automatically rolling over past unfinished tasks to the current day.
 *
 * Rules:
 * - Task is NOT completed ([Task.isCompleted] == false).
 * - Task has auto-rollover enabled ([Task.autoRollover] == true).
 * - Task has a due date strictly prior to today ([Task.dueDate] < today).
 * - When rolled over, [Task.dueDate] is updated to today, preserving priority and estimates.
 */
class TaskRolloverUseCase(
    private val taskRepository: TaskRepository? = null
) {

    /**
     * Pure function to identify tasks requiring rollover and return their updated versions.
     */
    operator fun invoke(
        tasks: List<Task>,
        today: LocalDate = LocalDate.now()
    ): List<Task> {
        return rolloverTasks(tasks, today)
    }

    /**
     * Filters and finds tasks eligible for rollover.
     */
    fun findTasksToRollover(
        tasks: List<Task>,
        today: LocalDate = LocalDate.now()
    ): List<Task> {
        return tasks.filter { task ->
            !task.isCompleted &&
                task.autoRollover &&
                task.dueDate != null &&
                task.dueDate.isBefore(today)
        }
    }

    /**
     * Returns only the modified rolled-over tasks with their dueDate set to today.
     */
    fun rolloverTasks(
        tasks: List<Task>,
        today: LocalDate = LocalDate.now()
    ): List<Task> {
        return findTasksToRollover(tasks, today).map { task ->
            task.copy(dueDate = today)
        }
    }

    /**
     * Takes an entire list of tasks and returns the whole list with past eligible tasks updated.
     */
    fun applyRolloverToAll(
        tasks: List<Task>,
        today: LocalDate = LocalDate.now()
    ): List<Task> {
        return tasks.map { task ->
            if (!task.isCompleted && task.autoRollover && task.dueDate != null && task.dueDate.isBefore(today)) {
                task.copy(dueDate = today)
            } else {
                task
            }
        }
    }

    /**
     * Executes rollover directly against the [TaskRepository], persisting changes to local storage.
     *
     * @param today The target date (defaults to [LocalDate.now]).
     * @return List of tasks that were updated and persisted.
     */
    suspend fun execute(today: LocalDate = LocalDate.now()): List<Task> {
        val repo = taskRepository ?: return emptyList()
        val overdueTasks = repo.getPendingRolloverTasksList(today)
        val rolledOver = overdueTasks
            .filter { it.autoRollover }
            .map { it.copy(dueDate = today) }

        if (rolledOver.isNotEmpty()) {
            repo.updateTasks(rolledOver)
        }

        return rolledOver
    }
}
