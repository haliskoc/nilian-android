package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.DayTemplate

/**
 * Room entity representing a reusable Day Template (e.g. Exam Day, Weekend Reset).
 */
@Entity(tableName = "day_templates")
data class DayTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "icon_name")
    val iconName: String? = null,

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false
) {
    fun toDomain(): DayTemplate = DayTemplate(
        id = id,
        name = name,
        description = description,
        iconName = iconName,
        isDefault = isDefault
    )

    companion object {
        fun fromDomain(template: DayTemplate): DayTemplateEntity = DayTemplateEntity(
            id = template.id,
            name = template.name,
            description = template.description,
            iconName = template.iconName,
            isDefault = template.isDefault
        )
    }
}
