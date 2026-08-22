package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.TemplateBlock
import java.time.LocalTime

/**
 * Room entity representing a pre-set time block within a Day Template.
 */
@Entity(
    tableName = "template_blocks",
    foreignKeys = [
        ForeignKey(
            entity = DayTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["template_id"])
    ]
)
data class TemplateBlockEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "template_id")
    val templateId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "block_type")
    val blockType: BlockType = BlockType.GENERAL,

    @ColumnInfo(name = "start_time")
    val startTime: LocalTime,

    @ColumnInfo(name = "end_time")
    val endTime: LocalTime
) {
    fun toDomain(): TemplateBlock = TemplateBlock(
        id = id,
        templateId = templateId,
        title = title,
        blockType = blockType,
        startTime = startTime,
        endTime = endTime
    )

    companion object {
        fun fromDomain(block: TemplateBlock): TemplateBlockEntity = TemplateBlockEntity(
            id = block.id,
            templateId = block.templateId,
            title = block.title,
            blockType = block.blockType,
            startTime = block.startTime,
            endTime = block.endTime
        )
    }
}
