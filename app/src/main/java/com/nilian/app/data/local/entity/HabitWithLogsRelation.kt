package com.nilian.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.nilian.app.domain.model.HabitWithLogs

/**
 * Relation wrapper for Room to load a Habit together with its full list of HabitLogs.
 */
data class HabitWithLogsRelation(
    @Embedded
    val habit: HabitEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "habit_id"
    )
    val logs: List<HabitLogEntity> = emptyList()
) {
    fun toDomain(): HabitWithLogs = HabitWithLogs(
        habit = habit.toDomain(),
        logs = logs.map { it.toDomain() }
    )
}
