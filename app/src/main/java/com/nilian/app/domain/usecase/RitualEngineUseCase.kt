package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.DailyRitual
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import java.time.LocalDate
import java.time.LocalDateTime

data class MorningRitualResult(
    val date: LocalDate,
    val top3Tasks: List<Task>,
    val rolloverCount: Int,
    val readinessScore: Int,
    val focusMantra: String
)

data class EveningShutdownResult(
    val date: LocalDate,
    val completedTasksCount: Int,
    val pendingTasksCount: Int,
    val rolloverCandidates: List<Task>,
    val habitsCompletedCount: Int,
    val totalHabitsCount: Int,
    val focusMinutesLogged: Int,
    val accomplishmentScorePercent: Int,
    val reflectionPrompt: String
)

class RitualEngineUseCase {

    /**
     * Executes the Morning Ritual Kickoff.
     * Evaluates daily focus priorities, rollover items, and sets a calm intention.
     */
    fun executeMorningKickoff(
        tasks: List<Task>,
        habits: List<Habit>,
        targetDate: LocalDate = LocalDate.now()
    ): MorningRitualResult {
        // 1. Identify tasks due today or overdue
        val activeTasks = tasks.filter { task ->
            task.dueDate == null || task.dueDate == targetDate || (!task.isCompleted && task.dueDate.isBefore(targetDate))
        }

        // 2. Count rollovers from past uncompleted tasks
        val rolloverCount = activeTasks.count { !it.isCompleted && it.dueDate != null && it.dueDate.isBefore(targetDate) }

        // 3. Select Top 3 focus tasks (sorted by High Priority first, then uncompleted, then duration)
        val top3 = activeTasks.filter { !it.isCompleted }
            .sortedWith(
                compareByDescending<Task> { it.priority.ordinal }
                    .thenByDescending { it.estimatedDurationMinutes }
            )
            .take(3)

        // 4. Calculate Readiness Score (0..100)
        // High readiness = reasonable task count (<= 5), clear top 3, low rollover stress
        val taskCountPenalty = (activeTasks.filter { !it.isCompleted }.size - 3).coerceAtLeast(0) * 8
        val rolloverPenalty = rolloverCount * 10
        val baseScore = 100 - taskCountPenalty - rolloverPenalty
        val readinessScore = baseScore.coerceIn(30, 100)

        // 5. Select Calm Focus Mantra based on load
        val focusMantra = when {
            activeTasks.isEmpty() -> "Bugün zihnini dinlendirmek ve yeni ufuklar keşfetmek için harika bir gün 🌿"
            readinessScore >= 80 -> "Net bir odak ve sakin bir ritimle günün en önemli 3 hedefine adım at 🎯"
            readinessScore >= 50 -> "Telaşsız ve dengeli ilerle. Bir seferde tek bir göreve odaklan 🕊️"
            else -> "Bugün yoğun görünüyor. Önceliksiz işleri ertelemekten çekinme, enerjini koru 🛡️"
        }

        return MorningRitualResult(
            date = targetDate,
            top3Tasks = top3,
            rolloverCount = rolloverCount,
            readinessScore = readinessScore,
            focusMantra = focusMantra
        )
    }

    /**
     * Executes the Evening Ritual Shutdown.
     * Reviews daily achievements, habits, uncompleted tasks, and produces a mindful summary.
     */
    fun executeEveningShutdown(
        tasks: List<Task>,
        habits: List<Habit>,
        habitLogs: List<HabitLog>,
        timeBlocks: List<TimeBlock>,
        targetDate: LocalDate = LocalDate.now(),
        eveningReflection: String? = null
    ): EveningShutdownResult {
        val todayTasks = tasks.filter { it.dueDate == null || it.dueDate == targetDate }
        val completedTasks = todayTasks.count { it.isCompleted }
        val pendingTasks = todayTasks.count { !it.isCompleted }

        val rolloverCandidates = todayTasks.filter { !it.isCompleted && it.autoRollover }

        // Active habits for today's day of week
        val activeHabits = habits.filter { it.targetDaysOfWeek.contains(targetDate.dayOfWeek) }
        val completedHabits = activeHabits.count { habit ->
            habitLogs.any { it.habitId == habit.id && it.date == targetDate && it.isCompleted }
        }

        // Focus minutes from completed time blocks
        val focusMinutes = timeBlocks.filter { it.date == targetDate }
            .sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes().toInt() }

        val totalActions = todayTasks.size + activeHabits.size
        val completedActions = completedTasks + completedHabits
        val accomplishmentPercent = if (totalActions > 0) {
            ((completedActions.toFloat() / totalActions) * 100).toInt()
        } else {
            100
        }

        val reflectionPrompt = when {
            accomplishmentPercent >= 80 -> "Harika bir gün geçirdin! Zihnini dinlenmeye bırak, yarın yeni bir başlangıç 🌙"
            accomplishmentPercent >= 50 -> "Güzel bir ritim yakaladın. Kalan işler sakin bir şekilde yarına devredildi 🌿"
            else -> "Bugün her şey planlandığı gibi gitmemiş olabilir. Kendine nazik ol ve dinlenmenin tadını çıkar ✨"
        }

        return EveningShutdownResult(
            date = targetDate,
            completedTasksCount = completedTasks,
            pendingTasksCount = pendingTasks,
            rolloverCandidates = rolloverCandidates,
            habitsCompletedCount = completedHabits,
            totalHabitsCount = activeHabits.size,
            focusMinutesLogged = focusMinutes,
            accomplishmentScorePercent = accomplishmentPercent,
            reflectionPrompt = eveningReflection ?: reflectionPrompt
        )
    }
}
