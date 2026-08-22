package com.nilian.app.core.sync

import com.nilian.app.domain.model.SyncDeviceInfo
import com.nilian.app.domain.model.SyncMode
import com.nilian.app.domain.model.SyncResult
import com.nilian.app.domain.model.SyncRole
import com.nilian.app.domain.model.SyncState
import com.nilian.app.domain.model.SyncStrategy
import com.nilian.app.domain.usecase.JsonBackupRestoreUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Robust, zero-cloud Local P2P Wi-Fi Synchronization Engine for Nilian.
 *
 * Capabilities:
 * 1. Automatic IP & Subnet Discovery (UDP beacon discovery on local Wi-Fi).
 * 2. High-security PIN Handshake using PBKDF2WithHmacSHA256 (10,000 rounds) + AES-256-GCM.
 * 3. End-to-End Encrypted database backup transmission (Push / Pull, Overwrite / Merge).
 * 4. Observable StateFlow with real-time transfer progress and error handling.
 */
class LocalWifiSyncManager(
    private val jsonBackupRestoreUseCase: JsonBackupRestoreUseCase,
    private val deviceName: String = "Nilian Device",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val deviceId: String = UUID.randomUUID().toString()

    private var serverSocket: ServerSocket? = null
    private var activeSocket: Socket? = null
    private var beaconBroadcastJob: Job? = null
    private var discoveryJob: Job? = null
    private var hostJob: Job? = null

    // -----------------------------------------------------------------------------------------
    // 1. Network Discovery Utilities
    // -----------------------------------------------------------------------------------------

    /**
     * Resolves the active local IPv4 address on the Wi-Fi/LAN interface.
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue

                // Prefer Wi-Fi or Ethernet interfaces
                val name = intf.name.lowercase()
                val isPreferred = name.startsWith("wlan") || name.startsWith("wifi") ||
                    name.startsWith("eth") || name.startsWith("en")

                val addresses = intf.inetAddresses
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress
                        if (isPreferred && host != null) return host
                    }
                }
            }

            // Fallback to any non-loopback IPv4
            for (intf in NetworkInterface.getNetworkInterfaces()) {
                if (intf.isLoopback || !intf.isUp) continue
                for (addr in intf.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    /**
     * Returns broadcast addresses for all active network interfaces.
     */
    fun getBroadcastAddresses(): List<InetAddress> {
        val broadcastList = mutableListOf<InetAddress>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return broadcastList
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                for (interfaceAddress in intf.interfaceAddresses) {
                    val broadcast = interfaceAddress.broadcast
                    if (broadcast != null) {
                        broadcastList.add(broadcast)
                    }
                }
            }
        } catch (_: Exception) {}
        return broadcastList
    }

    /**
     * Scans the local network for active Nilian host beacons.
     */
    fun startPeerDiscovery(timeoutMs: Long = 4000L, onPeersUpdated: (List<SyncDeviceInfo>) -> Unit = {}) {
        discoveryJob?.cancel()
        discoveryJob = scope.launch {
            val discovered = mutableListOf<SyncDeviceInfo>()
            _syncState.value = SyncState.Discovering(discovered)

            var datagramSocket: DatagramSocket? = null
            try {
                datagramSocket = DatagramSocket(null).apply {
                    reuseAddress = true
                    soTimeout = 1000
                    bind(InetSocketAddress(BEACON_PORT))
                }

                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)
                val startTime = System.currentTimeMillis()

                while (isActive && (System.currentTimeMillis() - startTime < timeoutMs)) {
                    try {
                        datagramSocket.receive(packet)
                        val text = String(packet.data, 0, packet.length, StandardCharsets.UTF_8)
                        val info = parseBeaconPayload(text)
                        if (info != null && info.deviceId != deviceId && discovered.none { it.deviceId == info.deviceId }) {
                            discovered.add(info)
                            _syncState.value = SyncState.Discovering(discovered.toList())
                            onPeersUpdated(discovered.toList())
                        }
                    } catch (_: IOException) {
                        // Socket read timeout, continue loop
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    _syncState.value = SyncState.Failed("Peer discovery failed: ${e.message}", e)
                }
            } finally {
                datagramSocket?.close()
                if (_syncState.value is SyncState.Discovering) {
                    _syncState.value = SyncState.Idle
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // 2. Hosting (Server Mode)
    // -----------------------------------------------------------------------------------------

    /**
     * Starts hosting a P2P sync session on the local network with a secure PIN.
     *
     * @param pin 4-8 character PIN shared with the peer.
     * @param strategy Overwrite or merge strategy when remote data is received.
     * @param port TCP port to bind (default 52840).
     */
    fun startHosting(
        pin: String,
        strategy: SyncStrategy = SyncStrategy.MERGE,
        port: Int = DEFAULT_TCP_PORT,
        onSuccess: (SyncResult) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        stopAll()

        val localIp = getLocalIpAddress() ?: "127.0.0.1"
        _syncState.value = SyncState.Hosting(hostIp = localIp, port = port, pin = pin)

        // 1. Start UDP Discovery Beacon Broadcast
        beaconBroadcastJob = scope.launch {
            broadcastBeaconLoop(port)
        }

        // 2. Start TCP Server Socket
        hostJob = scope.launch {
            try {
                serverSocket = ServerSocket(port).apply {
                    reuseAddress = true
                }

                while (isActive) {
                    val clientSocket = serverSocket?.accept() ?: break
                    activeSocket = clientSocket

                    val result = handleIncomingClient(clientSocket, pin, strategy)
                    if (result.success) {
                        _syncState.value = SyncState.Success(result)
                        onSuccess(result)
                        break
                    } else {
                        _syncState.value = SyncState.Failed(result.message)
                        onError(result.message)
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    val msg = "Host server error: ${e.message}"
                    _syncState.value = SyncState.Failed(msg, e)
                    onError(msg)
                }
            } finally {
                stopHosting()
            }
        }
    }

    private suspend fun broadcastBeaconLoop(port: Int) = withContext(ioDispatcher) {
        var datagramSocket: DatagramSocket? = null
        try {
            datagramSocket = DatagramSocket().apply { broadcast = true }
            val payload = buildBeaconPayload(port).toByteArray(StandardCharsets.UTF_8)
            val broadcasts = getBroadcastAddresses().ifEmpty { listOf(InetAddress.getByName("255.255.255.255")) }

            while (isActive) {
                for (bcast in broadcasts) {
                    try {
                        val packet = DatagramPacket(payload, payload.size, bcast, BEACON_PORT)
                        datagramSocket.send(packet)
                    } catch (_: Exception) {}
                }
                delay(1500)
            }
        } catch (_: Exception) {
        } finally {
            datagramSocket?.close()
        }
    }

    private suspend fun handleIncomingClient(
        socket: Socket,
        pin: String,
        strategy: SyncStrategy
    ): SyncResult = withContext(ioDispatcher) {
        val dis = DataInputStream(socket.getInputStream())
        val dos = DataOutputStream(socket.getOutputStream())

        try {
            _syncState.value = SyncState.Authenticating()

            // 1. Server generates Salt and Challenge Nonce
            val salt = generateRandomBytes(16)
            val challengeNonce = generateRandomBytes(16)
            val derivedKey = deriveAesKey(pin, salt)

            // Send HANDSHAKE_CHALLENGE (Salt [16] + Nonce [16])
            dos.write(MAGIC_BYTES)
            dos.writeByte(FRAME_HANDSHAKE_CHALLENGE)
            dos.writeInt(32)
            dos.write(salt)
            dos.write(challengeNonce)
            dos.flush()

            // Read Client HANDSHAKE_RESPONSE
            val clientMagic = ByteArray(4)
            dis.readFully(clientMagic)
            if (!clientMagic.contentEquals(MAGIC_BYTES)) {
                return@withContext SyncResult(success = false, mode = SyncMode.PULL, strategy = strategy, message = "Invalid protocol magic")
            }

            val frameType = dis.readByte().toInt()
            if (frameType != FRAME_HANDSHAKE_RESPONSE) {
                return@withContext SyncResult(success = false, mode = SyncMode.PULL, strategy = strategy, message = "Expected handshake response")
            }

            val payloadLen = dis.readInt()
            val encResponse = ByteArray(payloadLen)
            dis.readFully(encResponse)

            // Decrypt and verify nonce
            val decryptedBytes = try {
                decryptPayload(encResponse, derivedKey)
            } catch (e: Exception) {
                dos.writeByte(FRAME_HANDSHAKE_FAIL)
                dos.flush()
                return@withContext SyncResult(success = false, mode = SyncMode.PULL, strategy = strategy, message = "PIN verification failed (Invalid PIN)")
            }

            if (decryptedBytes.size < 16) {
                return@withContext SyncResult(success = false, mode = SyncMode.PULL, strategy = strategy, message = "Malformed handshake challenge response")
            }

            val returnedNonce = decryptedBytes.copyOfRange(0, 16)
            if (!MessageDigest.isEqual(challengeNonce, returnedNonce)) {
                dos.writeByte(FRAME_HANDSHAKE_FAIL)
                dos.flush()
                return@withContext SyncResult(success = false, mode = SyncMode.PULL, strategy = strategy, message = "PIN handshake mismatch")
            }

            // Auth Success: send HANDSHAKE_ACK
            val hostInfoJson = "{\"deviceId\":\"$deviceId\",\"deviceName\":\"$deviceName\"}"
            val encHostInfo = encryptPayload(hostInfoJson.toByteArray(StandardCharsets.UTF_8), derivedKey)
            dos.write(MAGIC_BYTES)
            dos.writeByte(FRAME_HANDSHAKE_ACK)
            dos.writeInt(encHostInfo.size)
            dos.write(encHostInfo)
            dos.flush()

            // 2. Await Client SYNC_REQUEST
            val reqMagic = ByteArray(4)
            dis.readFully(reqMagic)
            val reqType = dis.readByte().toInt()
            if (reqType != FRAME_SYNC_REQUEST) {
                return@withContext SyncResult(success = false, mode = SyncMode.PULL, strategy = strategy, message = "Expected sync request")
            }

            val reqLen = dis.readInt()
            val encReqBytes = ByteArray(reqLen)
            dis.readFully(encReqBytes)
            val reqJson = String(decryptPayload(encReqBytes, derivedKey), StandardCharsets.UTF_8)

            val mode = if (reqJson.contains("\"mode\":\"PUSH\"")) SyncMode.PUSH else SyncMode.PULL

            // 3. Perform Sync Action
            if (mode == SyncMode.PUSH) {
                // Client is PUSHING data to Host
                _syncState.value = SyncState.Transferring(progressPercent = 0.5f, isSending = false)

                val dataMagic = ByteArray(4)
                dis.readFully(dataMagic)
                dis.readByte() // type
                val dataLen = dis.readInt()
                val encData = ByteArray(dataLen)
                dis.readFully(encData)

                _syncState.value = SyncState.Applying(itemsCount = 1)
                val jsonString = String(decryptPayload(encData, derivedKey), StandardCharsets.UTF_8)
                val restoreResult = jsonBackupRestoreUseCase.restoreBackupJson(
                    jsonString = jsonString,
                    replaceExisting = (strategy == SyncStrategy.OVERWRITE)
                )

                // Send SYNC_ACK
                val ackMsg = "Restored ${restoreResult.tasksCount} tasks, ${restoreResult.eventsCount} events, ${restoreResult.habitsCount} habits"
                val encAck = encryptPayload(ackMsg.toByteArray(StandardCharsets.UTF_8), derivedKey)
                dos.write(MAGIC_BYTES)
                dos.writeByte(FRAME_SYNC_ACK)
                dos.writeInt(encAck.size)
                dos.write(encAck)
                dos.flush()

                return@withContext SyncResult(
                    success = restoreResult.success,
                    mode = SyncMode.PUSH,
                    strategy = strategy,
                    tasksCount = restoreResult.tasksCount,
                    eventsCount = restoreResult.eventsCount,
                    habitsCount = restoreResult.habitsCount,
                    habitLogsCount = restoreResult.habitLogsCount,
                    timeBlocksCount = restoreResult.timeBlocksCount,
                    goalsCount = restoreResult.goalsCount,
                    message = ackMsg
                )
            } else {
                // Client is PULLING data from Host
                _syncState.value = SyncState.Transferring(progressPercent = 0.5f, isSending = true)

                val backupJson = jsonBackupRestoreUseCase.createBackupJson()
                val encBackup = encryptPayload(backupJson.toByteArray(StandardCharsets.UTF_8), derivedKey)

                dos.write(MAGIC_BYTES)
                dos.writeByte(FRAME_SYNC_DATA)
                dos.writeInt(encBackup.size)
                dos.write(encBackup)
                dos.flush()

                // Read SYNC_ACK
                val ackMagic = ByteArray(4)
                dis.readFully(ackMagic)
                dis.readByte()
                val ackLen = dis.readInt()
                val ackBytes = ByteArray(ackLen)
                dis.readFully(ackBytes)

                return@withContext SyncResult(
                    success = true,
                    mode = SyncMode.PULL,
                    strategy = strategy,
                    message = "Successfully served backup to peer"
                )
            }
        } catch (e: Exception) {
            SyncResult(success = false, mode = SyncMode.PULL, strategy = strategy, message = "Handshake error: ${e.message}")
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    // -----------------------------------------------------------------------------------------
    // 3. Client Mode (Initiator)
    // -----------------------------------------------------------------------------------------

    /**
     * Connects to a remote Nilian peer, performs the PIN handshake, and synchronizes data.
     *
     * @param targetIp Remote device IP address.
     * @param targetPort Remote device TCP port (default 52840).
     * @param pin Shared PIN for authentication and AES key derivation.
     * @param mode PUSH (upload local DB to peer) or PULL (download peer's DB).
     * @param strategy OVERWRITE or MERGE strategy.
     */
    suspend fun connectAndSync(
        targetIp: String,
        targetPort: Int = DEFAULT_TCP_PORT,
        pin: String,
        mode: SyncMode = SyncMode.PULL,
        strategy: SyncStrategy = SyncStrategy.MERGE
    ): SyncResult = withContext(ioDispatcher) {
        _syncState.value = SyncState.Connecting(targetIp, targetPort)
        var socket: Socket? = null

        try {
            socket = Socket().apply {
                connect(InetSocketAddress(targetIp, targetPort), 5000)
                soTimeout = 15000
            }
            activeSocket = socket

            val dis = DataInputStream(socket.getInputStream())
            val dos = DataOutputStream(socket.getOutputStream())

            _syncState.value = SyncState.Authenticating()

            // 1. Read Server HANDSHAKE_CHALLENGE
            val magic = ByteArray(4)
            dis.readFully(magic)
            if (!magic.contentEquals(MAGIC_BYTES)) {
                val fail = SyncResult(success = false, mode = mode, strategy = strategy, message = "Invalid host magic handshake")
                _syncState.value = SyncState.Failed(fail.message)
                return@withContext fail
            }

            val frameType = dis.readByte().toInt()
            if (frameType != FRAME_HANDSHAKE_CHALLENGE) {
                val fail = SyncResult(success = false, mode = mode, strategy = strategy, message = "Invalid handshake sequence")
                _syncState.value = SyncState.Failed(fail.message)
                return@withContext fail
            }

            val challengeLen = dis.readInt()
            val salt = ByteArray(16)
            val challengeNonce = ByteArray(16)
            dis.readFully(salt)
            dis.readFully(challengeNonce)

            val derivedKey = deriveAesKey(pin, salt)

            // 2. Send HANDSHAKE_RESPONSE
            val clientDeviceInfoJson = "{\"deviceId\":\"$deviceId\",\"deviceName\":\"$deviceName\"}"
            val clientInfoBytes = clientDeviceInfoJson.toByteArray(StandardCharsets.UTF_8)
            val challengePayload = ByteArray(16 + clientInfoBytes.size)
            System.arraycopy(challengeNonce, 0, challengePayload, 0, 16)
            System.arraycopy(clientInfoBytes, 0, challengePayload, 16, clientInfoBytes.size)

            val encResponse = encryptPayload(challengePayload, derivedKey)
            dos.write(MAGIC_BYTES)
            dos.writeByte(FRAME_HANDSHAKE_RESPONSE)
            dos.writeInt(encResponse.size)
            dos.write(encResponse)
            dos.flush()

            // 3. Read Server HANDSHAKE_ACK
            val ackMagic = ByteArray(4)
            dis.readFully(ackMagic)
            val ackFrame = dis.readByte().toInt()
            if (ackFrame == FRAME_HANDSHAKE_FAIL) {
                val fail = SyncResult(success = false, mode = mode, strategy = strategy, message = "PIN verification failed on host")
                _syncState.value = SyncState.Failed(fail.message)
                return@withContext fail
            }
            if (ackFrame != FRAME_HANDSHAKE_ACK) {
                val fail = SyncResult(success = false, mode = mode, strategy = strategy, message = "Handshake rejected by host")
                _syncState.value = SyncState.Failed(fail.message)
                return@withContext fail
            }

            val hostInfoLen = dis.readInt()
            val encHostInfo = ByteArray(hostInfoLen)
            dis.readFully(encHostInfo)

            // 4. Send SYNC_REQUEST
            val reqPayload = "{\"mode\":\"${mode.name}\",\"strategy\":\"${strategy.name}\"}".toByteArray(StandardCharsets.UTF_8)
            val encReq = encryptPayload(reqPayload, derivedKey)
            dos.write(MAGIC_BYTES)
            dos.writeByte(FRAME_SYNC_REQUEST)
            dos.writeInt(encReq.size)
            dos.write(encReq)
            dos.flush()

            // 5. Transfer Data
            if (mode == SyncMode.PUSH) {
                _syncState.value = SyncState.Transferring(progressPercent = 0.5f, isSending = true)
                val backupJson = jsonBackupRestoreUseCase.createBackupJson()
                val encBackup = encryptPayload(backupJson.toByteArray(StandardCharsets.UTF_8), derivedKey)

                dos.write(MAGIC_BYTES)
                dos.writeByte(FRAME_SYNC_DATA)
                dos.writeInt(encBackup.size)
                dos.write(encBackup)
                dos.flush()

                // Read Host SYNC_ACK
                val pushAckMagic = ByteArray(4)
                dis.readFully(pushAckMagic)
                dis.readByte()
                val ackMsgLen = dis.readInt()
                val encAckMsg = ByteArray(ackMsgLen)
                dis.readFully(encAckMsg)
                val hostMsg = String(decryptPayload(encAckMsg, derivedKey), StandardCharsets.UTF_8)

                val successResult = SyncResult(
                    success = true,
                    mode = SyncMode.PUSH,
                    strategy = strategy,
                    message = "Data pushed to host: $hostMsg"
                )
                _syncState.value = SyncState.Success(successResult)
                return@withContext successResult
            } else {
                // PULL Mode: Receive data from Host
                _syncState.value = SyncState.Transferring(progressPercent = 0.5f, isSending = false)

                val dataMagic = ByteArray(4)
                dis.readFully(dataMagic)
                dis.readByte() // Frame type
                val payloadSize = dis.readInt()
                val encData = ByteArray(payloadSize)
                dis.readFully(encData)

                _syncState.value = SyncState.Applying(itemsCount = 1)
                val backupJson = String(decryptPayload(encData, derivedKey), StandardCharsets.UTF_8)
                val restoreResult = jsonBackupRestoreUseCase.restoreBackupJson(
                    jsonString = backupJson,
                    replaceExisting = (strategy == SyncStrategy.OVERWRITE)
                )

                // Send SYNC_ACK to Host
                val ackMsg = "Client successfully applied backup"
                val encAck = encryptPayload(ackMsg.toByteArray(StandardCharsets.UTF_8), derivedKey)
                dos.write(MAGIC_BYTES)
                dos.writeByte(FRAME_SYNC_ACK)
                dos.writeInt(encAck.size)
                dos.write(encAck)
                dos.flush()

                val result = SyncResult(
                    success = restoreResult.success,
                    mode = SyncMode.PULL,
                    strategy = strategy,
                    tasksCount = restoreResult.tasksCount,
                    eventsCount = restoreResult.eventsCount,
                    habitsCount = restoreResult.habitsCount,
                    habitLogsCount = restoreResult.habitLogsCount,
                    timeBlocksCount = restoreResult.timeBlocksCount,
                    goalsCount = restoreResult.goalsCount,
                    message = "Restored ${restoreResult.tasksCount} tasks, ${restoreResult.eventsCount} events, ${restoreResult.habitsCount} habits"
                )
                _syncState.value = if (result.success) SyncState.Success(result) else SyncState.Failed(result.message)
                return@withContext result
            }
        } catch (e: Exception) {
            val errorResult = SyncResult(success = false, mode = mode, strategy = strategy, message = "Connection failed: ${e.message}")
            _syncState.value = SyncState.Failed(errorResult.message, e)
            return@withContext errorResult
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    // -----------------------------------------------------------------------------------------
    // 4. Lifecycle Cleanup
    // -----------------------------------------------------------------------------------------

    /**
     * Halts hosting server and broadcast beacons.
     */
    fun stopHosting() {
        beaconBroadcastJob?.cancel()
        beaconBroadcastJob = null
        hostJob?.cancel()
        hostJob = null
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        try { activeSocket?.close() } catch (_: Exception) {}
        activeSocket = null
        if (_syncState.value is SyncState.Hosting) {
            _syncState.value = SyncState.Idle
        }
    }

    /**
     * Cancels any active synchronization, hosting, or discovery operation.
     */
    fun stopAll() {
        discoveryJob?.cancel()
        discoveryJob = null
        stopHosting()
        _syncState.value = SyncState.Idle
    }

    // -----------------------------------------------------------------------------------------
    // 5. Cryptography & Frame Protocol Helpers
    // -----------------------------------------------------------------------------------------

    fun deriveAesKey(pin: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pin.toCharArray(), salt, 10000, 256)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptPayload(plaintext: ByteArray, key: SecretKeySpec): ByteArray {
        val iv = generateRandomBytes(12) // 12-byte IV standard for GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        val ciphertext = cipher.doFinal(plaintext)

        val out = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, out, 0, iv.size)
        System.arraycopy(ciphertext, 0, out, iv.size, ciphertext.size)
        return out
    }

    fun decryptPayload(ivAndCiphertext: ByteArray, key: SecretKeySpec): ByteArray {
        if (ivAndCiphertext.size < 12) {
            throw IllegalArgumentException("Invalid ciphertext payload size")
        }
        val iv = ByteArray(12)
        System.arraycopy(ivAndCiphertext, 0, iv, 0, 12)
        val ciphertext = ByteArray(ivAndCiphertext.size - 12)
        System.arraycopy(ivAndCiphertext, 12, ciphertext, 0, ciphertext.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(ciphertext)
    }

    private fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun buildBeaconPayload(port: Int): String {
        return "{\"app\":\"$BEACON_APP_ID\",\"deviceId\":\"$deviceId\",\"deviceName\":\"$deviceName\",\"port\":$port,\"timestamp\":${System.currentTimeMillis()}}"
    }

    private fun parseBeaconPayload(text: String): SyncDeviceInfo? {
        if (!text.contains("\"app\":\"$BEACON_APP_ID\"")) return null
        return try {
            val devId = extractJsonString(text, "deviceId") ?: return null
            val devName = extractJsonString(text, "deviceName") ?: "Nilian Peer"
            val port = extractJsonInt(text, "port") ?: DEFAULT_TCP_PORT
            val timestamp = extractJsonLong(text, "timestamp") ?: System.currentTimeMillis()

            SyncDeviceInfo(
                deviceId = devId,
                deviceName = devName,
                ipAddress = "",
                port = port,
                timestamp = timestamp
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"")
        return pattern.find(json)?.groupValues?.get(1)
    }

    private fun extractJsonInt(json: String, key: String): Int? {
        val pattern = Regex("\"$key\"\\s*:\\s*(\\d+)")
        return pattern.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractJsonLong(json: String, key: String): Long? {
        val pattern = Regex("\"$key\"\\s*:\\s*(\\d+)")
        return pattern.find(json)?.groupValues?.get(1)?.toLongOrNull()
    }

    companion object {
        const val DEFAULT_TCP_PORT = 52840
        const val BEACON_PORT = 52841
        const val BEACON_APP_ID = "NILIAN_SYNC"

        val MAGIC_BYTES = byteArrayOf(0x4E, 0x49, 0x4C, 0x53) // "NILS"

        const val FRAME_HANDSHAKE_CHALLENGE = 0x01
        const val FRAME_HANDSHAKE_RESPONSE = 0x02
        const val FRAME_HANDSHAKE_ACK = 0x03
        const val FRAME_HANDSHAKE_FAIL = 0x04
        const val FRAME_SYNC_REQUEST = 0x10
        const val FRAME_SYNC_DATA = 0x11
        const val FRAME_SYNC_ACK = 0x12
    }
}
