package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import java.time.Duration
import java.time.LocalDate

data class TimeCategoryBudget(
    val categoryName: String,
    val totalMinutes: Int,
    val percentageOfWakingTime: Float,
    val colorHex: String
)

data class DailyTimeBudgetResult(
    val date: LocalDate,
    val totalWakingMinutesBudget: Int = 16 * 60, // 16 hours waking budget
    val deepWorkMinutes: Int,
    val studyMinutes: Int,
    val healthWorkoutMinutes: Int,
    val restReflectionMinutes: Int,
    val bufferTransitionMinutes: Int,
    val otherCommittedMinutes: Int,
    val totalCommittedMinutes: Int,
    val remainingFreeMinutes: Int,
    val isOverBudget: Boolean,
    val categories: List<TimeCategoryBudget>,
    val calmGuidance: String
)

class TimeBudgetAllocatorUseCase {

    /**
     * Calculates time budget distribution across life domains for a given day.
     */
    fun calculateBudget(
        tasks: List<Task>,
        events: List<Event>,
        timeBlocks: List<TimeBlock>,
        targetDate: LocalDate = LocalDate.now(),
        wakingHoursBudget: Int = 16
    ): DailyTimeBudgetResult {
        val totalWakingMinutes = wakingHoursBudget * 60

        // 1. Calculate time from TimeBlocks
        val dateBlocks = timeBlocks.filter { it.date == targetDate }
        var deepWorkMin = 0
        var studyMin = 0
        var healthMin = 0
        var restMin = 0
        var bufferMin = 0
        var otherMin = 0

        for (b in dateBlocks) {
            val dur = Duration.between(b.startTime, b.endTime).toMinutes().toInt().coerceAtLeast(0)
            when (b.blockType) {
                BlockType.DEEP_WORK -> deepWorkMin += dur
                BlockType.STUDY -> studyMin += dur
                BlockType.WORKOUT -> healthMin += dur
                BlockType.REST -> restMin += dur
                BlockType.BUFFER -> bufferMin += dur
                BlockType.SLEEP -> { /* Sleep is outside waking hours */ }
                BlockType.GENERAL, BlockType.OTHER -> otherMin += dur
            }
        }

        // 2. Add Event durations
        val dateEvents = events.filter {
            !targetDate.isBefore(it.startDateTime.toLocalDate()) && !targetDate.isAfter(it.endDateTime.toLocalDate())
        }
        for (e in dateEvents) {
            val start = if (e.startDateTime.toLocalDate() == targetDate) e.startDateTime.toLocalTime() else java.time.LocalTime.MIN
            val end = if (e.endDateTime.toLocalDate() == targetDate) e.endDateTime.toLocalTime() else java.time.LocalTime.MAX
            val dur = Duration.between(start, end).toMinutes().toInt().coerceAtLeast(0)
            when (e.category) {
                EventCategory.LECTURE, EventCategory.STUDY -> studyMin += dur
                EventCategory.WORK, EventCategory.MEETING -> deepWorkMin += dur
                EventCategory.PERSONAL -> restMin += dur
                EventCategory.GENERAL, EventCategory.OTHER -> otherMin += dur
            }
        }

        // 3. Add standalone unallocated tasks due today
        val standaloneTasks = tasks.filter { it.dueDate == targetDate }
        val taskMin = standaloneTasks.sumOf { it.estimatedDurationMinutes }
        deepWorkMin += taskMin

        val totalCommitted = deepWorkMin + studyMin + healthMin + restMin + bufferMin + otherMin
        val remainingFree = (totalWakingMinutes - totalCommitted).coerceAtLeast(0)
        val isOverBudget = totalCommitted > totalWakingMinutes

        val categories = listOf(
            TimeCategoryBudget("Derin Odak & İş", deepWorkMin, if (totalWakingMinutes > 0) deepWorkMin.toFloat() / totalWakingMinutes else 0f, "#4E876A"),
            TimeCategoryBudget("Ders & Eğitim", studyMin, if (totalWakingMinutes > 0) studyMin.toFloat() / totalWakingMinutes else 0f, "#6A7B8C"),
            TimeCategoryBudget("Spor & Sağlık", healthMin, if (totalWakingMinutes > 0) healthMin.toFloat() / totalWakingMinutes else 0f, "#5B9279"),
            TimeCategoryBudget("Dinlenme & Yansıma", restMin, if (totalWakingMinutes > 0) restMin.toFloat() / totalWakingMinutes else 0f, "#D99B43"),
            TimeCategoryBudget("Geçiş & Mola", bufferMin, if (totalWakingMinutes > 0) bufferMin.toFloat() / totalWakingMinutes else 0f, "#7A8288")
        )

        val guidance = when {
            isOverBudget -> "Gününüz 16 saatlik uyanık kapasiteyi aşıyor. Birkaç görevi erteleyerek kendinize dinlenme alanı açın 🌿"
            totalCommitted > (12 * 60) -> "Bugün yoğun bir odak programınız var. Bloklar arasında en az 15 dakikalık nefes aralıkları bırakın ✨"
            else -> "Zaman bütçeniz son derece dengeli ve sakin. Serbest zamanınızı keyifle değerlendirin 🕊️"
        }

        return DailyTimeBudgetResult(
            date = targetDate,
            totalWakingMinutesBudget = totalWakingMinutes,
            deepWorkMinutes = deepWorkMin,
            studyMinutes = studyMin,
            healthWorkoutMinutes = healthMin,
            restReflectionMinutes = restMin,
            bufferTransitionMinutes = bufferMin,
            otherCommittedMinutes = otherMin,
            totalCommittedMinutes = totalCommitted,
            remainingFreeMinutes = remainingFree,
            isOverBudget = isOverBudget,
            categories = categories,
            calmGuidance = guidance
        )
    }
}
