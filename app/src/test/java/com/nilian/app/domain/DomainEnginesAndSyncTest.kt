package com.nilian.app.domain

import com.nilian.app.core.sync.LocalWifiSyncManager
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.BrainDumpType
import com.nilian.app.domain.model.DayTemplateType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.InboxNote
import com.nilian.app.domain.model.MorningKickoffState
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.model.TimeBudgetStatus
import com.nilian.app.domain.usecase.BrainDumpUseCase
import com.nilian.app.domain.usecase.DayTemplateUseCase
import com.nilian.app.domain.usecase.EveningCloseoutUseCase
import com.nilian.app.domain.usecase.JsonBackupRestoreUseCase
import com.nilian.app.domain.usecase.MorningKickoffUseCase
import com.nilian.app.domain.usecase.TimeBudgetCalculatorUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DomainEnginesAndSyncTest {

    private val testDate = LocalDate.of(2026, 8, 22)

    // -------------------------------------------------------------------------------------
    // 1. MorningKickoffUseCase Tests
    // -------------------------------------------------------------------------------------
    @Test
    fun testMorningKickoff_evaluatesScheduleAndRecommendsTop3FocusTasks() {
        val useCase = MorningKickoffUseCase()

        val overdueTask = Task(
            id = 1L,
            title = "Dünden Kalan Kritik Görev",
            priority = Priority.HIGH,
            estimatedDurationMinutes = 60,
            dueDate = testDate.minusDays(1),
            isCompleted = false,
            autoRollover = true
        )

        val highPriorityToday = Task(
            id = 2L,
            title = "Yüksek Öncelikli Kodlama",
            priority = Priority.HIGH,
            estimatedDurationMinutes = 90,
            dueDate = testDate,
            isCompleted = false
        )

        val mediumPriorityToday = Task(
            id = 3L,
            title = "Orta Öncelikli İnceleme",
            priority = Priority.MEDIUM,
            estimatedDurationMinutes = 45,
            dueDate = testDate,
            isCompleted = false
        )

        val lowPriorityToday = Task(
            id = 4L,
            title = "Düşük Öncelikli Temizlik",
            priority = Priority.LOW,
            estimatedDurationMinutes = 30,
            dueDate = testDate,
            isCompleted = false
        )

        val completedTask = Task(
            id = 5L,
            title = "Tamamlanmış Görev",
            priority = Priority.HIGH,
            dueDate = testDate,
            isCompleted = true
        )

        val todayEvent = Event(
            id = 10L,
            title = "Sprint Planlama Toplantısı",
            startDateTime = LocalDateTime.of(testDate, LocalTime.of(10, 0)),
            endDateTime = LocalDateTime.of(testDate, LocalTime.of(11, 30)),
            category = EventCategory.MEETING
        )

        val todayBlock = TimeBlock(
            id = 20L,
            title = "Derin Odak Sprinti",
            blockType = BlockType.DEEP_WORK,
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(16, 0),
            date = testDate
        )

        val allTasks = listOf(overdueTask, highPriorityToday, mediumPriorityToday, lowPriorityToday, completedTask)
        val summary = useCase(
            today = testDate,
            allTasks = allTasks,
            todayEvents = listOf(todayEvent),
            todayTimeBlocks = listOf(todayBlock)
        )

        // Verifications
        assertEquals(1, summary.overdueRolloverTasks.size)
        assertEquals(1L, summary.overdueRolloverTasks.first().id)

        assertEquals(3, summary.recommendedFocusTasks.size)
        // High priority + overdue first
        assertEquals(1L, summary.recommendedFocusTasks[0].id)
        assertEquals(2L, summary.recommendedFocusTasks[1].id)
        assertEquals(3L, summary.recommendedFocusTasks[2].id)

        // Free slots calculated (06:00-10:00 = 240m, 11:30-14:00 = 150m, 16:00-23:00 = 420m -> 810m)
        assertTrue(summary.totalFreeSlotMinutes > 0)
        assertTrue(summary.morningPrompt.contains("Sprint Planlama Toplantısı"))
        assertTrue(summary.morningPrompt.contains("Dünden devreden 1 görev"))
    }

    @Test
    fun testMorningKickoff_statePersistence() {
        val useCase = MorningKickoffUseCase()

        val state = MorningKickoffState(
            date = testDate,
            selectedFocusTaskIds = listOf(2L, 3L),
            intentionNote = "Bugün mimariyi sadeleştir ve odaklan.",
            isCompleted = true,
            completedAt = LocalDateTime.of(testDate, LocalTime.of(8, 0))
        )

        useCase.saveState(state)
        val retrieved = useCase.getState(testDate)

        assertEquals(true, retrieved.isCompleted)
        assertEquals(listOf(2L, 3L), retrieved.selectedFocusTaskIds)
        assertEquals("Bugün mimariyi sadeleştir ve odaklan.", retrieved.intentionNote)
    }

    // -------------------------------------------------------------------------------------
    // 2. EveningCloseoutUseCase Tests
    // -------------------------------------------------------------------------------------
    @Test
    fun testEveningCloseout_aggregatesCompletedTasksAndFocusMinutes() {
        val useCase = EveningCloseoutUseCase()

        val completedTask1 = Task(
            id = 1L,
            title = "Bitirilen Veritabanı Modülü",
            estimatedDurationMinutes = 60,
            dueDate = testDate,
            isCompleted = true,
            completedAt = LocalDateTime.of(testDate, LocalTime.of(17, 0))
        )
        val completedTask2 = Task(
            id = 2L,
            title = "Tamamlanan UI İncelemesi",
            estimatedDurationMinutes = 45,
            dueDate = testDate,
            isCompleted = true,
            completedAt = LocalDateTime.of(testDate, LocalTime.of(18, 0))
        )
        val unfinishedRolloverTask = Task(
            id = 3L,
            title = "Kalan Dokümantasyon",
            estimatedDurationMinutes = 30,
            dueDate = testDate,
            isCompleted = false,
            autoRollover = true
        )
        val unfinishedManualTask = Task(
            id = 4L,
            title = "Kalan Opsiyonel İş",
            estimatedDurationMinutes = 30,
            dueDate = testDate,
            isCompleted = false,
            autoRollover = false
        )

        val deepWorkBlock = TimeBlock(
            id = 10L,
            title = "Derin Kodlama",
            blockType = BlockType.DEEP_WORK,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(11, 0), // 120 mins
            date = testDate
        )
        val studyBlock = TimeBlock(
            id = 11L,
            title = "Algoritma Dersi",
            blockType = BlockType.STUDY,
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(15, 0), // 60 mins
            date = testDate
        )
        val workoutBlock = TimeBlock(
            id = 12L,
            title = "Akşam Koşusu",
            blockType = BlockType.WORKOUT,
            startTime = LocalTime.of(18, 0),
            endTime = LocalTime.of(18, 45), // 45 mins
            date = testDate
        )

        val habit = Habit(
            id = 100L,
            title = "Günlük Kitap Okuma",
            targetDaysOfWeek = setOf(DayOfWeek.SATURDAY) // testDate is Saturday
        )
        val habitLog = HabitLog(habitId = 100L, date = testDate, isCompleted = true)

        val report = useCase(
            date = testDate,
            tasks = listOf(completedTask1, completedTask2, unfinishedRolloverTask, unfinishedManualTask),
            timeBlocks = listOf(deepWorkBlock, studyBlock, workoutBlock),
            habits = listOf(habit),
            habitLogs = listOf(habitLog)
        )

        assertEquals(2, report.completedTasks.size)
        assertEquals(2, report.uncompletedTasks.size)
        assertEquals(1, report.rolledOverTasks.size)
        assertEquals(3L, report.rolledOverTasks.first().id)
        assertEquals(105, report.completedTaskMinutes) // 60 + 45

        assertEquals(120, report.deepWorkMinutes)
        assertEquals(60, report.studyMinutes)
        assertEquals(180, report.totalFocusMinutes) // 120 + 60
        assertEquals(45, report.workoutMinutes)

        assertEquals(1, report.totalHabitsCount)
        assertEquals(1, report.completedHabitsCount)
        assertEquals(1.0f, report.habitCompletionRate, 0.01f)

        assertTrue(report.closeoutScore in 80..100)
        assertTrue(report.reflectionSummary.contains("2 görev tamamlandı"))
        assertTrue(report.reflectionSummary.contains("3 saat"))
        assertTrue(report.reflectionSummary.contains("1 görev sakin bir şekilde yarına aktarılmak üzere"))
    }

    // -------------------------------------------------------------------------------------
    // 3. DayTemplateUseCase Tests
    // -------------------------------------------------------------------------------------
    @Test
    fun testDayTemplate_predefinedTemplatesAndInstantiation() {
        val useCase = DayTemplateUseCase()
        val templates = useCase.getPredefinedTemplates()

        assertEquals(3, templates.size)

        // Exam Day Template
        val examDay = templates.first { it.type == DayTemplateType.EXAM_DAY }
        assertEquals("Sınav Günü (Exam Day)", examDay.template.name)
        assertTrue(examDay.blocks.isNotEmpty())

        // Deep Coding Template
        val codingDay = templates.first { it.type == DayTemplateType.DEEP_CODING }
        assertTrue(codingDay.blocks.any { it.blockType == BlockType.DEEP_WORK })

        // Weekend Rest Template
        val weekendRest = templates.first { it.type == DayTemplateType.WEEKEND_REST }
        assertTrue(weekendRest.blocks.any { it.blockType == BlockType.REST })

        // Instantiation for a specific date
        val instantiatedBlocks = useCase.instantiateTemplateBlocks(codingDay.blocks, testDate)
        assertEquals(codingDay.blocks.size, instantiatedBlocks.size)
        for (block in instantiatedBlocks) {
            assertEquals(testDate, block.date)
            assertEquals(0L, block.id)
        }
    }

    // -------------------------------------------------------------------------------------
    // 4. BrainDumpUseCase Tests
    // -------------------------------------------------------------------------------------
    @Test
    fun testBrainDump_deterministicParsingAndConversion() {
        val useCase = BrainDumpUseCase()

        // 1. Task Note Parsing
        val taskText = "- [ ] Nilian UI refactor yap !high 45dk #android #kotlin"
        val parsedTask = useCase.parseNote(taskText, testDate)
        assertEquals(BrainDumpType.TASK, parsedTask.suggestedType)
        assertEquals(Priority.HIGH, parsedTask.extractedPriority)
        assertEquals(45, parsedTask.extractedDurationMinutes)
        assertEquals(listOf("android", "kotlin"), parsedTask.tags)
        assertEquals("Nilian UI refactor yap", parsedTask.cleanTitle)

        val taskEntity = useCase.convertToTask(
            InboxNote(id = 1L, content = taskText),
            defaultDueDate = testDate
        )
        assertEquals("Nilian UI refactor yap", taskEntity.title)
        assertEquals(Priority.HIGH, taskEntity.priority)
        assertEquals(45, taskEntity.estimatedDurationMinutes)
        assertEquals(testDate, taskEntity.dueDate)

        // 2. Event Note Parsing
        val eventText = "Ekip Toplantısı saat 14:30 90dk #design"
        val parsedEvent = useCase.parseNote(eventText, testDate)
        assertEquals(BrainDumpType.EVENT, parsedEvent.suggestedType)
        assertEquals(LocalTime.of(14, 30), parsedEvent.extractedTime)
        assertEquals(90, parsedEvent.extractedDurationMinutes)

        val eventEntity = useCase.convertToEvent(
            InboxNote(id = 2L, content = eventText),
            defaultDate = testDate
        )
        assertEquals("Ekip Toplantısı", eventEntity.title)
        assertEquals(LocalDateTime.of(testDate, LocalTime.of(14, 30)), eventEntity.startDateTime)
        assertEquals(LocalDateTime.of(testDate, LocalTime.of(16, 0)), eventEntity.endDateTime)
        assertEquals(EventCategory.MEETING, eventEntity.category)

        // 3. Goal Note Parsing
        val goalText = "Yeni SaaS ürün lansmanı vizyon hedefi #saas"
        val parsedGoal = useCase.parseNote(goalText, testDate)
        assertEquals(BrainDumpType.GOAL, parsedGoal.suggestedType)

        val goalEntity = useCase.convertToGoal(
            InboxNote(id = 3L, content = goalText),
            defaultTargetDate = testDate.plusMonths(3)
        )
        assertEquals("Yeni SaaS ürün lansmanı vizyon hedefi", goalEntity.title)
        assertEquals(testDate.plusMonths(3), goalEntity.targetDate)
    }

    // -------------------------------------------------------------------------------------
    // 5. TimeBudgetCalculatorUseCase Tests
    // -------------------------------------------------------------------------------------
    @Test
    fun testTimeBudgetCalculator_balancedAndOverallocatedScenarios() {
        val useCase = TimeBudgetCalculatorUseCase()

        // 1. Balanced Day
        val task1 = Task(id = 1, title = "Task 1", estimatedDurationMinutes = 60, dueDate = testDate)
        val task2 = Task(id = 2, title = "Task 2", estimatedDurationMinutes = 60, dueDate = testDate)

        val event = Event(
            id = 10,
            title = "Morning Sync",
            startDateTime = LocalDateTime.of(testDate, LocalTime.of(9, 0)),
            endDateTime = LocalDateTime.of(testDate, LocalTime.of(10, 0))
        )

        val assessmentBalanced = useCase(
            tasks = listOf(task1, task2),
            events = listOf(event),
            timeBlocks = emptyList(),
            targetDate = testDate,
            dayStart = LocalTime.of(8, 0),
            dayEnd = LocalTime.of(14, 0)
        )

        // Free slots: 08:00-09:00 (60m) + 10:00-14:00 (240m) = 300m free slot capacity
        // Task demand: 120m
        assertEquals(120, assessmentBalanced.totalTaskMinutes)
        assertEquals(300, assessmentBalanced.freeSlotMinutes)
        assertFalse(assessmentBalanced.isOverAllocated)
        assertEquals(0, assessmentBalanced.deficitMinutes)
        assertEquals(180, assessmentBalanced.surplusMinutes)
        assertEquals(TimeBudgetStatus.SURPLUS, assessmentBalanced.status)

        // 2. Overallocated Day
        val heavyTask = Task(id = 3, title = "Heavy Refactor", estimatedDurationMinutes = 350, dueDate = testDate)
        val assessmentHeavy = useCase(
            tasks = listOf(task1, task2, heavyTask), // 60 + 60 + 350 = 470 min task demand
            events = listOf(event),
            timeBlocks = emptyList(),
            targetDate = testDate,
            dayStart = LocalTime.of(8, 0),
            dayEnd = LocalTime.of(14, 0) // 300 min free capacity
        )

        assertEquals(470, assessmentHeavy.totalTaskMinutes)
        assertEquals(300, assessmentHeavy.freeSlotMinutes)
        assertTrue(assessmentHeavy.isOverAllocated)
        assertEquals(170, assessmentHeavy.deficitMinutes)
        assertEquals(0, assessmentHeavy.surplusMinutes)
        assertEquals(TimeBudgetStatus.CRITICAL_DEFICIT, assessmentHeavy.status)
        assertTrue(assessmentHeavy.advice.contains("170 dk açık"))
    }

    // -------------------------------------------------------------------------------------
    // 6. LocalWifiSyncManager Cryptography & Frame Protocol Tests
    // -------------------------------------------------------------------------------------
    @Test
    fun testLocalWifiSyncManager_aesGcmCryptoRoundtrip() {
        val jsonUseCase = JsonBackupRestoreUseCase()
        val syncManager = LocalWifiSyncManager(jsonUseCase)

        val pin = "8492"
        val salt = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        val key = syncManager.deriveAesKey(pin, salt)

        val originalPayload = "{\"app\":\"Nilian\",\"version\":1,\"tasks\":[{\"id\":1,\"title\":\"Encrypted P2P Task\"}]}"
        val plaintextBytes = originalPayload.toByteArray(StandardCharsets.UTF_8)

        // 1. Encrypt with derived AES-256-GCM key
        val encrypted = syncManager.encryptPayload(plaintextBytes, key)
        assertNotNull(encrypted)
        assertTrue(encrypted.size > plaintextBytes.size)

        // 2. Decrypt with matching key
        val decryptedBytes = syncManager.decryptPayload(encrypted, key)
        val decryptedPayload = String(decryptedBytes, StandardCharsets.UTF_8)

        assertEquals(originalPayload, decryptedPayload)

        // 3. Decrypt with invalid key / PIN must fail
        val wrongKey = syncManager.deriveAesKey("0000", salt)
        var exceptionThrown = false
        try {
            syncManager.decryptPayload(encrypted, wrongKey)
        } catch (_: Exception) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }
}
