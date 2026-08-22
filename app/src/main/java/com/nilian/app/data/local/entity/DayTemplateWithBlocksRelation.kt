package com.nilian.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.nilian.app.domain.model.DayTemplateWithBlocks

/**
 * Relation wrapper for Room to load a DayTemplate together with its associated TemplateBlocks.
 */
data class DayTemplateWithBlocksRelation(
    @Embedded
    val template: DayTemplateEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "template_id"
    )
    val blocks: List<TemplateBlockEntity> = emptyList()
) {
    fun toDomain(): DayTemplateWithBlocks = DayTemplateWithBlocks(
        template = template.toDomain(),
        blocks = blocks.map { it.toDomain() }
    )
}
