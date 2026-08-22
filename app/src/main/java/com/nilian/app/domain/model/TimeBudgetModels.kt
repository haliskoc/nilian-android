package com.nilian.app.domain.model

import java.time.LocalDate

/**
 * Status level for the daily time budget assessment.
 */
enum class TimeBudgetStatus {
    SURPLUS,
    BALANCED,
    MODERATE_LOAD,
    OVERALLOCATED,
    CRITICAL_DEFICIT
}

/**
 * Deterministic balance sheet comparing estimated task workload against available free timeline slots.
 */
data class TimeBudgetAssessment(
    val date: LocalDate,
    val totalTaskMinutes: Int,
    val freeSlotMinutes: Int,
    val committedEventMinutes: Int = 0,
    val committedBlockMinutes: Int = 0,
    val isOverAllocated: Boolean,
    val deficitMinutes: Int,
    val surplusMinutes: Int,
    val budgetRatio: Float,
    val status: TimeBudgetStatus,
    val advice: String
)
