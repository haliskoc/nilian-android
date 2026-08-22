package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.Goal
import java.time.LocalDate

/**
 * Room entity representing a long-term goal or milestone in the local SQLite database.
 */
@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["target_date"]),
        Index(value = ["is_archived"])
    ]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "target_date")
    val targetDate: LocalDate? = null,

    @ColumnInfo(name = "progress_percent")
    val progressPercent: Float = 0f,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false
) {
    fun toDomain(): Goal = Goal(
        id = id,
        title = title,
        description = description,
        targetDate = targetDate,
        progressPercent = progressPercent,
        isArchived = isArchived
    )

    companion object {
        fun fromDomain(goal: Goal): GoalEntity = GoalEntity(
            id = goal.id,
            title = goal.title,
            description = goal.description,
            targetDate = goal.targetDate,
            progressPercent = goal.progressPercent,
            isArchived = goal.isArchived
        )
    }
}
