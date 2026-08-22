package com.nilian.app.domain

import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.FreeTimeSlot
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DomainEnginesTest {

    private val testDate = LocalDate.of(2026, 8, 22)

    // --- 1. DetectCollisionsUseCase ---
    @Test
    fun testDetectCollisions() {
        val useCase = DetectCollisionsUseCase()

        val event1 = Event(
            id = 1,
            title = "Math Lecture",
            startDateTime = LocalDateTime.of(testDate, LocalTime.of(10, 0)),
            endDateTime = LocalDateTime.of(testDate, LocalTime.of(11, 30)),
            category = EventCategory.LECTURE
        )

        val event2 = Event(
            id = 2,
            title = "Team Sync",
            startDateTime = LocalDateTime.of(testDate, LocalTime.of(11, 0)),
            endDateTime = LocalDateTime.of(testDate, LocalTime.of(12, 0)),
            category = EventCategory.MEETING
        )

        val block1 = TimeBlock(
            id = 10,
            title = "Deep Focus",
            blockType = BlockType.DEEP_WORK,
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(16, 0),
            date = testDate
        )

        val block2 = TimeBlock(
            id = 11,
            title = "Workout",
            blockType = BlockType.WORKOUT,
            startTime = LocalTime.of(15, 30),
            endTime = LocalTime.of(16, 30),
            date = testDate
        )

        val conflicts = useCase(
            events = listOf(event1, event2),
            timeBlocks = listOf(block1, block2),
            targetDate = testDate
        )

        assertEquals(2, conflicts.size)

        // Conflict 1: event1 vs event2 (11:00 to 11:30 = 30 mins)
        val eventConflict = conflicts.firstOrNull { it.itemA.id == 1L && it.itemB.id == 2L || it.itemA.id == 2L && it.itemB.id == 1L }
        assertNotNull(eventConflict)
        assertEquals(30L, eventConflict?.overlapDurationMinutes)

        // Conflict 2: block1 vs block2 (15:30 to 16:00 = 30 mins)
        val blockConflict = conflicts.firstOrNull { it.itemA.id == 10L && it.itemB.id == 11L || it.itemA.id == 11L && it.itemB.id == 10L }
        assertNotNull(blockConflict)
        assertEquals(30L, blockConflict?.overlapDurationMinutes)
    }

    // --- 2. HabitStreakCalculatorUseCase ---
    @Test
    fun testHabitStreakCalculation() {
        val useCase = HabitStreakCalculatorUseCase()
        val habit = Habit(
            id = 1,
            title = "Daily Reading",
            targetDaysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            createdAt = LocalDate.of(2026, 8, 1)
        )

        // 2026-08-22 is Saturday (non-target day).
        // Friday 2026-08-21, Thursday 2026-08-20, Wednesday 2026-08-19 are target days.
        val logs = listOf(
            HabitLog(habitId = 1, date = LocalDate.of(2026, 8, 21), isCompleted = true),
            HabitLog(habitId = 1, date = LocalDate.of(2026, 8, 20), isCompleted = true),
            HabitLog(habitId = 1, date = LocalDate.of(2026, 8, 19), isCompleted = true),
            // Missed Tuesday Aug 18
            HabitLog(habitId = 1, date = LocalDate.of(2026, 8, 17), isCompleted = true),
            HabitLog(habitId = 1, date = LocalDate.of(2026, 8, 14), isCompleted = true),
            HabitLog(habitId = 1, date = LocalDate.of(2026, 8, 13), isCompleted = true),
            HabitLog(habitId = 1, date = LocalDate.of(2026, 8, 12), isCompleted = true),
            HabitLog(habitId = 1, date = LocalDate.of(2026, 8, 11), isCompleted = true)
        )

        val result = useCase(habit, logs, referenceDate = testDate)
        assertEquals(3, result.currentStreak) // Wed, Thu, Fri = 3
        assertEquals(5, result.bestStreak) // Aug 11 to Aug 17 (excluding weekend) = 5
        assertFalse(result.isCompletedToday)
    }

    // --- 3. TaskRolloverUseCase ---
    @Test
    fun testTaskRollover() {
        val useCase = TaskRolloverUseCase()

        val pastDate = testDate.minusDays(2)
        val tasks = listOf(
            Task(id = 1, title = "Unfinished Past Task", isCompleted = false, autoRollover = true, dueDate = pastDate),
            Task(id = 2, title = "Completed Past Task", isCompleted = true, autoRollover = true, dueDate = pastDate),
            Task(id = 3, title = "No Rollover Past Task", isCompleted = false, autoRollover = false, dueDate = pastDate),
            Task(id = 4, title = "Today Task", isCompleted = false, autoRollover = true, dueDate = testDate)
        )

        val rolledOver = useCase.findTasksToRollover(tasks, today = testDate)
        assertEquals(1, rolledOver.size)
        assertEquals(1L, rolledOver[0].id)

        val modified = useCase.rolloverTasks(tasks, today = testDate)
        assertEquals(1, modified.size)
        assertEquals(testDate, modified[0].dueDate)
    }

    // --- 4. FreeSlotFinderUseCase ---
    @Test
    fun testFreeSlotFinder() {
        val useCase = FreeSlotFinderUseCase()

        val event = Event(
            id = 1,
            title = "Morning Meeting",
            startDateTime = LocalDateTime.of(testDate, LocalTime.of(9, 0)),
            endDateTime = LocalDateTime.of(testDate, LocalTime.of(10, 30)),
            category = EventCategory.MEETING
        )

        val block = TimeBlock(
            id = 1,
            title = "Afternoon Study",
            blockType = BlockType.STUDY,
            startTime = LocalTime.of(12, 0),
            endTime = LocalTime.of(14, 0),
            date = testDate
        )

        val slots = useCase(
            events = listOf(event),
            timeBlocks = listOf(block),
            date = testDate,
            dayStart = LocalTime.of(8, 0),
            dayEnd = LocalTime.of(16, 0),
            minDurationMinutes = 30
        )

        // Expect gaps:
        // 08:00 - 09:00 (60 mins)
        // 10:30 - 12:00 (90 mins)
        // 14:00 - 16:00 (120 mins)
        assertEquals(3, slots.size)
        assertEquals(LocalTime.of(8, 0), slots[0].startTime)
        assertEquals(LocalTime.of(9, 0), slots[0].endTime)
        assertEquals(60L, slots[0].durationMinutes)

        assertEquals(LocalTime.of(10, 30), slots[1].startTime)
        assertEquals(LocalTime.of(12, 0), slots[1].endTime)
        assertEquals(90L, slots[1].durationMinutes)

        assertEquals(LocalTime.of(14, 0), slots[2].startTime)
        assertEquals(LocalTime.of(16, 0), slots[2].endTime)
        assertEquals(120L, slots[2].durationMinutes)
    }

    // --- 5. WorkloadStressUseCase ---
    @Test
    fun testWorkloadStressAssessment() {
        val useCase = WorkloadStressUseCase()

        val tasks = listOf(
            Task(id = 1, title = "Code feature", estimatedDurationMinutes = 120, dueDate = testDate),
            Task(id = 2, title = "Write docs", estimatedDurationMinutes = 60, dueDate = testDate)
        )

        val events = listOf(
            Event(
                id = 1,
                title = "Client Call",
                startDateTime = LocalDateTime.of(testDate, LocalTime.of(10, 0)),
                endDateTime = LocalDateTime.of(testDate, LocalTime.of(12, 0)),
                category = EventCategory.MEETING
            )
        )

        val blocks = listOf(
            TimeBlock(
                id = 1,
                title = "Deep Focus Block",
                blockType = BlockType.DEEP_WORK,
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(19, 0),
                date = testDate
            )
        )

        // Tasks = 180 min (3h)
        // Events = 120 min (2h)
        // Blocks = 300 min (5h)
        // Total = 600 min (10h) -> MODERATE (<= 600)
        val assessment = useCase(tasks, events, blocks, testDate)
        assertEquals(180, assessment.taskMinutes)
        assertEquals(120, assessment.eventMinutes)
        assertEquals(300, assessment.blockMinutes)
        assertEquals(600, assessment.totalCommittedMinutes)
        assertEquals(WorkloadLevel.MODERATE, assessment.workloadLevel)
        assertFalse(assessment.isHeavyLoad)

        // Adding 30 more minutes pushes it to HEAVY (> 600)
        val heavyTasks = tasks + Task(id = 3, title = "Extra task", estimatedDurationMinutes = 30, dueDate = testDate)
        val heavyAssessment = useCase(heavyTasks, events, blocks, testDate)
        assertEquals(630, heavyAssessment.totalCommittedMinutes)
        assertEquals(WorkloadLevel.HEAVY, heavyAssessment.workloadLevel)
        assertTrue(heavyAssessment.isHeavyLoad)
    }

    // --- 6. JsonBackupRestoreUseCase ---
    @Test
    fun testJsonBackupAndRestore() {
        val useCase = JsonBackupRestoreUseCase()

        val task = Task(id = 1, title = "Backup & Restore Task", priority = Priority.HIGH, dueDate = testDate)
        val event = Event(
            id = 2,
            title = "Sync Call",
            startDateTime = LocalDateTime.of(testDate, LocalTime.of(10, 0)),
            endDateTime = LocalDateTime.of(testDate, LocalTime.of(11, 0)),
            category = EventCategory.WORK
        )
        val habit = Habit(id = 3, title = "Drink Water", targetDaysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        val habitLog = HabitLog(habitId = 3, date = testDate, isCompleted = true)
        val block = TimeBlock(id = 4, title = "Coding", blockType = BlockType.DEEP_WORK, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(12, 0), date = testDate)

        val backupData = com.nilian.app.domain.model.BackupData(
            version = 1,
            exportTimestamp = LocalDateTime.of(testDate, LocalTime.of(20, 0)),
            tasks = listOf(task),
            events = listOf(event),
            habits = listOf(habit),
            habitLogs = listOf(habitLog),
            timeBlocks = listOf(block),
            goals = emptyList()
        )

        val json = useCase.exportToJson(backupData)
        assertNotNull(json)
        assertTrue(json.contains("\"title\": \"Backup & Restore Task\""))
        assertTrue(json.contains("\"title\": \"Sync Call\""))
        assertTrue(json.contains("\"title\": \"Drink Water\""))

        val parsed = useCase.parseFromJson(json)
        assertEquals(1, parsed.tasks.size)
        assertEquals("Backup & Restore Task", parsed.tasks[0].title)
        assertEquals(Priority.HIGH, parsed.tasks[0].priority)
        assertEquals(testDate, parsed.tasks[0].dueDate)

        assertEquals(1, parsed.events.size)
        assertEquals("Sync Call", parsed.events[0].title)

        assertEquals(1, parsed.habits.size)
        assertEquals("Drink Water", parsed.habits[0].title)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), parsed.habits[0].targetDaysOfWeek)

        assertEquals(1, parsed.habitLogs.size)
        assertEquals(3L, parsed.habitLogs[0].habitId)
        assertEquals(testDate, parsed.habitLogs[0].date)

        assertEquals(1, parsed.timeBlocks.size)
        assertEquals(BlockType.DEEP_WORK, parsed.timeBlocks[0].blockType)
    }
}
