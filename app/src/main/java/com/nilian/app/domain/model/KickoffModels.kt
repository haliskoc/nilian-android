package com.nilian.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Persisted state of a completed or in-progress morning kickoff session.
 */
data class MorningKickoffState(
    val date: LocalDate,
    val selectedFocusTaskIds: List<Long> = emptyList(),
    val intentionNote: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null
)

/**
 * Aggregated summary and decision dashboard for the morning kickoff routine.
 */
data class MorningKickoffSummary(
    val date: LocalDate,
    val overdueRolloverTasks: List<Task> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val todayEvents: List<Event> = emptyList(),
    val todayTimeBlocks: List<TimeBlock> = emptyList(),
    val recommendedFocusTasks: List<Task> = emptyList(),
    val selectedFocusTasks: List<Task> = emptyList(),
    val freeSlots: List<FreeTimeSlot> = emptyList(),
    val totalFreeSlotMinutes: Long = 0L,
    val totalScheduledMinutes: Long = 0L,
    val morningPrompt: String = "",
    val state: MorningKickoffState = MorningKickoffState(date = date)
)
