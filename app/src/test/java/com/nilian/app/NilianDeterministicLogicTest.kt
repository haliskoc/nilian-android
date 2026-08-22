package com.nilian.app

import com.google.common.truth.Truth.assertThat
import com.nilian.app.domain.model.BackupData
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.model.WorkloadLevel
import com.nilian.app.domain.usecase.DetectCollisionsUseCase
import com.nilian.app.domain.usecase.FreeSlotFinderUseCase
import com.nilian.app.domain.usecase.HabitStreakCalculatorUseCase
import com.nilian.app.domain.usecase.JsonBackupRestoreUseCase
import com.nilian.app.domain.usecase.TaskRolloverUseCase
import com.nilian.app.domain.usecase.WorkloadStressUseCase
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NilianDeterministicLogicTest {

    private val detectCollisions = DetectCollisionsUseCase()
    private val streakCalculator = HabitStreakCalculatorUseCase()
    private val taskRollover = TaskRolloverUseCase()
    private val freeSlotFinder = FreeSlotFinderUseCase()
    private val workloadStress = WorkloadStressUseCase()
    private val jsonBackupRestore = JsonBackupRestoreUseCase()

    // -------------------------------------------------------------------------------------
    // 1. Collision Detection Tests
    // -------------------------------------------------------------------------------------

    @Test
    fun detectCollisions_overlappingEvents_findsConflict() {
        val today = LocalDate.of(2026, 8, 22)
        val event1 = Event(
            id = 1L,
            title = "Ders: Algoritmalar",
            startDateTime = LocalDateTime.of(today, LocalTime.of(10, 0)),
            endDateTime = LocalDateTime.of(today, LocalTime.of(12, 0))
        )
        val event2 = Event(
            id = 2L,
            title = "Proje Toplantısı",
            startDateTime = LocalDateTime.of(today, LocalTime.of(11, 30)),
            endDateTime = LocalDateTime.of(today, LocalTime.of(13, 0))
        )

        val conflicts = detectCollisions(listOf(event1, event2), emptyList(), today)

        assertThat(conflicts).hasSize(1)
        val conflict = conflicts.first()
        assertThat(conflict.overlapDurationMinutes).isEqualTo(30L)
        assertThat(conflict.overlapStart).isEqualTo(LocalDateTime.of(today, LocalTime.of(11, 30)))
        assertThat(conflict.overlapEnd).isEqualTo(LocalDateTime.of(today, LocalTime.of(12, 0)))
    }

    @Test
    fun detectCollisions_backToBackBlocks_noConflict() {
        val today = LocalDate.of(2026, 8, 22)
        val block1 = TimeBlock(
            id = 1L,
            title = "Derin Odak",
            blockType = BlockType.DEEP_WORK,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 30),
            date = today
        )
        val block2 = TimeBlock(
            id = 2L,
            title = "Dinlenme",
            blockType = BlockType.REST,
            startTime = LocalTime.of(10, 30),
            endTime = LocalTime.of(11, 0),
            date = today
        )

        val conflicts = detectCollisions(emptyList(), listOf(block1, block2), today)

        assertThat(conflicts).isEmpty()
    }

    @Test
    fun detectCollisions_eventAndTimeBlockCollision_detected() {
        val today = LocalDate.of(2026, 8, 22)
        val event = Event(
            id = 1L,
            title = "Doktor Randevusu",
            startDateTime = LocalDateTime.of(today, LocalTime.of(14, 0)),
            endDateTime = LocalDateTime.of(today, LocalTime.of(15, 0))
        )
        val block = TimeBlock(
            id = 2L,
            title = "Çalışma Bloğu",
            blockType = BlockType.STUDY,
            startTime = LocalTime.of(14, 30),
            endTime = LocalTime.of(16, 30),
            date = today
        )

        val conflicts = detectCollisions(listOf(event), listOf(block), today)

        assertThat(conflicts).hasSize(1)
        assertThat(conflicts.first().overlapDurationMinutes).isEqualTo(30L)
    }

    // -------------------------------------------------------------------------------------
    // 2. Habit Streak Calculation Tests
    // -------------------------------------------------------------------------------------

    @Test
    fun habitStreak_consecutiveDaysCompleted_calculatesStreak() {
        val habit = Habit(
            id = 1L,
            title = "Kitap Okuma",
            createdAt = LocalDate.of(2026, 8, 1),
            targetDaysOfWeek = DayOfWeek.values().toSet()
        )
        val today = LocalDate.of(2026, 8, 22)
        val logs = (0..4).map { offset ->
            HabitLog(habitId = 1L, date = today.minusDays(offset.toLong()), isCompleted = true)
        }

        val result = streakCalculator(habit, logs, today)

        assertThat(result.currentStreak).isEqualTo(5)
        assertThat(result.isCompletedToday).isTrue()
        assertThat(result.bestStreak).isAtLeast(5)
    }

    @Test
    fun habitStreak_todayNotYetCompleted_preservesPastStreak() {
        val habit = Habit(
            id = 1L,
            title = "Meditasyon",
            createdAt = LocalDate.of(2026, 8, 1),
            targetDaysOfWeek = DayOfWeek.values().toSet()
        )
        val today = LocalDate.of(2026, 8, 22)
        // Yesterday and day before completed, today not yet
        val logs = listOf(
            HabitLog(habitId = 1L, date = today.minusDays(1), isCompleted = true),
            HabitLog(habitId = 1L, date = today.minusDays(2), isCompleted = true)
        )

        val result = streakCalculator(habit, logs, today)

        assertThat(result.currentStreak).isEqualTo(2)
        assertThat(result.isCompletedToday).isFalse()
    }

    @Test
    fun habitStreak_skipsNonTargetDaysWithoutPenalty() {
        // Weekday habit (Mon to Fri)
        val weekdaySet = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        val habit = Habit(
            id = 1L,
            title = "Kodlama Sprinti",
            createdAt = LocalDate.of(2026, 8, 1),
            targetDaysOfWeek = weekdaySet
        )
        // Monday (2026-08-17) to Friday (2026-08-21), reference date is Monday (2026-08-24)
        val refMonday = LocalDate.of(2026, 8, 24)
        val friday = LocalDate.of(2026, 8, 21)
        val thursday = LocalDate.of(2026, 8, 20)
        val wednesday = LocalDate.of(2026, 8, 19)

        val logs = listOf(
            HabitLog(habitId = 1L, date = refMonday, isCompleted = true),
            HabitLog(habitId = 1L, date = friday, isCompleted = true),
            HabitLog(habitId = 1L, date = thursday, isCompleted = true),
            HabitLog(habitId = 1L, date = wednesday, isCompleted = true)
        )

        val result = streakCalculator(habit, logs, refMonday)

        // Weekend days (Sat, Sun) are skipped, streak continues uninterrupted
        assertThat(result.currentStreak).isEqualTo(4)
    }

    // -------------------------------------------------------------------------------------
    // 3. Task Rollover Tests
    // -------------------------------------------------------------------------------------

    @Test
    fun taskRollover_overdueUncompletedWithAutoRollover_rollsOverToToday() {
        val today = LocalDate.of(2026, 8, 22)
        val overdueTask = Task(
            id = 1L,
            title = "Bitirilecek Rapor",
            dueDate = LocalDate.of(2026, 8, 20),
            isCompleted = false,
            autoRollover = true
        )
        val completedTask = Task(
            id = 2L,
            title = "Tamamlanmış Görev",
            dueDate = LocalDate.of(2026, 8, 20),
            isCompleted = true,
            autoRollover = true
        )
        val manualTask = Task(
            id = 3L,
            title = "Manuel Görev",
            dueDate = LocalDate.of(2026, 8, 20),
            isCompleted = false,
            autoRollover = false
        )
        val futureTask = Task(
            id = 4L,
            title = "Gelecek Görev",
            dueDate = LocalDate.of(2026, 8, 25),
            isCompleted = false,
            autoRollover = true
        )

        val tasks = listOf(overdueTask, completedTask, manualTask, futureTask)
        val rolledOver = taskRollover.rolloverTasks(tasks, today)

        assertThat(rolledOver).hasSize(1)
        assertThat(rolledOver.first().id).isEqualTo(1L)
        assertThat(rolledOver.first().dueDate).isEqualTo(today)
    }

    // -------------------------------------------------------------------------------------
    // 4. Free Slot Finder Tests
    // -------------------------------------------------------------------------------------

    @Test
    fun freeSlotFinder_findsGapsBetweenBlocks() {
        val today = LocalDate.of(2026, 8, 22)
        val event1 = Event(
            id = 1L,
            title = "Sabah Toplantısı",
            startDateTime = LocalDateTime.of(today, LocalTime.of(9, 0)),
            endDateTime = LocalDateTime.of(today, LocalTime.of(10, 0))
        )
        val event2 = Event(
            id = 2L,
            title = "Öğleden Sonra Semineri",
            startDateTime = LocalDateTime.of(today, LocalTime.of(14, 0)),
            endDateTime = LocalDateTime.of(today, LocalTime.of(16, 0))
        )

        val slots = freeSlotFinder(
            events = listOf(event1, event2),
            timeBlocks = emptyList(),
            date = today,
            dayStart = LocalTime.of(8, 0),
            dayEnd = LocalTime.of(18, 0),
            minDurationMinutes = 30
        )

        // Slots: [08:00 - 09:00 (60m)], [10:00 - 14:00 (240m)], [16:00 - 18:00 (120m)]
        assertThat(slots).hasSize(3)
        assertThat(slots[0].startTime).isEqualTo(LocalTime.of(8, 0))
        assertThat(slots[0].endTime).isEqualTo(LocalTime.of(9, 0))
        assertThat(slots[1].startTime).isEqualTo(LocalTime.of(10, 0))
        assertThat(slots[1].endTime).isEqualTo(LocalTime.of(14, 0))
        assertThat(slots[1].durationMinutes).isEqualTo(240L)
    }

    // -------------------------------------------------------------------------------------
    // 5. Workload Stress Assessment Tests
    // -------------------------------------------------------------------------------------

    @Test
    fun workloadStress_heavyDay_triggersMindfulAlert() {
        val today = LocalDate.of(2026, 8, 22)
        // 6 hours of events
        val event = Event(
            id = 1L,
            title = "Uzun Çalıştay",
            startDateTime = LocalDateTime.of(today, LocalTime.of(9, 0)),
            endDateTime = LocalDateTime.of(today, LocalTime.of(15, 0))
        )
        // 5 hours of deep work blocks
        val block = TimeBlock(
            id = 2L,
            title = "Sprint Kodlama",
            blockType = BlockType.DEEP_WORK,
            startTime = LocalTime.of(15, 0),
            endTime = LocalTime.of(20, 0),
            date = today
        )
        // 1 hour of tasks
        val task = Task(
            id = 3L,
            title = "Dokümantasyon",
            estimatedDurationMinutes = 60,
            dueDate = today
        )

        val assessment = workloadStress(listOf(task), listOf(event), listOf(block), today)

        assertThat(assessment.totalCommittedMinutes).isEqualTo(720) // 12 hours
        assertThat(assessment.workloadLevel).isEqualTo(WorkloadLevel.HEAVY)
        assertThat(assessment.isHeavyLoad).isTrue()
        assertThat(assessment.suggestion).contains("10 saatin üzerinde")
    }

    @Test
    fun workloadStress_balancedDay_isBalanced() {
        val today = LocalDate.of(2026, 8, 22)
        val event = Event(
            id = 1L,
            title = "Ders",
            startDateTime = LocalDateTime.of(today, LocalTime.of(10, 0)),
            endDateTime = LocalDateTime.of(today, LocalTime.of(12, 0))
        )
        val task = Task(
            id = 2L,
            title = "Ödev",
            estimatedDurationMinutes = 90,
            dueDate = today
        )

        val assessment = workloadStress(listOf(task), listOf(event), emptyList(), today)

        assertThat(assessment.totalCommittedMinutes).isEqualTo(210) // 3.5 hours
        assertThat(assessment.workloadLevel).isEqualTo(WorkloadLevel.LIGHT)
        assertThat(assessment.isHeavyLoad).isFalse()
    }

    // -------------------------------------------------------------------------------------
    // 6. JSON Backup and Restore Tests
    // -------------------------------------------------------------------------------------

    @Test
    fun jsonBackupRestore_roundtripSerialization_preservesAllData() {
        val task = Task(
            id = 101L,
            title = "Kotlin Refactor",
            description = "Tüm use case testlerini yaz",
            priority = Priority.HIGH,
            estimatedDurationMinutes = 45,
            dueDate = LocalDate.of(2026, 8, 22),
            isCompleted = false,
            goalId = 55L,
            autoRollover = true
        )
        val event = Event(
            id = 201L,
            title = "Sprint Retrospective",
            locationOrLink = "Google Meet",
            startDateTime = LocalDateTime.of(2026, 8, 22, 16, 0),
            endDateTime = LocalDateTime.of(2026, 8, 22, 17, 0),
            category = EventCategory.WORK,
            colorHex = "#4E876A"
        )
        val habit = Habit(
            id = 301L,
            title = "Her Gün 20 Sayfa Oku",
            targetDaysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            currentStreak = 7,
            bestStreak = 21,
            createdAt = LocalDate.of(2026, 7, 1)
        )
        val habitLog = HabitLog(
            habitId = 301L,
            date = LocalDate.of(2026, 8, 22),
            isCompleted = true
        )
        val timeBlock = TimeBlock(
            id = 401L,
            title = "Sabah Koşusu",
            blockType = BlockType.WORKOUT,
            startTime = LocalTime.of(7, 0),
            endTime = LocalTime.of(7, 45),
            date = LocalDate.of(2026, 8, 22)
        )
        val goal = Goal(
            id = 55L,
            title = "Nilian 1.0 Release",
            description = "Google Play ve GitHub CI/CD ile dağıtım",
            targetDate = LocalDate.of(2026, 9, 1),
            progressPercent = 0.85f,
            isArchived = false
        )

        val originalData = BackupData(
            version = 1,
            exportTimestamp = LocalDateTime.of(2026, 8, 22, 21, 0),
            tasks = listOf(task),
            events = listOf(event),
            habits = listOf(habit),
            habitLogs = listOf(habitLog),
            timeBlocks = listOf(timeBlock),
            goals = listOf(goal)
        )

        // 1. Serialize to JSON string
        val jsonString = jsonBackupRestore.exportToJson(originalData)
        assertThat(jsonString).isNotEmpty()
        assertThat(jsonString).contains("Kotlin Refactor")
        assertThat(jsonString).contains("Sprint Retrospective")
        assertThat(jsonString).contains("Nilian 1.0 Release")

        // 2. Parse back from JSON string
        val parsedData = jsonBackupRestore.parseFromJson(jsonString)

        // 3. Verify exact parity
        assertThat(parsedData.tasks).hasSize(1)
        assertThat(parsedData.tasks.first()).isEqualTo(task)

        assertThat(parsedData.events).hasSize(1)
        assertThat(parsedData.events.first()).isEqualTo(event)

        assertThat(parsedData.habits).hasSize(1)
        assertThat(parsedData.habits.first().title).isEqualTo(habit.title)
        assertThat(parsedData.habits.first().currentStreak).isEqualTo(habit.currentStreak)

        assertThat(parsedData.habitLogs).hasSize(1)
        assertThat(parsedData.habitLogs.first()).isEqualTo(habitLog)

        assertThat(parsedData.timeBlocks).hasSize(1)
        assertThat(parsedData.timeBlocks.first()).isEqualTo(timeBlock)

        assertThat(parsedData.goals).hasSize(1)
        assertThat(parsedData.goals.first()).isEqualTo(goal)
    }
}
