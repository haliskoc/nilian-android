package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.EveningCloseoutReport
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.repository.HabitRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.domain.repository.TimeBlockRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Deterministic evening closeout and daily reflection engine for Nilian.
 *
 * Responsibilities:
 * 1. Aggregates completed and uncompleted tasks for the target date.
 * 2. Sums total focus, study, deep work, workout, and rest minutes from daily time blocks.
 * 3. Evaluates daily habit consistency and completion percentage.
 * 4. Computes a balanced 0-100 closeout score and mindful reflection feedback.
 * 5. Flags uncompleted tasks and automatically prepares or executes rollover to tomorrow.
 */
class EveningCloseoutUseCase(
    private val taskRepository: TaskRepository? = null,
    private val timeBlockRepository: TimeBlockRepository? = null,
    private val habitRepository: HabitRepository? = null,
    private val taskRolloverUseCase: TaskRolloverUseCase = TaskRolloverUseCase(taskRepository)
) {

    /**
     * Pure function to generate an [EveningCloseoutReport] from provided collections.
     */
    operator fun invoke(
        date: LocalDate = LocalDate.now(),
        tasks: List<Task>,
        timeBlocks: List<TimeBlock>,
        habits: List<Habit>,
        habitLogs: List<HabitLog>
    ): EveningCloseoutReport {
        // 1. Task Segregation
        val completedTasks = tasks.filter { task ->
            task.isCompleted && (task.completedAt?.toLocalDate() == date || task.dueDate == date)
        }

        val uncompletedTasks = tasks.filter { task ->
            !task.isCompleted && (task.dueDate == date || (task.dueDate != null && task.dueDate.isBefore(date)))
        }

        val rolledOverTasks = uncompletedTasks.filter { it.autoRollover }

        val completedTaskMinutes = completedTasks.sumOf { it.estimatedDurationMinutes }

        // 2. Time Block Focus Aggregations
        val dayBlocks = timeBlocks.filter { it.date == date }
        var deepWorkMinutes = 0
        var studyMinutes = 0
        var workoutMinutes = 0
        var restMinutes = 0

        for (block in dayBlocks) {
            val duration = if (block.endTime.isAfter(block.startTime)) {
                Duration.between(block.startTime, block.endTime).toMinutes().toInt()
            } else {
                Duration.between(block.startTime, LocalTime.MAX).toMinutes().toInt()
            }

            when (block.blockType) {
                BlockType.DEEP_WORK -> deepWorkMinutes += duration
                BlockType.STUDY -> studyMinutes += duration
                BlockType.WORKOUT -> workoutMinutes += duration
                BlockType.REST, BlockType.SLEEP -> restMinutes += duration
                else -> {}
            }
        }

        val totalFocusMinutes = deepWorkMinutes + studyMinutes

        // 3. Habit Calculations
        val scheduledHabitsToday = habits.filter { habit ->
            habit.targetDaysOfWeek.contains(date.dayOfWeek)
        }
        val completedLogsToday = habitLogs.filter { log ->
            log.date == date && log.isCompleted && scheduledHabitsToday.any { it.id == log.habitId }
        }

        val totalHabitsCount = scheduledHabitsToday.size
        val completedHabitsCount = completedLogsToday.size
        val habitCompletionRate = if (totalHabitsCount > 0) {
            completedHabitsCount.toFloat() / totalHabitsCount.toFloat()
        } else {
            1.0f
        }

        // 4. Compute Calm Closeout Score (0 - 100)
        val score = calculateCloseoutScore(
            completedTasksCount = completedTasks.size,
            uncompletedTasksCount = uncompletedTasks.size,
            habitRate = habitCompletionRate,
            focusMinutes = totalFocusMinutes
        )

        // 5. Generate Reflection Summary
        val reflection = generateReflectionSummary(
            date = date,
            completedTasks = completedTasks,
            uncompletedTasks = uncompletedTasks,
            rolledOverCount = rolledOverTasks.size,
            totalFocusMinutes = totalFocusMinutes,
            workoutMinutes = workoutMinutes,
            habitRate = habitCompletionRate,
            score = score
        )

        return EveningCloseoutReport(
            date = date,
            completedTasks = completedTasks,
            uncompletedTasks = uncompletedTasks,
            rolledOverTasks = rolledOverTasks,
            completedTaskMinutes = completedTaskMinutes,
            totalFocusMinutes = totalFocusMinutes,
            deepWorkMinutes = deepWorkMinutes,
            studyMinutes = studyMinutes,
            workoutMinutes = workoutMinutes,
            restMinutes = restMinutes,
            totalHabitsCount = totalHabitsCount,
            completedHabitsCount = completedHabitsCount,
            habitCompletionRate = habitCompletionRate,
            reflectionSummary = reflection,
            closeoutScore = score,
            completedAt = LocalDateTime.now()
        )
    }

    /**
     * Computes a calm 0-100 rating based on effort and habit consistency without anxiety-inducing punishment.
     */
    fun calculateCloseoutScore(
        completedTasksCount: Int,
        uncompletedTasksCount: Int,
        habitRate: Float,
        focusMinutes: Int
    ): Int {
        val totalTasks = completedTasksCount + uncompletedTasksCount
        val taskComponent = if (totalTasks > 0) {
            (completedTasksCount.toFloat() / totalTasks.toFloat()) * 40f
        } else {
            35f // Default baseline if no tasks were scheduled
        }

        val habitComponent = habitRate * 30f

        // 120+ minutes of deep focus / study yields full 30 points
        val focusComponent = (focusMinutes.toFloat() / 120f).coerceIn(0f, 1f) * 30f

        return (taskComponent + habitComponent + focusComponent).toInt().coerceIn(0, 100)
    }

    /**
     * Formulates a mindful, non-judgmental evening reflection message.
     */
    fun generateReflectionSummary(
        date: LocalDate,
        completedTasks: List<Task>,
        uncompletedTasks: List<Task>,
        rolledOverCount: Int,
        totalFocusMinutes: Int,
        workoutMinutes: Int,
        habitRate: Float,
        score: Int
    ): String {
        val focusText = if (totalFocusMinutes >= 60) {
            "${totalFocusMinutes / 60} saat ${totalFocusMinutes % 60} dk"
        } else {
            "$totalFocusMinutes dk"
        }

        val habitPercent = (habitRate * 100).toInt()

        val sb = StringBuilder()
        sb.append("Bugün ${completedTasks.size} görev tamamlandı ve $focusText derin odak süresi kaydedildi.")

        if (workoutMinutes > 0) {
            sb.append(" $workoutMinutes dk hareket/spor ile bedenini tazeledin.")
        }

        if (habitPercent > 0) {
            sb.append(" Alışkanlık uyumu: %$habitPercent.")
        }

        if (rolledOverCount > 0) {
            sb.append(" Tamamlanmayan $rolledOverCount görev sakin bir şekilde yarına aktarılmak üzere işaretlendi.")
        }

        sb.append(" Şimdi günü huzurla geride bırakıp zihnini dinlendirme zamanı.")
        return sb.toString()
    }

    /**
     * Asynchronously loads data from repositories and generates the closeout report.
     */
    suspend fun getCloseoutReport(date: LocalDate = LocalDate.now()): EveningCloseoutReport {
        val tasks = taskRepository?.getAllTasksSync() ?: emptyList()
        val blocks = timeBlockRepository?.getAllTimeBlocksSync()?.filter { it.date == date } ?: emptyList()
        val habits = habitRepository?.getAllHabitsSync() ?: emptyList()
        val habitLogs = habitRepository?.getAllHabitLogsSync()?.filter { it.date == date } ?: emptyList()

        return invoke(
            date = date,
            tasks = tasks,
            timeBlocks = blocks,
            habits = habits,
            habitLogs = habitLogs
        )
    }

    /**
     * Executes closeout: generates the report and automatically updates uncompleted
     * tasks marked for auto-rollover to tomorrow's date.
     *
     * @param date The date being closed out.
     * @param autoRolloverToTomorrow If true, updates due date of unfinished auto-rollover tasks to date + 1 day.
     */
    suspend fun executeCloseout(
        date: LocalDate = LocalDate.now(),
        autoRolloverToTomorrow: Boolean = true
    ): EveningCloseoutReport {
        val report = getCloseoutReport(date)

        if (autoRolloverToTomorrow && report.rolledOverTasks.isNotEmpty() && taskRepository != null) {
            val tomorrow = date.plusDays(1)
            val updatedTasks = report.rolledOverTasks.map { task ->
                task.copy(dueDate = tomorrow)
            }
            taskRepository.updateTasks(updatedTasks)
        }

        return report
    }
}
