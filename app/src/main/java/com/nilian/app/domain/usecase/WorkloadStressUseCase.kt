package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.model.WorkloadAssessment
import com.nilian.app.domain.model.WorkloadLevel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Deterministic engine for evaluating daily workload stress score.
 *
 * Rules:
 * - Aggregates estimated task minutes, scheduled event durations, and focused time blocks.
 * - Categorizes the day into LIGHT (< 4h), BALANCED (4-8h), MODERATE (8-10h), or HEAVY (> 10h).
 * - Flags heavy load (> 600 minutes / 10 hours) and provides calm, mindful guidance.
 */
class WorkloadStressUseCase {

    /**
     * Calculates the daily workload stress assessment for a given date.
     *
     * @param tasks List of tasks scheduled for or due on the date.
     * @param events List of calendar events touching the date.
     * @param timeBlocks List of time blocks on the date.
     * @param date The date to analyze.
     * @return [WorkloadAssessment] with duration breakdown, stress level, and gentle suggestion.
     */
    operator fun invoke(
        tasks: List<Task>,
        events: List<Event>,
        timeBlocks: List<TimeBlock>,
        date: LocalDate
    ): WorkloadAssessment {
        // 1. Task minutes for the date
        val relevantTasks = tasks.filter { it.dueDate == null || it.dueDate == date }
        val taskMinutes = relevantTasks.sumOf { it.estimatedDurationMinutes }

        // 2. Event minutes touching the date
        val eventMinutes = events.filter { event ->
            val eventStartDate = event.startDateTime.toLocalDate()
            val eventEndDate = event.endDateTime.toLocalDate()
            !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)
        }.sumOf { event ->
            val eventStartDate = event.startDateTime.toLocalDate()
            val eventEndDate = event.endDateTime.toLocalDate()

            val start = if (date.isEqual(eventStartDate)) event.startDateTime.toLocalTime() else LocalTime.MIN
            val end = if (date.isEqual(eventEndDate)) event.endDateTime.toLocalTime() else LocalTime.MAX

            if (end.isAfter(start)) {
                Duration.between(start, end).toMinutes().toInt()
            } else {
                0
            }
        }

        // 3. Focused TimeBlock minutes (exclude SLEEP and pure REST to focus on active commitments)
        val activeBlocks = timeBlocks.filter { block ->
            block.date == date &&
                block.blockType != BlockType.SLEEP &&
                block.blockType != BlockType.REST
        }
        val blockMinutes = activeBlocks.sumOf { block ->
            if (block.endTime.isAfter(block.startTime)) {
                Duration.between(block.startTime, block.endTime).toMinutes().toInt()
            } else {
                Duration.between(block.startTime, LocalTime.MAX).toMinutes().toInt()
            }
        }

        // 4. Total committed minutes
        val totalCommittedMinutes = taskMinutes + eventMinutes + blockMinutes

        // 5. Workload level categorization
        val workloadLevel = when {
            totalCommittedMinutes <= LIGHT_THRESHOLD_MINUTES -> WorkloadLevel.LIGHT
            totalCommittedMinutes <= BALANCED_THRESHOLD_MINUTES -> WorkloadLevel.BALANCED
            totalCommittedMinutes <= HEAVY_THRESHOLD_MINUTES -> WorkloadLevel.MODERATE
            else -> WorkloadLevel.HEAVY
        }

        val isHeavyLoad = totalCommittedMinutes > HEAVY_THRESHOLD_MINUTES

        // 6. Generate Calm Tech suggestion
        val hours = totalCommittedMinutes / 60
        val mins = totalCommittedMinutes % 60
        val durationFormatted = if (hours > 0) "${hours}s ${mins}dk" else "${mins}dk"

        val suggestion = when (workloadLevel) {
            WorkloadLevel.HEAVY ->
                "Bugün planlanan toplam yük 10 saatin üzerinde ($durationFormatted). Zihnini dinlendirmek için öncelikli olmayan 1-2 görevi yarına devretmeyi düşünebilirsin."
            WorkloadLevel.MODERATE ->
                "Bugün yoğun ve odaklı bir programın var ($durationFormatted). Bloklar arasına kısa nefes ve mola aralıkları eklemeyi unutma."
            WorkloadLevel.BALANCED ->
                "Dengeli ve sürdürülebilir bir gün ($durationFormatted). Odaklanma ve dinlenme dengen ideal görünüyor."
            WorkloadLevel.LIGHT ->
                "Hafif ve sakin bir gün ($durationFormatted). Yaratıcı fikirlere, dinlenmeye veya spontane aktivitelere vakit ayırabilirsin."
        }

        return WorkloadAssessment(
            date = date,
            taskMinutes = taskMinutes,
            eventMinutes = eventMinutes,
            blockMinutes = blockMinutes,
            totalCommittedMinutes = totalCommittedMinutes,
            workloadLevel = workloadLevel,
            isHeavyLoad = isHeavyLoad,
            suggestion = suggestion
        )
    }

    companion object {
        const val LIGHT_THRESHOLD_MINUTES = 240 // 4 hours
        const val BALANCED_THRESHOLD_MINUTES = 480 // 8 hours
        const val HEAVY_THRESHOLD_MINUTES = 600 // 10 hours
    }
}
