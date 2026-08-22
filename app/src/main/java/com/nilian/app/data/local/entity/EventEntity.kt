package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.EventCategory
import java.time.LocalDateTime

/**
 * Room entity representing a calendar event in the local SQLite database.
 */
@Entity(
    tableName = "events",
    indices = [
        Index(value = ["start_date_time"]),
        Index(value = ["end_date_time"]),
        Index(value = ["category"])
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "location_or_link")
    val locationOrLink: String? = null,

    @ColumnInfo(name = "start_date_time")
    val startDateTime: LocalDateTime,

    @ColumnInfo(name = "end_date_time")
    val endDateTime: LocalDateTime,

    @ColumnInfo(name = "category")
    val category: EventCategory = EventCategory.GENERAL,

    @ColumnInfo(name = "color_hex")
    val colorHex: String? = null
) {
    fun toDomain(): Event = Event(
        id = id,
        title = title,
        locationOrLink = locationOrLink,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        category = category,
        colorHex = colorHex
    )

    companion object {
        fun fromDomain(event: Event): EventEntity = EventEntity(
            id = event.id,
            title = event.title,
            locationOrLink = event.locationOrLink,
            startDateTime = event.startDateTime,
            endDateTime = event.endDateTime,
            category = event.category,
            colorHex = event.colorHex
        )
    }
}
