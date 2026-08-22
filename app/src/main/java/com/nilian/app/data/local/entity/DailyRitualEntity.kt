package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.DailyRitual
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Room entity representing morning & evening daily ritual reviews.
 */
@Entity(
    tableName = "daily_rituals",
    indices = [
        Index(value = ["date"], unique = true)
    ]
)
data class DailyRitualEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "date")
    val date: LocalDate = LocalDate.now(),

    @ColumnInfo(name = "top_3_task_ids")
    val top3TaskIds: List<Long> = emptyList(),

    @ColumnInfo(name = "focus_minutes_total")
    val focusMinutesTotal: Int = 0,

    @ColumnInfo(name = "evening_reflection")
    val eveningReflection: String? = null,

    @ColumnInfo(name = "completed_at")
    val completedAt: LocalDateTime? = null
) {
    fun toDomain(): DailyRitual = DailyRitual(
        id = id,
        date = date,
        top3TaskIds = top3TaskIds,
        focusMinutesTotal = focusMinutesTotal,
        eveningReflection = eveningReflection,
        completedAt = completedAt
    )

    companion object {
        fun fromDomain(ritual: DailyRitual): DailyRitualEntity = DailyRitualEntity(
            id = ritual.id,
            date = ritual.date,
            top3TaskIds = ritual.top3TaskIds,
            focusMinutesTotal = ritual.focusMinutesTotal,
            eveningReflection = ritual.eveningReflection,
            completedAt = ritual.completedAt
        )
    }
}
