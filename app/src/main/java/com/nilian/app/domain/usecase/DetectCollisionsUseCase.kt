package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.ConflictSourceItem
import com.nilian.app.domain.model.ConflictSourceType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.ScheduleConflict
import com.nilian.app.domain.model.TimeBlock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Deterministic engine for detecting schedule collisions between calendar events and time blocks.
 *
 * Offline, rule-based algorithm:
 * 1. Normalizes Events and TimeBlocks into uniform interval items ([ConflictSourceItem]).
 * 2. Compares all non-identical pairs (O(N^2) over the daily items, where N is typically < 50).
 * 3. Identifies overlapping sub-intervals [max(startA, startB), min(endA, endB)].
 * 4. Yields structured [ScheduleConflict] objects with exact start, end, and duration.
 */
class DetectCollisionsUseCase {

    /**
     * Finds all collisions among the provided events and time blocks.
     *
     * @param events List of calendar events.
     * @param timeBlocks List of daily time blocks.
     * @param targetDate Optional date to restrict collision detection to. If null, all items are checked.
     * @return Sorted list of [ScheduleConflict] items ordered chronologically.
     */
    operator fun invoke(
        events: List<Event>,
        timeBlocks: List<TimeBlock>,
        targetDate: LocalDate? = null
    ): List<ScheduleConflict> {
        val items = mutableListOf<ConflictSourceItem>()

        // 1. Convert Events
        for (event in events) {
            val start = event.startDateTime
            val end = if (event.endDateTime.isAfter(start)) event.endDateTime else start.plusMinutes(15)

            if (targetDate != null) {
                val dayStart = targetDate.atStartOfDay()
                val dayEnd = targetDate.plusDays(1).atStartOfDay()
                if (end.isBefore(dayStart) || start.isAfter(dayEnd) || start == dayEnd) {
                    continue
                }
            }

            items.add(
                ConflictSourceItem(
                    id = event.id,
                    title = event.title,
                    type = ConflictSourceType.EVENT,
                    startDateTime = start,
                    endDateTime = end
                )
            )
        }

        // 2. Convert TimeBlocks
        for (block in timeBlocks) {
            if (targetDate != null && block.date != targetDate) {
                continue
            }

            val start = LocalDateTime.of(block.date, block.startTime)
            val end = if (block.endTime.isAfter(block.startTime)) {
                LocalDateTime.of(block.date, block.endTime)
            } else {
                // Block spans across midnight
                LocalDateTime.of(block.date.plusDays(1), block.endTime)
            }

            items.add(
                ConflictSourceItem(
                    id = block.id,
                    title = block.title,
                    type = ConflictSourceType.TIME_BLOCK,
                    startDateTime = start,
                    endDateTime = end
                )
            )
        }

        val conflicts = mutableListOf<ScheduleConflict>()

        // 3. Compare all unique pairs
        for (i in 0 until items.size) {
            for (j in i + 1 until items.size) {
                val a = items[i]
                val b = items[j]

                val overlapStart = if (a.startDateTime.isAfter(b.startDateTime)) a.startDateTime else b.startDateTime
                val overlapEnd = if (a.endDateTime.isBefore(b.endDateTime)) a.endDateTime else b.endDateTime

                if (overlapStart.isBefore(overlapEnd)) {
                    val overlapMinutes = Duration.between(overlapStart, overlapEnd).toMinutes()
                    if (overlapMinutes > 0) {
                        conflicts.add(
                            ScheduleConflict(
                                itemA = a,
                                itemB = b,
                                overlapStart = overlapStart,
                                overlapEnd = overlapEnd,
                                overlapDurationMinutes = overlapMinutes
                            )
                        )
                    }
                }
            }
        }

        return conflicts.sortedWith(
            compareBy<ScheduleConflict> { it.overlapStart }
                .thenByDescending { it.overlapDurationMinutes }
        )
    }
}
