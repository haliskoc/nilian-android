package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.model.TimeBudgetAssessment
import com.nilian.app.domain.model.TimeBudgetStatus
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Deterministic balance sheet engine calculating workload demand vs timeline capacity.
 *
 * Compares the total estimated task minutes for a given day against the actual
 * available free time slots (calculated via [FreeSlotFinderUseCase]).
 *
 * Provides calm, actionable advice when schedules become over-allocated or under-utilized.
 */
class TimeBudgetCalculatorUseCase(
    private val freeSlotFinderUseCase: FreeSlotFinderUseCase = FreeSlotFinderUseCase()
) {

    /**
     * Evaluates the daily time budget.
     *
     * @param tasks All candidate tasks for the day.
     * @param events Scheduled events touching the target date.
     * @param timeBlocks Time blocks scheduled on the target date.
     * @param targetDate The date being assessed.
     * @param dayStart Start of daytime window (default 06:00).
     * @param dayEnd End of daytime window (default 23:00).
     * @return [TimeBudgetAssessment] summarizing capacity, demand, balance, and mindful advice.
     */
    operator fun invoke(
        tasks: List<Task>,
        events: List<Event>,
        timeBlocks: List<TimeBlock>,
        targetDate: LocalDate = LocalDate.now(),
        dayStart: LocalTime = FreeSlotFinderUseCase.DEFAULT_DAY_START,
        dayEnd: LocalTime = FreeSlotFinderUseCase.DEFAULT_DAY_END
    ): TimeBudgetAssessment {
        // 1. Calculate Task Demand (only uncompleted tasks assigned to or due by targetDate)
        val applicableTasks = tasks.filter { task ->
            !task.isCompleted && (task.dueDate == null || !task.dueDate.isAfter(targetDate))
        }
        val totalTaskMinutes = applicableTasks.sumOf { it.estimatedDurationMinutes }

        // 2. Calculate Committed Event Minutes
        var committedEventMinutes = 0
        for (event in events) {
            val eventStartDate = event.startDateTime.toLocalDate()
            val eventEndDate = event.endDateTime.toLocalDate()
            if (targetDate.isBefore(eventStartDate) || targetDate.isAfter(eventEndDate)) continue

            val startTime = if (targetDate.isEqual(eventStartDate)) event.startDateTime.toLocalTime() else LocalTime.MIN
            val endTime = if (targetDate.isEqual(eventEndDate)) event.endDateTime.toLocalTime() else LocalTime.MAX
            val clampedStart = if (startTime.isBefore(dayStart)) dayStart else startTime
            val clampedEnd = if (endTime.isAfter(dayEnd)) dayEnd else endTime

            if (clampedStart.isBefore(clampedEnd)) {
                committedEventMinutes += Duration.between(clampedStart, clampedEnd).toMinutes().toInt()
            }
        }

        // 3. Calculate Committed TimeBlock Minutes
        var committedBlockMinutes = 0
        for (block in timeBlocks) {
            if (block.date != targetDate) continue
            val blockStart = block.startTime
            val blockEnd = block.endTime

            if (blockEnd.isAfter(blockStart)) {
                val clampedStart = if (blockStart.isBefore(dayStart)) dayStart else blockStart
                val clampedEnd = if (blockEnd.isAfter(dayEnd)) dayEnd else blockEnd
                if (clampedStart.isBefore(clampedEnd)) {
                    committedBlockMinutes += Duration.between(clampedStart, clampedEnd).toMinutes().toInt()
                }
            } else {
                val clampedStart = if (blockStart.isBefore(dayStart)) dayStart else blockStart
                if (clampedStart.isBefore(dayEnd)) {
                    committedBlockMinutes += Duration.between(clampedStart, dayEnd).toMinutes().toInt()
                }
            }
        }

        // 4. Calculate Available Free Slots
        val freeSlots = freeSlotFinderUseCase(
            events = events,
            timeBlocks = timeBlocks,
            date = targetDate,
            dayStart = dayStart,
            dayEnd = dayEnd,
            minDurationMinutes = 15L // Catch even shorter 15m focus gaps
        )
        val freeSlotMinutes = freeSlots.sumOf { it.durationMinutes }.toInt()

        // 5. Compute Deficit / Surplus & Ratio
        val isOverAllocated = totalTaskMinutes > freeSlotMinutes
        val deficitMinutes = maxOf(0, totalTaskMinutes - freeSlotMinutes)
        val surplusMinutes = maxOf(0, freeSlotMinutes - totalTaskMinutes)

        val budgetRatio = if (freeSlotMinutes > 0) {
            totalTaskMinutes.toFloat() / freeSlotMinutes.toFloat()
        } else if (totalTaskMinutes > 0) {
            99.0f
        } else {
            0.0f
        }

        // 6. Determine Budget Status
        val status = when {
            totalTaskMinutes == 0 -> TimeBudgetStatus.SURPLUS
            budgetRatio <= 0.50f -> TimeBudgetStatus.SURPLUS
            budgetRatio <= 0.85f -> TimeBudgetStatus.BALANCED
            budgetRatio <= 1.00f -> TimeBudgetStatus.MODERATE_LOAD
            budgetRatio <= 1.30f -> TimeBudgetStatus.OVERALLOCATED
            else -> TimeBudgetStatus.CRITICAL_DEFICIT
        }

        // 7. Generate Mindful Guidance
        val advice = generateAdvice(status, totalTaskMinutes, freeSlotMinutes, deficitMinutes, surplusMinutes)

        return TimeBudgetAssessment(
            date = targetDate,
            totalTaskMinutes = totalTaskMinutes,
            freeSlotMinutes = freeSlotMinutes,
            committedEventMinutes = committedEventMinutes,
            committedBlockMinutes = committedBlockMinutes,
            isOverAllocated = isOverAllocated,
            deficitMinutes = deficitMinutes,
            surplusMinutes = surplusMinutes,
            budgetRatio = budgetRatio,
            status = status,
            advice = advice
        )
    }

    private fun generateAdvice(
        status: TimeBudgetStatus,
        taskMinutes: Int,
        freeMinutes: Int,
        deficitMinutes: Int,
        surplusMinutes: Int
    ): String {
        return when (status) {
            TimeBudgetStatus.SURPLUS -> {
                "Zaman bütçen son derece ferah ($surplusMinutes dk serbest alan). Görevlerini sakin bir ritimle bitirebilir veya dinlenmeye vakit ayırabilirsin."
            }
            TimeBudgetStatus.BALANCED -> {
                "İdeal zaman dengesi: $freeMinutes dk boşluğa karşılık $taskMinutes dk görev planlandı. Rahat ve odaklı bir gün seni bekliyor."
            }
            TimeBudgetStatus.MODERATE_LOAD -> {
                "Zaman bütçen sınıra yakın ($taskMinutes dk görev / $freeMinutes dk serbest zaman). Beklenmeyen gecikmeler için odak pencerelerini koru."
            }
            TimeBudgetStatus.OVERALLOCATED -> {
                "Günün kapasitesi $deficitMinutes dk aşıldı. 1 veya 2 düşük öncelikli görevi erteleyerek zihnini rahatlatmanı öneririz."
            }
            TimeBudgetStatus.CRITICAL_DEFICIT -> {
                "Ciddi aşırı planlama ($deficitMinutes dk açık). Tüm işleri tek güne sığdırmak yerine sadece en kritik 3 göreve odaklan."
            }
        }
    }
}
