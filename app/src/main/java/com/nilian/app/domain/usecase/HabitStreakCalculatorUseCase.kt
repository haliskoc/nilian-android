package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.StreakResult
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.max

/**
 * Deterministic engine for computing active consecutive streaks and all-time best streaks for habits.
 *
 * Calm Tech & Offline Philosophy:
 * - Respects custom target active days (e.g. Mon-Fri or Weekends).
 * - If today is an active target day but not yet logged as completed, the streak from previous
 *   target days is preserved rather than harshly reset to 0 before the day ends.
 * - Non-target days (rest days) are skipped without penalizing the ongoing streak.
 */
class HabitStreakCalculatorUseCase {

    /**
     * Calculates the current and best streak for the specified habit against its past logs.
     *
     * @param habit The target habit with its active schedule.
     * @param logs Historical completion logs for this habit.
     * @param referenceDate The reference point in time (defaults to [LocalDate.now]).
     * @return [StreakResult] containing currentStreak, bestStreak, and whether today is completed.
     */
    operator fun invoke(
        habit: Habit,
        logs: List<HabitLog>,
        referenceDate: LocalDate = LocalDate.now()
    ): StreakResult {
        return calculate(habit, logs, referenceDate)
    }

    /**
     * Calculates the streak result for a habit.
     */
    fun calculate(
        habit: Habit,
        logs: List<HabitLog>,
        referenceDate: LocalDate = LocalDate.now()
    ): StreakResult {
        val targetDays = if (habit.targetDaysOfWeek.isEmpty()) {
            DayOfWeek.values().toSet()
        } else {
            habit.targetDaysOfWeek
        }

        val completedDates: Set<LocalDate> = logs
            .filter { it.habitId == habit.id && it.isCompleted }
            .map { it.date }
            .toSet()

        val isCompletedToday = completedDates.contains(referenceDate)

        // 1. Compute Current Streak
        var currentStreak = 0
        val isTodayTargetDay = targetDays.contains(referenceDate.dayOfWeek)

        if (isTodayTargetDay && isCompletedToday) {
            currentStreak = 1
        }

        // Start scanning backward from yesterday
        var cursor = referenceDate.minusDays(1)
        val searchLimit = habit.createdAt.minusDays(365) // Reasonable lower boundary

        while (!cursor.isBefore(searchLimit)) {
            val isTargetDay = targetDays.contains(cursor.dayOfWeek)
            if (isTargetDay) {
                if (completedDates.contains(cursor)) {
                    currentStreak++
                } else {
                    // Missed an active target day, streak ends
                    break
                }
            }
            cursor = cursor.minusDays(1)
        }

        // 2. Compute All-Time Best Streak
        val earliestLogDate = completedDates.minOrNull()
        val startDate = when {
            earliestLogDate != null && earliestLogDate.isBefore(habit.createdAt) -> earliestLogDate
            else -> habit.createdAt
        }

        var runningStreak = 0
        var maxStreak = 0

        var dateIterator = startDate
        while (!dateIterator.isAfter(referenceDate)) {
            val isTargetDay = targetDays.contains(dateIterator.dayOfWeek)
            if (isTargetDay) {
                if (completedDates.contains(dateIterator)) {
                    runningStreak++
                    maxStreak = max(maxStreak, runningStreak)
                } else if (dateIterator.isBefore(referenceDate)) {
                    // Past target day was missed
                    runningStreak = 0
                }
            }
            dateIterator = dateIterator.plusDays(1)
        }

        val bestStreak = maxOf(maxStreak, currentStreak, habit.bestStreak)

        return StreakResult(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            isCompletedToday = isCompletedToday
        )
    }

    /**
     * Returns an updated copy of the [Habit] domain model with recalculated streaks.
     */
    fun updateHabit(
        habit: Habit,
        logs: List<HabitLog>,
        referenceDate: LocalDate = LocalDate.now()
    ): Habit {
        val result = calculate(habit, logs, referenceDate)
        return habit.copy(
            currentStreak = result.currentStreak,
            bestStreak = result.bestStreak
        )
    }
}
