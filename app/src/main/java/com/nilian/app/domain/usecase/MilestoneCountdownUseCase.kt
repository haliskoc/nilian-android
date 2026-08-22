package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Task
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class MilestonePaceStatus {
    COMPLETED,
    AHEAD,
    ON_TRACK,
    AT_RISK,
    OVERDUE,
    NO_DEADLINE
}

data class MilestoneCountdownResult(
    val goalId: Long,
    val title: String,
    val targetDate: LocalDate?,
    val daysRemaining: Long,
    val totalLinkedTasks: Int,
    val completedLinkedTasks: Int,
    val progressPercent: Float,
    val status: MilestonePaceStatus,
    val requiredTasksPerWeek: Float,
    val countdownFormatted: String
)

class MilestoneCountdownUseCase {

    /**
     * Calculates milestone countdowns and pace trajectory for goals.
     */
    fun calculateMilestone(
        goal: Goal,
        linkedTasks: List<Task> = emptyList(),
        referenceDate: LocalDate = LocalDate.now()
    ): MilestoneCountdownResult {
        val totalTasks = linkedTasks.size
        val completedTasks = linkedTasks.count { it.isCompleted }
        val taskProgress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else goal.progressPercent

        if (goal.targetDate == null) {
            val status = if (taskProgress >= 1.0f) MilestonePaceStatus.COMPLETED else MilestonePaceStatus.NO_DEADLINE
            return MilestoneCountdownResult(
                goalId = goal.id,
                title = goal.title,
                targetDate = null,
                daysRemaining = 0L,
                totalLinkedTasks = totalTasks,
                completedLinkedTasks = completedTasks,
                progressPercent = taskProgress,
                status = status,
                requiredTasksPerWeek = 0f,
                countdownFormatted = "Süresiz Vizyon"
            )
        }

        val daysRemaining = ChronoUnit.DAYS.between(referenceDate, goal.targetDate)
        val remainingTasks = (totalTasks - completedTasks).coerceAtLeast(0)

        val status: MilestonePaceStatus
        val countdownText: String

        if (taskProgress >= 1.0f) {
            status = MilestonePaceStatus.COMPLETED
            countdownText = "🎉 Tamamlandı"
        } else if (daysRemaining < 0) {
            status = MilestonePaceStatus.OVERDUE
            countdownText = "${-daysRemaining} gün gecikti"
        } else if (daysRemaining == 0L) {
            status = MilestonePaceStatus.AT_RISK
            countdownText = "Bugün son gün!"
        } else {
            val weeksRemaining = (daysRemaining / 7.0f).coerceAtLeast(0.1f)
            val expectedProgress = (1.0f - (daysRemaining.toFloat() / 90f)).coerceIn(0f, 1f)

            status = when {
                taskProgress >= expectedProgress -> MilestonePaceStatus.AHEAD
                taskProgress >= (expectedProgress - 0.2f) -> MilestonePaceStatus.ON_TRACK
                else -> MilestonePaceStatus.AT_RISK
            }

            countdownText = "$daysRemaining gün kaldı"
        }

        val weeksRemaining = (daysRemaining / 7.0f).coerceAtLeast(0.1f)
        val requiredTasksPerWeek = if (daysRemaining > 0 && remainingTasks > 0) {
            remainingTasks / weeksRemaining
        } else {
            0f
        }

        return MilestoneCountdownResult(
            goalId = goal.id,
            title = goal.title,
            targetDate = goal.targetDate,
            daysRemaining = daysRemaining,
            totalLinkedTasks = totalTasks,
            completedLinkedTasks = completedTasks,
            progressPercent = taskProgress,
            status = status,
            requiredTasksPerWeek = Math.round(requiredTasksPerWeek * 10) / 10.0f,
            countdownFormatted = countdownText
        )
    }
}
