package com.nilian.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nilian.app.domain.model.InboxNote
import java.time.LocalDateTime

/**
 * Room entity representing a quick brain dump inbox note.
 */
@Entity(
    tableName = "inbox_notes",
    indices = [
        Index(value = ["is_archived"]),
        Index(value = ["created_at"])
    ]
)
data class InboxNoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList()
) {
    fun toDomain(): InboxNote = InboxNote(
        id = id,
        content = content,
        createdAt = createdAt,
        isArchived = isArchived,
        tags = tags
    )

    companion object {
        fun fromDomain(note: InboxNote): InboxNoteEntity = InboxNoteEntity(
            id = note.id,
            content = note.content,
            createdAt = note.createdAt,
            isArchived = note.isArchived,
            tags = note.tags
        )
    }
}
