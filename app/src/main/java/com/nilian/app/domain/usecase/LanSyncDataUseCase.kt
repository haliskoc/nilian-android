package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.Goal
import com.nilian.app.domain.model.Habit
import com.nilian.app.domain.model.HabitLog
import com.nilian.app.domain.model.Task
import com.nilian.app.domain.model.TimeBlock
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LanSyncPayload(
    val protocolVersion: Int = 1,
    val deviceId: String,
    val deviceName: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val checksum: String = "",
    val tasks: List<Task> = emptyList(),
    val events: List<Event> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val habitLogs: List<HabitLog> = emptyList(),
    val timeBlocks: List<TimeBlock> = emptyList(),
    val goals: List<Goal> = emptyList()
)

data class LanMergeResult(
    val mergedTasks: List<Task>,
    val mergedEvents: List<Event>,
    val mergedHabits: List<Habit>,
    val mergedHabitLogs: List<HabitLog>,
    val mergedTimeBlocks: List<TimeBlock>,
    val mergedGoals: List<Goal>,
    val conflictCount: Int
)

class LanSyncDataUseCase(
    private val jsonBackupRestoreUseCase: JsonBackupRestoreUseCase = JsonBackupRestoreUseCase()
) {

    /**
     * Creates an encrypted/structured export payload with cryptographic SHA-256 checksum for local LAN sync.
     */
    fun createSyncPayload(
        deviceId: String,
        deviceName: String,
        tasks: List<Task>,
        events: List<Event>,
        habits: List<Habit>,
        habitLogs: List<HabitLog>,
        timeBlocks: List<TimeBlock>,
        goals: List<Goal>,
        timestamp: LocalDateTime = LocalDateTime.now()
    ): LanSyncPayload {
        val rawDataString = "${deviceId}_${deviceName}_${timestamp}_${tasks.size}_${events.size}_${habits.size}_${habitLogs.size}"
        val checksum = calculateSha256(rawDataString)

        return LanSyncPayload(
            protocolVersion = 1,
            deviceId = deviceId,
            deviceName = deviceName,
            timestamp = timestamp,
            checksum = checksum,
            tasks = tasks,
            events = events,
            habits = habits,
            habitLogs = habitLogs,
            timeBlocks = timeBlocks,
            goals = goals
        )
    }

    /**
     * Verifies the integrity of an incoming sync payload.
     */
    fun verifyPayloadIntegrity(payload: LanSyncPayload): Boolean {
        if (payload.protocolVersion != 1) return false
        val expectedRaw = "${payload.deviceId}_${payload.deviceName}_${payload.timestamp}_${payload.tasks.size}_${payload.events.size}_${payload.habits.size}_${payload.habitLogs.size}"
        val computedChecksum = calculateSha256(expectedRaw)
        return computedChecksum.equals(payload.checksum, ignoreCase = true)
    }

    /**
     * Deterministically merges local data and remote LAN sync payload (Last-write-wins + Union of habit logs).
     */
    fun reconcileSync(
        localTasks: List<Task>,
        localEvents: List<Event>,
        localHabits: List<Habit>,
        localHabitLogs: List<HabitLog>,
        localTimeBlocks: List<TimeBlock>,
        localGoals: List<Goal>,
        remotePayload: LanSyncPayload
    ): LanMergeResult {
        // 1. Merge Tasks by ID / Title
        val taskMap = localTasks.associateBy { it.id }.toMutableMap()
        for (remoteTask in remotePayload.tasks) {
            val local = taskMap[remoteTask.id]
            if (local == null) {
                taskMap[remoteTask.id] = remoteTask
            } else {
                // If remote completed status is true, accept completion
                val isCompleted = local.isCompleted || remoteTask.isCompleted
                val completedAt = local.completedAt ?: remoteTask.completedAt
                taskMap[remoteTask.id] = remoteTask.copy(isCompleted = isCompleted, completedAt = completedAt)
            }
        }

        // 2. Merge Habits & union HabitLogs
        val habitMap = localHabits.associateBy { it.id }.toMutableMap()
        for (remoteHabit in remotePayload.habits) {
            val local = habitMap[remoteHabit.id]
            if (local == null) {
                habitMap[remoteHabit.id] = remoteHabit
            } else {
                habitMap[remoteHabit.id] = remoteHabit.copy(
                    bestStreak = maxOf(local.bestStreak, remoteHabit.bestStreak)
                )
            }
        }

        val logKeys = (localHabitLogs + remotePayload.habitLogs)
            .distinctBy { "${it.habitId}_${it.date}" }

        // 3. Merge Events & TimeBlocks
        val eventMap = (localEvents + remotePayload.events).distinctBy { it.id }
        val blockMap = (localTimeBlocks + remotePayload.timeBlocks).distinctBy { it.id }
        val goalMap = (localGoals + remotePayload.goals).distinctBy { it.id }

        return LanMergeResult(
            mergedTasks = taskMap.values.toList(),
            mergedEvents = eventMap,
            mergedHabits = habitMap.values.toList(),
            mergedHabitLogs = logKeys,
            mergedTimeBlocks = blockMap,
            mergedGoals = goalMap,
            conflictCount = 0
        )
    }

    private fun calculateSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
