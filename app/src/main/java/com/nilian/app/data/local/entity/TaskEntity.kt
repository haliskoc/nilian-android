package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Room entity representing an actionable task in the local SQLite database.
 */
@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["due_date"]),
        Index(value = ["goal_id"]),
        Index(value = ["is_completed"]),
        Index(value = ["due_date", "is_completed"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "priority")
    val priority: Priority = Priority.MEDIUM,

    @ColumnInfo(name = "estimated_duration_minutes")
    val estimatedDurationMinutes: Int = 30,

    @ColumnInfo(name = "due_date")
    val dueDate: LocalDate? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_at")
    val completedAt: LocalDateTime? = null,

    @ColumnInfo(name = "goal_id")
    val goalId: Long? = null,

    @ColumnInfo(name = "auto_rollover")
    val autoRollover: Boolean = true
) {
    fun toDomain(): Task = Task(
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

    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            priority = task.priority,
            estimatedDurationMinutes = task.estimatedDurationMinutes,
            dueDate = task.dueDate,
            isCompleted = task.isCompleted,
            completedAt = task.completedAt,
            goalId = task.goalId,
            autoRollover = task.autoRollover
        )
    }
}
