package com.nilian.app

import com.google.common.truth.Truth.assertThat
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.usecase.BrainDumpUseCase
import com.nilian.app.domain.usecase.DayTemplateUseCase
import com.nilian.app.domain.usecase.LanSyncDataUseCase
import com.nilian.app.domain.usecase.MilestoneCountdownUseCase
import com.nilian.app.domain.usecase.MilestonePaceStatus
import com.nilian.app.domain.usecase.RitualEngineUseCase
import com.nilian.app.domain.usecase.TimeBudgetAllocatorUseCase
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NilianFeatureExpansionTest {

    private val ritualEngine = RitualEngineUseCase()
    private val dayTemplateEngine = DayTemplateUseCase()
    private val brainDumpEngine = BrainDumpUseCase()
    private val timeBudgetAllocator = TimeBudgetAllocatorUseCase()
    private val milestoneCountdownEngine = MilestoneCountdownUseCase()
    private val lanSyncEngine = LanSyncDataUseCase()

    private val testDate = LocalDate.of(2026, 8, 22)

    // =====================================================================================
    // 1. Morning & Evening Rituals
    // =====================================================================================

    @Test
    fun morningKickoff_selectsTop3Tasks_andCalculatesReadinessScore() {
        val task1 = Task(id = 1L, title = "Algoritmalar Final Projesi", priority = Priority.HIGH, estimatedDurationMinutes = 90, dueDate = testDate)
        val task2 = Task(id = 2L, title = "Nilian Release Notları", priority = Priority.HIGH, estimatedDurationMinutes = 45, dueDate = testDate)
        val task3 = Task(id = 3L, title = "E-posta Yanıtla", priority = Priority.MEDIUM, estimatedDurationMinutes = 20, dueDate = testDate)
        val task4 = Task(id = 4L, title = "Fatura Öde", priority = Priority.LOW, estimatedDurationMinutes = 10, dueDate = testDate)

        val habits = listOf(
            Habit(id = 1L, title = "Sabah Meditasyonu", targetDaysOfWeek = setOf(testDate.dayOfWeek))
        )

        val result = ritualEngine.executeMorningKickoff(listOf(task1, task2, task3, task4), habits, testDate)

        assertThat(result.top3Tasks).hasSize(3)
        assertThat(result.top3Tasks[0].id).isEqualTo(1L)
        assertThat(result.top3Tasks[1].id).isEqualTo(2L)
        assertThat(result.rolloverCount).isEqualTo(0)
        assertThat(result.readinessScore).isAtLeast(70)
        assertThat(result.focusMantra).isNotEmpty()
    }

    @Test
    fun morningKickoff_calculatesRolloverPenaltyAndSetsCalmMantra() {
        val pastDate = testDate.minusDays(2)
        val rolloverTask1 = Task(id = 1L, title = "Geciken Ödev", isCompleted = false, autoRollover = true, dueDate = pastDate)
        val rolloverTask2 = Task(id = 2L, title = "Geciken Rapor", isCompleted = false, autoRollover = true, dueDate = pastDate)

        val result = ritualEngine.executeMorningKickoff(listOf(rolloverTask1, rolloverTask2), emptyList(), testDate)

        assertThat(result.rolloverCount).isEqualTo(2)
        assertThat(result.readinessScore).isLessThan(100)
    }

    @Test
    fun eveningShutdown_reviewsAccomplishmentsAndRollovers() {
        val completedTask = Task(id = 1L, title = "Tamamlanan Görev", isCompleted = true, dueDate = testDate)
        val uncompletedRollover = Task(id = 2L, title = "Kalan Görev", isCompleted = false, autoRollover = true, dueDate = testDate)
        val habit = Habit(id = 1L, title = "Kitap Okuma", targetDaysOfWeek = setOf(testDate.dayOfWeek))
        val habitLog = HabitLog(habitId = 1L, date = testDate, isCompleted = true)

        val block = TimeBlock(
            id = 1L,
            title = "Derin Odak",
            blockType = BlockType.DEEP_WORK,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            date = testDate
        )

        val result = ritualEngine.executeEveningShutdown(
            tasks = listOf(completedTask, uncompletedRollover),
            habits = listOf(habit),
            habitLogs = listOf(habitLog),
            timeBlocks = listOf(block),
            targetDate = testDate
        )

        assertThat(result.completedTasksCount).isEqualTo(1)
        assertThat(result.pendingTasksCount).isEqualTo(1)
        assertThat(result.rolloverCandidates).hasSize(1)
        assertThat(result.habitsCompletedCount).isEqualTo(1)
        assertThat(result.focusMinutesLogged).isEqualTo(120)
        // 2 of 3 actions completed -> ~66%
        assertThat(result.accomplishmentScorePercent).isEqualTo(66)
        assertThat(result.reflectionPrompt).isNotEmpty()
    }

    // =====================================================================================
    // 2. Day Template Application
    // =====================================================================================

    @Test
    fun instantiateTemplate_generatesValidTimeBlocksForDate() {
        val defaultTemplate = dayTemplateEngine.getPredefinedTemplates().first()
        val blocks = dayTemplateEngine.instantiateTemplateBlocks(defaultTemplate.blocks, testDate)

        assertThat(blocks).isNotEmpty()
        assertThat(blocks.all { it.date == testDate }).isTrue()
        assertThat(blocks.first().blockType).isEqualTo(BlockType.REST)
    }

    @Test
    fun getPredefinedTemplates_returnsAllThreeArchetypes() {
        val templates = dayTemplateEngine.getPredefinedTemplates()

        assertThat(templates).hasSize(3)
        assertThat(templates.map { it.type.name }).containsExactly(
            "EXAM_DAY",
            "DEEP_CODING",
            "WEEKEND_REST"
        )
    }

    // =====================================================================================
    // 3. Brain Dump Conversion
    // =====================================================================================

    @Test
    fun parseBrainDump_parsesPrioritiesDurationsTagsAndDueDates() {
        val rawInput = """
            - Algoritmalar ödevini bitir !high (90m) #ders @today
            * Spor salonuna git (45m) #sağlık @tomorrow
            1. Kitap oku !low (20m) #gelişim
            [ ] Basit yapılacak iş
        """.trimIndent()

        val parsed = brainDumpEngine.parseBrainDump(rawInput, testDate)

        assertThat(parsed).hasSize(4)

        // Task 1
        assertThat(parsed[0].title).isEqualTo("Algoritmalar ödevini bitir")
        assertThat(parsed[0].priority).isEqualTo(Priority.HIGH)
        assertThat(parsed[0].estimatedDurationMinutes).isEqualTo(90)
        assertThat(parsed[0].description).isEqualTo("Etiket: #ders")
        assertThat(parsed[0].dueDate).isEqualTo(testDate)

        // Task 2
        assertThat(parsed[1].title).isEqualTo("Spor salonuna git")
        assertThat(parsed[1].estimatedDurationMinutes).isEqualTo(45)
        assertThat(parsed[1].description).isEqualTo("Etiket: #sağlık")
        assertThat(parsed[1].dueDate).isEqualTo(testDate.plusDays(1))

        // Task 3
        assertThat(parsed[2].title).isEqualTo("Kitap oku")
        assertThat(parsed[2].priority).isEqualTo(Priority.LOW)
        assertThat(parsed[2].estimatedDurationMinutes).isEqualTo(20)

        // Task 4
        assertThat(parsed[3].title).isEqualTo("Basit yapılacak iş")
        assertThat(parsed[3].priority).isEqualTo(Priority.MEDIUM)
        assertThat(parsed[3].estimatedDurationMinutes).isEqualTo(30)
    }

    @Test
    fun parseBrainDump_handlesEmptyOrWhitespaceTextGracefully() {
        val parsed = brainDumpEngine.parseBrainDump("   \n\n   ", testDate)
        assertThat(parsed).isEmpty()
    }

    // =====================================================================================
    // 4. Time Budget Calculations
    // =====================================================================================

    @Test
    fun calculateBudget_allocatesMinutesAcrossCategories() {
        val tasks = listOf(
            Task(id = 1L, title = "Kodlama", estimatedDurationMinutes = 60, dueDate = testDate)
        )
        val events = listOf(
            Event(
                id = 2L,
                title = "Ders",
                startDateTime = LocalDateTime.of(testDate, LocalTime.of(10, 0)),
                endDateTime = LocalDateTime.of(testDate, LocalTime.of(12, 0)),
                category = EventCategory.LECTURE
            )
        )
        val blocks = listOf(
            TimeBlock(
                id = 3L,
                title = "Koşu",
                blockType = BlockType.WORKOUT,
                startTime = LocalTime.of(17, 0),
                endTime = LocalTime.of(18, 0),
                date = testDate
            )
        )

        val budget = timeBudgetAllocator.calculateBudget(tasks, events, blocks, testDate, wakingHoursBudget = 16)

        // 60 min task + 120 min event + 60 min block = 240 min
        assertThat(budget.totalCommittedMinutes).isEqualTo(240)
        assertThat(budget.studyMinutes).isEqualTo(120)
        assertThat(budget.healthWorkoutMinutes).isEqualTo(60)
        assertThat(budget.remainingFreeMinutes).isEqualTo(16 * 60 - 240)
        assertThat(budget.isOverBudget).isFalse()
    }

    @Test
    fun calculateBudget_detectsOverBudgetWhenCommitmentsExceedWakingHours() {
        val hugeBlock = TimeBlock(
            id = 1L,
            title = "Aşırı Yoğun Çalışma",
            blockType = BlockType.DEEP_WORK,
            startTime = LocalTime.of(6, 0),
            endTime = LocalTime.of(23, 0),
            date = testDate
        )

        val budget = timeBudgetAllocator.calculateBudget(emptyList(), emptyList(), listOf(hugeBlock), testDate, wakingHoursBudget = 16)

        assertThat(budget.isOverBudget).isTrue()
        assertThat(budget.calmGuidance).contains("aş")
    }

    // =====================================================================================
    // 5. Milestone Countdowns
    // =====================================================================================

    @Test
    fun calculateMilestone_computesDaysRemainingAndPaceForUpcomingGoal() {
        val targetDate = testDate.plusDays(14)
        val goal = Goal(
            id = 1L,
            title = "Nilian 1.0 Lansmanı",
            targetDate = targetDate,
            progressPercent = 0.5f
        )
        val tasks = listOf(
            Task(id = 1L, title = "Görev 1", isCompleted = true, goalId = 1L),
            Task(id = 2L, title = "Görev 2", isCompleted = false, goalId = 1L)
        )

        val result = milestoneCountdownEngine.calculateMilestone(goal, tasks, testDate)

        assertThat(result.daysRemaining).isEqualTo(14L)
        assertThat(result.completedLinkedTasks).isEqualTo(1)
        assertThat(result.totalLinkedTasks).isEqualTo(2)
        assertThat(result.progressPercent).isEqualTo(0.5f)
        assertThat(result.countdownFormatted).isEqualTo("14 gün kaldı")
        assertThat(result.requiredTasksPerWeek).isEqualTo(0.5f)
    }

    @Test
    fun calculateMilestone_detectsOverdueGoalStatus() {
        val pastTarget = testDate.minusDays(5)
        val goal = Goal(
            id = 2L,
            title = "Gecikmiş Proje",
            targetDate = pastTarget,
            progressPercent = 0.2f
        )

        val result = milestoneCountdownEngine.calculateMilestone(goal, emptyList(), testDate)

        assertThat(result.status).isEqualTo(MilestonePaceStatus.OVERDUE)
        assertThat(result.countdownFormatted).contains("gecikti")
    }

    @Test
    fun calculateMilestone_handlesGoalsWithoutDeadline() {
        val goal = Goal(
            id = 3L,
            title = "Sürekli Gelişim",
            targetDate = null,
            progressPercent = 0.8f
        )

        val result = milestoneCountdownEngine.calculateMilestone(goal, emptyList(), testDate)

        assertThat(result.status).isEqualTo(MilestonePaceStatus.NO_DEADLINE)
        assertThat(result.countdownFormatted).isEqualTo("Süresiz Vizyon")
    }

    // =====================================================================================
    // 6. LAN Sync Data Formatting & Reconciliation
    // =====================================================================================

    @Test
    fun createSyncPayload_generatesValidSha256Checksum() {
        val task = Task(id = 1L, title = "Sync Test", dueDate = testDate)
        val payload = lanSyncEngine.createSyncPayload(
            deviceId = "device-alpha-123",
            deviceName = "Pixel Tablet",
            tasks = listOf(task),
            events = emptyList(),
            habits = emptyList(),
            habitLogs = emptyList(),
            timeBlocks = emptyList(),
            goals = emptyList()
        )

        assertThat(payload.checksum).isNotEmpty()
        assertThat(payload.checksum).hasLength(64) // SHA-256 is 64 hex characters
        assertThat(lanSyncEngine.verifyPayloadIntegrity(payload)).isTrue()
    }

    @Test
    fun verifyPayloadIntegrity_rejectsTamperedPayload() {
        val payload = lanSyncEngine.createSyncPayload(
            deviceId = "device-1",
            deviceName = "Phone",
            tasks = emptyList(),
            events = emptyList(),
            habits = emptyList(),
            habitLogs = emptyList(),
            timeBlocks = emptyList(),
            goals = emptyList()
        )

        val tamperedPayload = payload.copy(checksum = "invalidchecksum1234567890abcdef")
        assertThat(lanSyncEngine.verifyPayloadIntegrity(tamperedPayload)).isFalse()
    }

    @Test
    fun reconcileSync_mergesTasksAndUnionsHabitLogsDeterministically() {
        val localTask = Task(id = 1L, title = "Local Task", isCompleted = false)
        val remoteTask = Task(id = 2L, title = "Remote Task", isCompleted = true)

        val habit = Habit(id = 10L, title = "Egzersiz", bestStreak = 5)
        val localLog = HabitLog(habitId = 10L, date = testDate, isCompleted = true)
        val remoteLog = HabitLog(habitId = 10L, date = testDate.minusDays(1), isCompleted = true)

        val remotePayload = lanSyncEngine.createSyncPayload(
            deviceId = "remote-tablet",
            deviceName = "Tablet",
            tasks = listOf(remoteTask),
            events = emptyList(),
            habits = listOf(habit.copy(bestStreak = 10)),
            habitLogs = listOf(remoteLog),
            timeBlocks = emptyList(),
            goals = emptyList()
        )

        val mergeResult = lanSyncEngine.reconcileSync(
            localTasks = listOf(localTask),
            localEvents = emptyList(),
            localHabits = listOf(habit),
            localHabitLogs = listOf(localLog),
            localTimeBlocks = emptyList(),
            localGoals = emptyList(),
            remotePayload = remotePayload
        )

        assertThat(mergeResult.mergedTasks).hasSize(2)
        assertThat(mergeResult.mergedHabitLogs).hasSize(2)
        assertThat(mergeResult.mergedHabits.first().bestStreak).isEqualTo(10)
    }
}
