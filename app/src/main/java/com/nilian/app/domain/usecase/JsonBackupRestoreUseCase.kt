package com.nilian.app.domain.usecase

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
import com.nilian.app.domain.repository.EventRepository
import com.nilian.app.domain.repository.GoalRepository
import com.nilian.app.domain.repository.HabitRepository
import com.nilian.app.domain.repository.TaskRepository
import com.nilian.app.domain.repository.TimeBlockRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Result summary for backup restoration operations.
 */
data class BackupRestoreResult(
    val success: Boolean,
    val tasksCount: Int = 0,
    val eventsCount: Int = 0,
    val habitsCount: Int = 0,
    val habitLogsCount: Int = 0,
    val timeBlocksCount: Int = 0,
    val goalsCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * Pure Kotlin, zero-external-dependency JSON backup and restoration engine for Nilian.
 *
 * Capabilities:
 * 1. Serializes complete application state (Tasks, Events, Habits, HabitLogs, TimeBlocks, Goals)
 *    into a human-readable, indented JSON payload.
 * 2. Parses and validates backup JSON files with robust type coercion, date-time parsing, and fallback defaults.
 * 3. Provides repository-integrated export and restore functions with overwrite/merge options.
 */
class JsonBackupRestoreUseCase(
    private val taskRepository: TaskRepository? = null,
    private val eventRepository: EventRepository? = null,
    private val habitRepository: HabitRepository? = null,
    private val timeBlockRepository: TimeBlockRepository? = null,
    private val goalRepository: GoalRepository? = null
) {

    /**
     * Exports entire database content from repositories into a structured JSON string.
     */
    suspend fun createBackupJson(): String {
        val tasks = taskRepository?.getAllTasksSync() ?: emptyList()
        val events = eventRepository?.getAllEventsSync() ?: emptyList()
        val habits = habitRepository?.getAllHabitsSync() ?: emptyList()
        val habitLogs = habitRepository?.getAllHabitLogsSync() ?: emptyList()
        val timeBlocks = timeBlockRepository?.getAllTimeBlocksSync() ?: emptyList()
        val goals = goalRepository?.getAllGoalsSync() ?: emptyList()

        val backupData = BackupData(
            version = CURRENT_SCHEMA_VERSION,
            exportTimestamp = LocalDateTime.now(),
            tasks = tasks,
            events = events,
            habits = habits,
            habitLogs = habitLogs,
            timeBlocks = timeBlocks,
            goals = goals
        )

        return exportToJson(backupData)
    }

    /**
     * Restores backup JSON content into the repositories.
     *
     * @param jsonString The raw JSON backup text.
     * @param replaceExisting If true, purges existing tables before insertion; if false, merges.
     */
    suspend fun restoreBackupJson(
        jsonString: String,
        replaceExisting: Boolean = true
    ): BackupRestoreResult {
        return try {
            val backupData = parseFromJson(jsonString)

            if (replaceExisting) {
                taskRepository?.deleteAllTasks()
                eventRepository?.deleteAllEvents()
                habitRepository?.deleteAllHabitLogs()
                habitRepository?.deleteAllHabits()
                timeBlockRepository?.deleteAllTimeBlocks()
                goalRepository?.deleteAllGoals()
            }

            if (backupData.tasks.isNotEmpty()) {
                taskRepository?.insertTasks(backupData.tasks)
            }
            if (backupData.events.isNotEmpty()) {
                eventRepository?.insertEvents(backupData.events)
            }
            if (backupData.habits.isNotEmpty()) {
                habitRepository?.insertHabits(backupData.habits)
            }
            if (backupData.habitLogs.isNotEmpty()) {
                habitRepository?.insertHabitLogs(backupData.habitLogs)
            }
            if (backupData.timeBlocks.isNotEmpty()) {
                timeBlockRepository?.insertTimeBlocks(backupData.timeBlocks)
            }
            if (backupData.goals.isNotEmpty()) {
                goalRepository?.insertGoals(backupData.goals)
            }

            BackupRestoreResult(
                success = true,
                tasksCount = backupData.tasks.size,
                eventsCount = backupData.events.size,
                habitsCount = backupData.habits.size,
                habitLogsCount = backupData.habitLogs.size,
                timeBlocksCount = backupData.timeBlocks.size,
                goalsCount = backupData.goals.size
            )
        } catch (e: Exception) {
            BackupRestoreResult(
                success = false,
                errorMessage = e.message ?: "Unknown backup restoration error"
            )
        }
    }

    /**
     * Serializes a [BackupData] instance to an indented JSON string.
     */
    fun exportToJson(data: BackupData): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"app\": \"Nilian\",\n")
        sb.append("  \"version\": ${data.version},\n")
        sb.append("  \"exportTimestamp\": \"${data.exportTimestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}\",\n")

        // Tasks
        sb.append("  \"tasks\": [\n")
        data.tasks.forEachIndexed { index, task ->
            sb.append("    {\n")
            sb.append("      \"id\": ${task.id},\n")
            sb.append("      \"title\": ${escapeJson(task.title)},\n")
            sb.append("      \"description\": ${task.description?.let { escapeJson(it) } ?: "null"},\n")
            sb.append("      \"priority\": \"${task.priority.name}\",\n")
            sb.append("      \"estimatedDurationMinutes\": ${task.estimatedDurationMinutes},\n")
            sb.append("      \"dueDate\": ${task.dueDate?.let { "\"$it\"" } ?: "null"},\n")
            sb.append("      \"isCompleted\": ${task.isCompleted},\n")
            sb.append("      \"completedAt\": ${task.completedAt?.let { "\"$it\"" } ?: "null"},\n")
            sb.append("      \"goalId\": ${task.goalId ?: "null"},\n")
            sb.append("      \"autoRollover\": ${task.autoRollover}\n")
            sb.append("    }${if (index < data.tasks.size - 1) "," else ""}\n")
        }
        sb.append("  ],\n")

        // Events
        sb.append("  \"events\": [\n")
        data.events.forEachIndexed { index, event ->
            sb.append("    {\n")
            sb.append("      \"id\": ${event.id},\n")
            sb.append("      \"title\": ${escapeJson(event.title)},\n")
            sb.append("      \"locationOrLink\": ${event.locationOrLink?.let { escapeJson(it) } ?: "null"},\n")
            sb.append("      \"startDateTime\": \"${event.startDateTime}\",\n")
            sb.append("      \"endDateTime\": \"${event.endDateTime}\",\n")
            sb.append("      \"category\": \"${event.category.name}\",\n")
            sb.append("      \"colorHex\": ${event.colorHex?.let { escapeJson(it) } ?: "null"}\n")
            sb.append("    }${if (index < data.events.size - 1) "," else ""}\n")
        }
        sb.append("  ],\n")

        // Habits
        sb.append("  \"habits\": [\n")
        data.habits.forEachIndexed { index, habit ->
            val daysJson = habit.targetDaysOfWeek.joinToString(separator = ", ") { "\"${it.name}\"" }
            sb.append("    {\n")
            sb.append("      \"id\": ${habit.id},\n")
            sb.append("      \"title\": ${escapeJson(habit.title)},\n")
            sb.append("      \"goalId\": ${habit.goalId ?: "null"},\n")
            sb.append("      \"targetDaysOfWeek\": [$daysJson],\n")
            sb.append("      \"currentStreak\": ${habit.currentStreak},\n")
            sb.append("      \"bestStreak\": ${habit.bestStreak},\n")
            sb.append("      \"createdAt\": \"${habit.createdAt}\"\n")
            sb.append("    }${if (index < data.habits.size - 1) "," else ""}\n")
        }
        sb.append("  ],\n")

        // Habit Logs
        sb.append("  \"habitLogs\": [\n")
        data.habitLogs.forEachIndexed { index, log ->
            sb.append("    {\n")
            sb.append("      \"habitId\": ${log.habitId},\n")
            sb.append("      \"date\": \"${log.date}\",\n")
            sb.append("      \"isCompleted\": ${log.isCompleted}\n")
            sb.append("    }${if (index < data.habitLogs.size - 1) "," else ""}\n")
        }
        sb.append("  ],\n")

        // Time Blocks
        sb.append("  \"timeBlocks\": [\n")
        data.timeBlocks.forEachIndexed { index, block ->
            sb.append("    {\n")
            sb.append("      \"id\": ${block.id},\n")
            sb.append("      \"title\": ${escapeJson(block.title)},\n")
            sb.append("      \"blockType\": \"${block.blockType.name}\",\n")
            sb.append("      \"startTime\": \"${block.startTime}\",\n")
            sb.append("      \"endTime\": \"${block.endTime}\",\n")
            sb.append("      \"date\": \"${block.date}\",\n")
            sb.append("      \"linkedTaskId\": ${block.linkedTaskId ?: "null"}\n")
            sb.append("    }${if (index < data.timeBlocks.size - 1) "," else ""}\n")
        }
        sb.append("  ],\n")

        // Goals
        sb.append("  \"goals\": [\n")
        data.goals.forEachIndexed { index, goal ->
            sb.append("    {\n")
            sb.append("      \"id\": ${goal.id},\n")
            sb.append("      \"title\": ${escapeJson(goal.title)},\n")
            sb.append("      \"description\": ${goal.description?.let { escapeJson(it) } ?: "null"},\n")
            sb.append("      \"targetDate\": ${goal.targetDate?.let { "\"$it\"" } ?: "null"},\n")
            sb.append("      \"progressPercent\": ${goal.progressPercent},\n")
            sb.append("      \"isArchived\": ${goal.isArchived}\n")
            sb.append("    }${if (index < data.goals.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")

        sb.append("}")
        return sb.toString()
    }

    /**
     * Parses a JSON string back into a strongly-typed [BackupData] instance.
     */
    fun parseFromJson(jsonString: String): BackupData {
        val root = SimpleJsonParser.parse(jsonString) as? Map<*, *>
            ?: throw IllegalArgumentException("Root of backup JSON must be a JSON object")

        val version = (root["version"] as? Number)?.toInt() ?: 1
        val exportTimestampStr = root["exportTimestamp"] as? String
        val exportTimestamp = exportTimestampStr?.let {
            try {
                LocalDateTime.parse(it)
            } catch (_: Exception) {
                LocalDateTime.now()
            }
        } ?: LocalDateTime.now()

        // 1. Parse Tasks
        val rawTasks = root["tasks"] as? List<*> ?: emptyList<Any>()
        val tasks = rawTasks.mapNotNull { item ->
            val obj = item as? Map<*, *> ?: return@mapNotNull null
            val id = (obj["id"] as? Number)?.toLong() ?: 0L
            val title = obj["title"] as? String ?: return@mapNotNull null
            val description = obj["description"] as? String
            val priorityStr = obj["priority"] as? String ?: Priority.MEDIUM.name
            val priority = try { Priority.valueOf(priorityStr) } catch (_: Exception) { Priority.MEDIUM }
            val estimatedDuration = (obj["estimatedDurationMinutes"] as? Number)?.toInt() ?: 30
            val dueDate = (obj["dueDate"] as? String)?.let { parseLocalDate(it) }
            val isCompleted = obj["isCompleted"] as? Boolean ?: false
            val completedAt = (obj["completedAt"] as? String)?.let { parseLocalDateTime(it) }
            val goalId = (obj["goalId"] as? Number)?.toLong()
            val autoRollover = obj["autoRollover"] as? Boolean ?: true

            Task(
                id = id,
                title = title,
                description = description,
                priority = priority,
                estimatedDurationMinutes = estimatedDuration,
                dueDate = dueDate,
                isCompleted = isCompleted,
                completedAt = completedAt,
                goalId = goalId,
                autoRollover = autoRollover
            )
        }

        // 2. Parse Events
        val rawEvents = root["events"] as? List<*> ?: emptyList<Any>()
        val events = rawEvents.mapNotNull { item ->
            val obj = item as? Map<*, *> ?: return@mapNotNull null
            val id = (obj["id"] as? Number)?.toLong() ?: 0L
            val title = obj["title"] as? String ?: return@mapNotNull null
            val locationOrLink = obj["locationOrLink"] as? String
            val startDateTime = (obj["startDateTime"] as? String)?.let { parseLocalDateTime(it) }
                ?: return@mapNotNull null
            val endDateTime = (obj["endDateTime"] as? String)?.let { parseLocalDateTime(it) }
                ?: startDateTime.plusMinutes(30)
            val categoryStr = obj["category"] as? String ?: EventCategory.GENERAL.name
            val category = try { EventCategory.valueOf(categoryStr) } catch (_: Exception) { EventCategory.GENERAL }
            val colorHex = obj["colorHex"] as? String

            Event(
                id = id,
                title = title,
                locationOrLink = locationOrLink,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                category = category,
                colorHex = colorHex
            )
        }

        // 3. Parse Habits
        val rawHabits = root["habits"] as? List<*> ?: emptyList<Any>()
        val habits = rawHabits.mapNotNull { item ->
            val obj = item as? Map<*, *> ?: return@mapNotNull null
            val id = (obj["id"] as? Number)?.toLong() ?: 0L
            val title = obj["title"] as? String ?: return@mapNotNull null
            val goalId = (obj["goalId"] as? Number)?.toLong()
            val rawDays = obj["targetDaysOfWeek"] as? List<*> ?: emptyList<Any>()
            val targetDays = rawDays.mapNotNull { dayStr ->
                try {
                    DayOfWeek.valueOf(dayStr.toString().trim())
                } catch (_: Exception) {
                    null
                }
            }.toSet().ifEmpty { DayOfWeek.values().toSet() }

            val currentStreak = (obj["currentStreak"] as? Number)?.toInt() ?: 0
            val bestStreak = (obj["bestStreak"] as? Number)?.toInt() ?: 0
            val createdAt = (obj["createdAt"] as? String)?.let { parseLocalDate(it) } ?: LocalDate.now()

            Habit(
                id = id,
                title = title,
                goalId = goalId,
                targetDaysOfWeek = targetDays,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                createdAt = createdAt
            )
        }

        // 4. Parse Habit Logs
        val rawLogs = root["habitLogs"] as? List<*> ?: emptyList<Any>()
        val habitLogs = rawLogs.mapNotNull { item ->
            val obj = item as? Map<*, *> ?: return@mapNotNull null
            val habitId = (obj["habitId"] as? Number)?.toLong() ?: return@mapNotNull null
            val date = (obj["date"] as? String)?.let { parseLocalDate(it) } ?: return@mapNotNull null
            val isCompleted = obj["isCompleted"] as? Boolean ?: true

            HabitLog(
                habitId = habitId,
                date = date,
                isCompleted = isCompleted
            )
        }

        // 5. Parse TimeBlocks
        val rawBlocks = root["timeBlocks"] as? List<*> ?: emptyList<Any>()
        val timeBlocks = rawBlocks.mapNotNull { item ->
            val obj = item as? Map<*, *> ?: return@mapNotNull null
            val id = (obj["id"] as? Number)?.toLong() ?: 0L
            val title = obj["title"] as? String ?: return@mapNotNull null
            val blockTypeStr = obj["blockType"] as? String ?: BlockType.GENERAL.name
            val blockType = try { BlockType.valueOf(blockTypeStr) } catch (_: Exception) { BlockType.GENERAL }
            val startTime = (obj["startTime"] as? String)?.let { parseLocalTime(it) } ?: return@mapNotNull null
            val endTime = (obj["endTime"] as? String)?.let { parseLocalTime(it) } ?: return@mapNotNull null
            val date = (obj["date"] as? String)?.let { parseLocalDate(it) } ?: return@mapNotNull null
            val linkedTaskId = (obj["linkedTaskId"] as? Number)?.toLong()

            TimeBlock(
                id = id,
                title = title,
                blockType = blockType,
                startTime = startTime,
                endTime = endTime,
                date = date,
                linkedTaskId = linkedTaskId
            )
        }

        // 6. Parse Goals
        val rawGoals = root["goals"] as? List<*> ?: emptyList<Any>()
        val goals = rawGoals.mapNotNull { item ->
            val obj = item as? Map<*, *> ?: return@mapNotNull null
            val id = (obj["id"] as? Number)?.toLong() ?: 0L
            val title = obj["title"] as? String ?: return@mapNotNull null
            val description = obj["description"] as? String
            val targetDate = (obj["targetDate"] as? String)?.let { parseLocalDate(it) }
            val progressPercent = (obj["progressPercent"] as? Number)?.toFloat() ?: 0f
            val isArchived = obj["isArchived"] as? Boolean ?: false

            Goal(
                id = id,
                title = title,
                description = description,
                targetDate = targetDate,
                progressPercent = progressPercent,
                isArchived = isArchived
            )
        }

        return BackupData(
            version = version,
            exportTimestamp = exportTimestamp,
            tasks = tasks,
            events = events,
            habits = habits,
            habitLogs = habitLogs,
            timeBlocks = timeBlocks,
            goals = goals
        )
    }

    private fun parseLocalDate(str: String): LocalDate? {
        return try {
            LocalDate.parse(str)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseLocalDateTime(str: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(str)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseLocalTime(str: String): LocalTime? {
        return try {
            LocalTime.parse(str)
        } catch (_: Exception) {
            null
        }
    }

    private fun escapeJson(value: String): String {
        val out = StringBuilder("\"")
        for (char in value) {
            when (char) {
                '\\' -> out.append("\\\\")
                '"' -> out.append("\\\"")
                '\b' -> out.append("\\b")
                '\u000C' -> out.append("\\f")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                else -> {
                    if (char.code in 0x00..0x1F) {
                        out.append(String.format("\\u%04x", char.code))
                    } else {
                        out.append(char)
                    }
                }
            }
        }
        out.append("\"")
        return out.toString()
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }

    /**
     * Internal zero-dependency JSON parser for pure JVM / offline execution.
     */
    internal object SimpleJsonParser {

        fun parse(json: String): Any? {
            val tokenizer = Tokenizer(json)
            val result = parseValue(tokenizer)
            tokenizer.skipWhitespace()
            return result
        }

        private fun parseValue(tokenizer: Tokenizer): Any? {
            tokenizer.skipWhitespace()
            if (tokenizer.isEof()) return null

            return when (tokenizer.peek()) {
                '{' -> parseObject(tokenizer)
                '[' -> parseArray(tokenizer)
                '"' -> parseString(tokenizer)
                't', 'f' -> parseBoolean(tokenizer)
                'n' -> parseNull(tokenizer)
                else -> parseNumber(tokenizer)
            }
        }

        private fun parseObject(tokenizer: Tokenizer): Map<String, Any?> {
            tokenizer.consume('{')
            val map = mutableMapOf<String, Any?>()
            tokenizer.skipWhitespace()

            if (tokenizer.peek() == '}') {
                tokenizer.consume('}')
                return map
            }

            while (!tokenizer.isEof()) {
                tokenizer.skipWhitespace()
                val key = parseString(tokenizer)
                tokenizer.skipWhitespace()
                tokenizer.consume(':')
                val value = parseValue(tokenizer)
                map[key] = value

                tokenizer.skipWhitespace()
                val nextChar = tokenizer.peek()
                if (nextChar == ',') {
                    tokenizer.consume(',')
                    tokenizer.skipWhitespace()
                    if (tokenizer.peek() == '}') { // allow trailing comma
                        tokenizer.consume('}')
                        break
                    }
                } else if (nextChar == '}') {
                    tokenizer.consume('}')
                    break
                } else {
                    break
                }
            }

            return map
        }

        private fun parseArray(tokenizer: Tokenizer): List<Any?> {
            tokenizer.consume('[')
            val list = mutableListOf<Any?>()
            tokenizer.skipWhitespace()

            if (tokenizer.peek() == ']') {
                tokenizer.consume(']')
                return list
            }

            while (!tokenizer.isEof()) {
                tokenizer.skipWhitespace()
                val value = parseValue(tokenizer)
                list.add(value)

                tokenizer.skipWhitespace()
                val nextChar = tokenizer.peek()
                if (nextChar == ',') {
                    tokenizer.consume(',')
                    tokenizer.skipWhitespace()
                    if (tokenizer.peek() == ']') { // allow trailing comma
                        tokenizer.consume(']')
                        break
                    }
                } else if (nextChar == ']') {
                    tokenizer.consume(']')
                    break
                } else {
                    break
                }
            }

            return list
        }

        private fun parseString(tokenizer: Tokenizer): String {
            tokenizer.consume('"')
            val sb = StringBuilder()

            while (!tokenizer.isEof()) {
                val c = tokenizer.next()
                if (c == '"') {
                    return sb.toString()
                } else if (c == '\\') {
                    if (tokenizer.isEof()) break
                    when (val escape = tokenizer.next()) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            val hex = StringBuilder()
                            for (i in 0 until 4) {
                                if (!tokenizer.isEof()) hex.append(tokenizer.next())
                            }
                            val codePoint = hex.toString().toIntOrNull(16) ?: 0
                            sb.append(codePoint.toChar())
                        }
                        else -> sb.append(escape)
                    }
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun parseBoolean(tokenizer: Tokenizer): Boolean {
            val text = tokenizer.consumeWord()
            return text == "true"
        }

        private fun parseNull(tokenizer: Tokenizer): Any? {
            tokenizer.consumeWord()
            return null
        }

        private fun parseNumber(tokenizer: Tokenizer): Number {
            val word = tokenizer.consumeNumberWord()
            return when {
                word.contains('.') || word.contains('e') || word.contains('E') -> {
                    word.toDoubleOrNull() ?: 0.0
                }
                else -> {
                    word.toLongOrNull() ?: 0L
                }
            }
        }

        private class Tokenizer(private val input: String) {
            private var pos = 0

            fun isEof(): Boolean = pos >= input.length

            fun peek(): Char = if (isEof()) '\u0000' else input[pos]

            fun next(): Char {
                val c = peek()
                pos++
                return c
            }

            fun consume(expected: Char) {
                skipWhitespace()
                if (!isEof() && input[pos] == expected) {
                    pos++
                }
            }

            fun skipWhitespace() {
                while (!isEof()) {
                    val c = input[pos]
                    if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                        pos++
                    } else {
                        break
                    }
                }
            }

            fun consumeWord(): String {
                skipWhitespace()
                val start = pos
                while (!isEof() && input[pos].isLetter()) {
                    pos++
                }
                return input.substring(start, pos)
            }

            fun consumeNumberWord(): String {
                skipWhitespace()
                val start = pos
                while (!isEof()) {
                    val c = input[pos]
                    if (c.isDigit() || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                        pos++
                    } else {
                        break
                    }
                }
                return input.substring(start, pos)
            }
        }
    }
}
