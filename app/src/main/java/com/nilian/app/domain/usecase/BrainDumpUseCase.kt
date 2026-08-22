package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.BrainDumpConversionResult
import com.nilian.app.domain.model.BrainDumpParsedNote
import com.nilian.app.domain.model.BrainDumpType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.InboxNote
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.repository.EventRepository
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.InboxRepository
import com.nilian.app.domain.repository.TaskRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale

/**
 * Deterministic inbox capture and brain dump transformation engine for Nilian.
 *
 * Capabilities:
 * 1. Rapidly captures raw thoughts, meeting notes, project sparks, and to-do fragments.
 * 2. Deterministically classifies text by parsing time markers, priority tags, keywords, and action verbs.
 * 3. Converts unorganized [InboxNote] entries into structured [Task], [Event], or [Goal] entities.
 * 4. Provides direct repository persistence.
 */
class BrainDumpUseCase(
    private val inboxRepository: InboxRepository? = null,
    private val taskRepository: TaskRepository? = null,
    private val eventRepository: EventRepository? = null,
    private val goalRepository: GoalRepository? = null
) {

    /**
     * Captures a raw thought into the inbox with deterministic metadata extraction and type recommendation.
     */
    suspend fun captureNote(rawText: String): InboxNote {
        val parsed = parseNote(rawText)
        val note = InboxNote(
            id = 0L,
            content = rawText.trim(),
            createdAt = LocalDateTime.now(),
            isArchived = false,
            tags = parsed.tags
        )
        val assignedId = inboxRepository?.insertNote(note) ?: 0L
        return note.copy(id = assignedId)
    }

    /**
     * Deterministic classification parser for raw text.
     */
    fun parseNote(rawText: String, defaultDate: LocalDate = LocalDate.now()): BrainDumpParsedNote {
        val cleanText = rawText.trim()
        val lower = cleanText.lowercase(Locale.ROOT)

        // 1. Extract Tags (#tag)
        val tagRegex = Regex("""#(\w+)""")
        val tags = tagRegex.findAll(cleanText).map { it.groupValues[1] }.toList()

        // 2. Extract Priority (!high, !yüksek, !acil, !medium, !orta, !low, !düşük)
        val extractedPriority = when {
            lower.contains("!high") || lower.contains("!yüksek") || lower.contains("!acil") || lower.contains("!urgent") -> Priority.HIGH
            lower.contains("!low") || lower.contains("!düşük") -> Priority.LOW
            lower.contains("!medium") || lower.contains("!orta") -> Priority.MEDIUM
            else -> null
        }

        // 3. Extract Duration (e.g. 45dk, 30m, 120min, 2 saat, 1.5h)
        val durationRegexMin = Regex("""(\d+)\s*(dk|min|dakika|m)""")
        val durationRegexHour = Regex("""(\d+(?:\.\d+)?)\s*(saat|sa|h|hour)""")

        val extractedDurationMinutes = durationRegexMin.find(lower)?.let { match ->
            match.groupValues[1].toIntOrNull()
        } ?: durationRegexHour.find(lower)?.let { match ->
            val hours = match.groupValues[1].toDoubleOrNull() ?: 1.0
            (hours * 60).toInt()
        }

        // 4. Extract Time (e.g. 14:30, 09.00, @15:00)
        val timeRegex = Regex("""(?:\bat\s+|@|\bsaat\s+)?(\d{1,2})[:.](\d{2})""")
        val extractedTime = timeRegex.find(lower)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull()
            val min = match.groupValues[2].toIntOrNull()
            if (hour != null && min != null && hour in 0..23 && min in 0..59) {
                LocalTime.of(hour, min)
            } else null
        }

        // 5. Extract Due Date keywords (bugün, yarın, tomorrow, haftaya, etc.)
        val extractedDueDate = when {
            lower.contains("bugün") || lower.contains("today") -> defaultDate
            lower.contains("yarın") || lower.contains("tomorrow") -> defaultDate.plusDays(1)
            lower.contains("haftaya") || lower.contains("next week") -> defaultDate.plusWeeks(1)
            else -> null
        }

        // 6. Deterministic Type Classification
        val isEventKeyword = lower.contains("toplantı") || lower.contains("meeting") ||
            lower.contains("ders") || lower.contains("lecture") ||
            lower.contains("randevu") || lower.contains("webinar") ||
            lower.contains("seminer") || lower.contains("zoom") ||
            lower.contains("google meet") || extractedTime != null

        val isGoalKeyword = lower.contains("hedef") || lower.contains("goal") ||
            lower.contains("vizyon") || lower.contains("proje:") ||
            lower.contains("master") || lower.contains("yıl sonuna") ||
            lower.contains("ay sonuna") || lower.contains("release") ||
            lower.contains("launch")

        val isTaskKeyword = lower.contains("yap") || lower.contains("ara") ||
            lower.contains("yaz") || lower.contains("kodla") ||
            lower.contains("gönder") || lower.contains("hazırla") ||
            lower.contains("fix") || lower.contains("review") ||
            lower.contains("ödev") || lower.contains("tamamla") ||
            lower.startsWith("- [ ]") || lower.startsWith("* ") ||
            extractedPriority != null || extractedDurationMinutes != null

        val suggestedType = when {
            isEventKeyword -> BrainDumpType.EVENT
            isGoalKeyword -> BrainDumpType.GOAL
            isTaskKeyword -> BrainDumpType.TASK
            else -> BrainDumpType.TASK // Default actionable entity
        }

        val cleanTitle = sanitizeTitle(cleanText)

        return BrainDumpParsedNote(
            rawText = cleanText,
            cleanTitle = cleanTitle,
            suggestedType = suggestedType,
            extractedDueDate = extractedDueDate,
            extractedTime = extractedTime,
            extractedDurationMinutes = extractedDurationMinutes,
            extractedPriority = extractedPriority,
            tags = tags
        )
    }

    /**
     * Cleans markup tags and flags from text for entity title presentation.
     */
    fun sanitizeTitle(content: String): String {
        return content
            .replace(Regex("""![a-zA-ZçğıöşüÇĞİÖŞÜ]+"""), "") // remove priority flags
            .replace(Regex("""#\w+"""), "") // remove hashtag tokens
            .replace(Regex("""^-\s*\[\s*\]\s*"""), "") // remove checklist prefix
            .replace(Regex("""^\*\s*"""), "") // remove bullet prefix
            .replace(Regex("""\s+"""), " ") // normalize spacing
            .trim()
            .ifEmpty { "Yeni Öğe" }
    }

    /**
     * Converts an [InboxNote] into a strongly-typed [Task].
     */
    fun convertToTask(
        note: InboxNote,
        defaultDueDate: LocalDate = LocalDate.now()
    ): Task {
        val parsed = parseNote(note.content, defaultDueDate)
        val priority = parsed.extractedPriority ?: Priority.MEDIUM
        val duration = parsed.extractedDurationMinutes ?: 30
        val dueDate = parsed.extractedDueDate ?: defaultDueDate

        return Task(
            id = 0L,
            title = parsed.cleanTitle,
            description = if (note.tags.isNotEmpty()) "Etiketler: ${note.tags.joinToString()}" else null,
            priority = priority,
            estimatedDurationMinutes = duration,
            dueDate = dueDate,
            isCompleted = false,
            autoRollover = true
        )
    }

    /**
     * Converts an [InboxNote] into a strongly-typed [Event].
     */
    fun convertToEvent(
        note: InboxNote,
        defaultDate: LocalDate = LocalDate.now(),
        defaultDurationMinutes: Long = 60L
    ): Event {
        val parsed = parseNote(note.content, defaultDate)
        val date = parsed.extractedDueDate ?: defaultDate
        val startTime = parsed.extractedTime ?: LocalTime.of(10, 0)
        val startDateTime = LocalDateTime.of(date, startTime)
        val durationMinutes = parsed.extractedDurationMinutes?.toLong() ?: defaultDurationMinutes
        val endDateTime = startDateTime.plusMinutes(durationMinutes)

        val category = when {
            note.content.contains("ders", ignoreCase = true) || note.content.contains("lecture", ignoreCase = true) -> EventCategory.LECTURE
            note.content.contains("toplantı", ignoreCase = true) || note.content.contains("meeting", ignoreCase = true) -> EventCategory.MEETING
            note.content.contains("çalışma", ignoreCase = true) || note.content.contains("study", ignoreCase = true) -> EventCategory.STUDY
            else -> EventCategory.GENERAL
        }

        return Event(
            id = 0L,
            title = parsed.cleanTitle,
            locationOrLink = null,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            category = category,
            colorHex = null
        )
    }

    /**
     * Converts an [InboxNote] into a strongly-typed [Goal].
     */
    fun convertToGoal(
        note: InboxNote,
        defaultTargetDate: LocalDate? = null
    ): Goal {
        val parsed = parseNote(note.content)
        val targetDate = parsed.extractedDueDate ?: defaultTargetDate ?: LocalDate.now().plusMonths(1)

        return Goal(
            id = 0L,
            title = parsed.cleanTitle,
            description = if (note.tags.isNotEmpty()) "Etiketler: ${note.tags.joinToString()}" else null,
            targetDate = targetDate,
            progressPercent = 0f,
            isArchived = false
        )
    }

    /**
     * Converts the note based on its recommended or chosen type.
     */
    fun convertNote(
        note: InboxNote,
        targetType: BrainDumpType? = null,
        referenceDate: LocalDate = LocalDate.now()
    ): BrainDumpConversionResult {
        val effectiveType = targetType ?: parseNote(note.content, referenceDate).suggestedType

        return when (effectiveType) {
            BrainDumpType.TASK, BrainDumpType.NOTE -> {
                val task = convertToTask(note, referenceDate)
                BrainDumpConversionResult(note = note.copy(isArchived = true), convertedType = BrainDumpType.TASK, task = task)
            }
            BrainDumpType.EVENT -> {
                val event = convertToEvent(note, referenceDate)
                BrainDumpConversionResult(note = note.copy(isArchived = true), convertedType = BrainDumpType.EVENT, event = event)
            }
            BrainDumpType.GOAL -> {
                val goal = convertToGoal(note, referenceDate.plusMonths(1))
                BrainDumpConversionResult(note = note.copy(isArchived = true), convertedType = BrainDumpType.GOAL, goal = goal)
            }
        }
    }

    /**
     * Converts note and persists directly to respective Room repository, archiving original note.
     */
    suspend fun convertAndPersist(
        note: InboxNote,
        targetType: BrainDumpType? = null,
        referenceDate: LocalDate = LocalDate.now()
    ): BrainDumpConversionResult {
        val result = convertNote(note, targetType, referenceDate)

        when (result.convertedType) {
            BrainDumpType.TASK -> {
                result.task?.let { task ->
                    val id = taskRepository?.insertTask(task) ?: 0L
                    result.task.copy(id = id)
                }
            }
            BrainDumpType.EVENT -> {
                result.event?.let { event ->
                    val id = eventRepository?.insertEvent(event) ?: 0L
                    result.event.copy(id = id)
                }
            }
            BrainDumpType.GOAL -> {
                result.goal?.let { goal ->
                    val id = goalRepository?.insertGoal(goal) ?: 0L
                    result.goal.copy(id = id)
                }
            }
            else -> {}
        }

        // Archive note in inbox repository
        if (note.id > 0) {
            inboxRepository?.archiveNote(note.id, true)
        }

        return result
    }
}
