package com.nilian.app.domain.model

import java.time.LocalDateTime

/**
 * Peer role in a local P2P synchronization session.
 */
enum class SyncRole {
    HOST,
    CLIENT
}

/**
 * Direction of synchronization.
 */
enum class SyncMode {
    PUSH, // Send local data to remote peer
    PULL  // Fetch remote data from peer and apply locally
}

/**
 * Strategy for handling incoming synchronized data.
 */
enum class SyncStrategy {
    OVERWRITE, // Purges existing local tables and writes peer data
    MERGE      // Inserts new entities, preserving existing items
}

/**
 * Device metadata discovered or exchanged during handshake.
 */
data class SyncDeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int = 52840,
    val appVersion: String = "1.0.0",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Real-time state of the local Wi-Fi synchronization engine.
 */
sealed class SyncState {
    data object Idle : SyncState()
    data class Discovering(val foundDevices: List<SyncDeviceInfo> = emptyList()) : SyncState()
    data class Hosting(val hostIp: String, val port: Int, val pin: String) : SyncState()
    data class Connecting(val targetIp: String, val targetPort: Int) : SyncState()
    data class Authenticating(val peerDevice: SyncDeviceInfo? = null) : SyncState()
    data class Transferring(
        val progressPercent: Float = 0f,
        val bytesTransferred: Long = 0L,
        val totalBytes: Long = 0L,
        val isSending: Boolean = true
    ) : SyncState()
    data class Applying(val itemsCount: Int = 0) : SyncState()
    data class Success(val result: SyncResult) : SyncState()
    data class Failed(val errorMessage: String, val cause: Throwable? = null) : SyncState()
}

/**
 * Result summary of a completed synchronization operation.
 */
data class SyncResult(
    val success: Boolean,
    val mode: SyncMode,
    val strategy: SyncStrategy,
    val tasksCount: Int = 0,
    val eventsCount: Int = 0,
    val habitsCount: Int = 0,
    val habitLogsCount: Int = 0,
    val timeBlocksCount: Int = 0,
    val goalsCount: Int = 0,
    val peerDevice: SyncDeviceInfo? = null,
    val syncedAt: LocalDateTime = LocalDateTime.now(),
    val message: String = ""
)
