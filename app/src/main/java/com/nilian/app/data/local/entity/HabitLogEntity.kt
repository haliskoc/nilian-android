package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.nilian.app.domain.model.HabitLog
import java.time.LocalDate

/**
 * Room entity representing a daily completion record for a specific habit.
 */
@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habit_id", "date"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habit_id"]),
        Index(value = ["date"]),
        Index(value = ["habit_id", "date"])
    ]
)
data class HabitLogEntity(
    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = true
) {
    fun toDomain(): HabitLog = HabitLog(
        habitId = habitId,
        date = date,
        isCompleted = isCompleted
    )

    companion object {
        fun fromDomain(log: HabitLog): HabitLogEntity = HabitLogEntity(
            habitId = log.habitId,
            date = log.date,
            isCompleted = log.isCompleted
        )
    }
}
