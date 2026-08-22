package com.nilian.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Comprehensive evening closeout summary and daily reflection report.
 */
data class EveningCloseoutReport(
    val date: LocalDate,
    val completedTasks: List<Task> = emptyList(),
    val uncompletedTasks: List<Task> = emptyList(),
    val rolledOverTasks: List<Task> = emptyList(),
    val completedTaskMinutes: Int = 0,
    val totalFocusMinutes: Int = 0,
    val deepWorkMinutes: Int = 0,
    val studyMinutes: Int = 0,
    val workoutMinutes: Int = 0,
    val restMinutes: Int = 0,
    val totalHabitsCount: Int = 0,
    val completedHabitsCount: Int = 0,
    val habitCompletionRate: Float = 0f,
    val reflectionSummary: String = "",
    val closeoutScore: Int = 0,
    val completedAt: LocalDateTime = LocalDateTime.now()
)
