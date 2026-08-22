package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Room entity representing a recurring habit in the local SQLite database.
 */
@Entity(
    tableName = "habits",
    indices = [
        Index(value = ["goal_id"])
    ]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "goal_id")
    val goalId: Long? = null,

    @ColumnInfo(name = "target_days_of_week")
    val targetDaysOfWeek: Set<DayOfWeek> = emptySet(),

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,

    @ColumnInfo(name = "best_streak")
    val bestStreak: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDate = LocalDate.now()
) {
    fun toDomain(): Habit = Habit(
        id = id,
        title = title,
        goalId = goalId,
        targetDaysOfWeek = targetDaysOfWeek,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(habit: Habit): HabitEntity = HabitEntity(
            id = habit.id,
            title = habit.title,
            goalId = habit.goalId,
            targetDaysOfWeek = habit.targetDaysOfWeek,
            currentStreak = habit.currentStreak,
            bestStreak = habit.bestStreak,
            createdAt = habit.createdAt
        )
    }
}
