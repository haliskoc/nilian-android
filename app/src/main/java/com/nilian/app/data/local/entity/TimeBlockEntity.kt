package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.TimeBlock
import java.time.LocalDate
import java.time.LocalTime

/**
 * Room entity representing a 24-hour daily timeline block in the local SQLite database.
 */
@Entity(
    tableName = "time_blocks",
    indices = [
        Index(value = ["date"]),
        Index(value = ["linked_task_id"]),
        Index(value = ["date", "start_time"])
    ]
)
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "block_type")
    val blockType: BlockType = BlockType.DEEP_WORK,

    @ColumnInfo(name = "start_time")
    val startTime: LocalTime,

    @ColumnInfo(name = "end_time")
    val endTime: LocalTime,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "linked_task_id")
    val linkedTaskId: Long? = null
) {
    fun toDomain(): TimeBlock = TimeBlock(
        id = id,
        title = title,
        blockType = blockType,
        startTime = startTime,
        endTime = endTime,
        date = date,
        linkedTaskId = linkedTaskId
    )

    companion object {
        fun fromDomain(timeBlock: TimeBlock): TimeBlockEntity = TimeBlockEntity(
            id = timeBlock.id,
            title = timeBlock.title,
            blockType = timeBlock.blockType,
            startTime = timeBlock.startTime,
            endTime = timeBlock.endTime,
            date = timeBlock.date,
            linkedTaskId = timeBlock.linkedTaskId
        )
    }
}
