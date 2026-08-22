package com.nilian.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Suggested target classification for an inbox idea or note.
 */
enum class BrainDumpType {
    TASK,
    EVENT,
    GOAL,
    NOTE
}

/**
 * Metadata extracted deterministically from a raw inbox string.
 */
data class BrainDumpParsedNote(
    val rawText: String,
    val cleanTitle: String,
    val suggestedType: BrainDumpType,
    val extractedDueDate: LocalDate? = null,
    val extractedTime: LocalTime? = null,
    val extractedDurationMinutes: Int? = null,
    val extractedPriority: Priority? = null,
    val tags: List<String> = emptyList()
)

/**
 * Result of converting an [InboxNote] into an actionable domain entity.
 */
data class BrainDumpConversionResult(
    val note: InboxNote,
    val convertedType: BrainDumpType,
    val task: Task? = null,
    val event: Event? = null,
    val goal: Goal? = null
)
