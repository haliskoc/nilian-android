package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.FreeTimeSlot
import com.nilian.app.domain.model.TimeBlock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Deterministic engine for identifying open gaps in the daily schedule.
 *
 * Scans the active daytime window (default 06:00 to 23:00), overlays all scheduled
 * Events and TimeBlocks, merges busy intervals, and extracts free slots of sufficient
 * duration (e.g. >= 30 or >= 45 minutes) with calm activity recommendations.
 */
class FreeSlotFinderUseCase {

    private data class TimeInterval(
        val start: LocalTime,
        val end: LocalTime
    )

    /**
     * Identifies available free time slots for the given date.
     *
     * @param events List of calendar events.
     * @param timeBlocks List of daily time blocks.
     * @param date The date to analyze.
     * @param dayStart Start of the active daytime window (default 06:00).
     * @param dayEnd End of the active daytime window (default 23:00).
     * @param minDurationMinutes Minimum gap threshold in minutes (default 30 min).
     * @return List of [FreeTimeSlot] items.
     */
    operator fun invoke(
        events: List<Event>,
        timeBlocks: List<TimeBlock>,
        date: LocalDate,
        dayStart: LocalTime = DEFAULT_DAY_START,
        dayEnd: LocalTime = DEFAULT_DAY_END,
        minDurationMinutes: Long = DEFAULT_MIN_GAP_MINUTES
    ): List<FreeTimeSlot> {
        val busyIntervals = mutableListOf<TimeInterval>()

        // 1. Process Events
        for (event in events) {
            val eventStartDate = event.startDateTime.toLocalDate()
            val eventEndDate = event.endDateTime.toLocalDate()

            // Skip events that do not touch this date
            if (date.isBefore(eventStartDate) || date.isAfter(eventEndDate)) {
                continue
            }

            val startTime = if (date.isEqual(eventStartDate)) event.startDateTime.toLocalTime() else LocalTime.MIN
            val endTime = if (date.isEqual(eventEndDate)) event.endDateTime.toLocalTime() else LocalTime.MAX

            val clampedStart = if (startTime.isBefore(dayStart)) dayStart else startTime
            val clampedEnd = if (endTime.isAfter(dayEnd)) dayEnd else endTime

            if (clampedStart.isBefore(clampedEnd)) {
                busyIntervals.add(TimeInterval(clampedStart, clampedEnd))
            }
        }

        // 2. Process TimeBlocks
        for (block in timeBlocks) {
            if (block.date != date) {
                continue
            }

            val blockStart = block.startTime
            val blockEnd = block.endTime

            if (blockEnd.isAfter(blockStart)) {
                val clampedStart = if (blockStart.isBefore(dayStart)) dayStart else blockStart
                val clampedEnd = if (blockEnd.isAfter(dayEnd)) dayEnd else blockEnd

                if (clampedStart.isBefore(clampedEnd)) {
                    busyIntervals.add(TimeInterval(clampedStart, clampedEnd))
                }
            } else {
                // Overnight block: spans until end of day
                val clampedStart = if (blockStart.isBefore(dayStart)) dayStart else blockStart
                if (clampedStart.isBefore(dayEnd)) {
                    busyIntervals.add(TimeInterval(clampedStart, dayEnd))
                }
            }
        }

        // 3. Merge overlapping / adjacent busy intervals
        val mergedBusy = mergeIntervals(busyIntervals)

        // 4. Invert busy intervals against [dayStart, dayEnd]
        val freeSlots = mutableListOf<FreeTimeSlot>()
        var cursor = dayStart

        for (interval in mergedBusy) {
            if (interval.start.isAfter(cursor)) {
                val durationMinutes = Duration.between(cursor, interval.start).toMinutes()
                if (durationMinutes >= minDurationMinutes) {
                    freeSlots.add(
                        FreeTimeSlot(
                            startTime = cursor,
                            endTime = interval.start,
                            durationMinutes = durationMinutes,
                            date = date,
                            suggestedActivity = suggestActivity(durationMinutes)
                        )
                    )
                }
            }
            if (interval.end.isAfter(cursor)) {
                cursor = interval.end
            }
        }

        // Check remaining time after the last busy block
        if (cursor.isBefore(dayEnd)) {
            val remainingMinutes = Duration.between(cursor, dayEnd).toMinutes()
            if (remainingMinutes >= minDurationMinutes) {
                freeSlots.add(
                    FreeTimeSlot(
                        startTime = cursor,
                        endTime = dayEnd,
                        durationMinutes = remainingMinutes,
                        date = date,
                        suggestedActivity = suggestActivity(remainingMinutes)
                    )
                )
            }
        }

        return freeSlots
    }

    private fun mergeIntervals(intervals: List<TimeInterval>): List<TimeInterval> {
        if (intervals.isEmpty()) return emptyList()

        val sorted = intervals.sortedBy { it.start }
        val merged = mutableListOf<TimeInterval>()
        var current = sorted[0]

        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (!next.start.isAfter(current.end)) {
                // Overlapping or adjacent
                val newEnd = if (next.end.isAfter(current.end)) next.end else current.end
                current = TimeInterval(current.start, newEnd)
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }

    /**
     * Deterministic heuristic suggesting suitable calm activities based on window duration.
     */
    private fun suggestActivity(durationMinutes: Long): String {
        return when {
            durationMinutes < 45 -> "Hızlı Odak & Kahve Molası"
            durationMinutes < 90 -> "Derin Odaklanma Seansı"
            durationMinutes < 180 -> "Stratejik Görev & Ders Bloğu"
            else -> "Geniş Serbest Zaman & Dinlenme"
        }
    }

    companion object {
        val DEFAULT_DAY_START: LocalTime = LocalTime.of(6, 0)
        val DEFAULT_DAY_END: LocalTime = LocalTime.of(23, 0)
        const val DEFAULT_MIN_GAP_MINUTES = 30L
    }
}
