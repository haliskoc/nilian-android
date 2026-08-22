package com.nilian.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nilian.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "nilian_security_prefs")

/**
 * Manages local security preferences, master PIN hashing with cryptographic salt,
 * biometric lock preferences, and application visual theme settings.
 */
class SecurityPreferences(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.securityDataStore

    companion object {
        private val KEY_PIN_HASH = stringPreferencesKey("master_pin_hash")
        private val KEY_PIN_SALT = stringPreferencesKey("master_pin_salt")
        private val KEY_BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

        @Volatile
        private var instance: SecurityPreferences? = null

        fun getInstance(context: Context): SecurityPreferences {
            return instance ?: synchronized(this) {
                instance ?: SecurityPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    private val preferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    /**
     * Observes whether a Master PIN is currently configured.
     */
    val isPinSet: Flow<Boolean> = preferencesFlow.map { prefs ->
        !prefs[KEY_PIN_HASH].isNullOrEmpty() && !prefs[KEY_PIN_SALT].isNullOrEmpty()
    }

    /**
     * Observes whether biometric unlock is enabled.
     */
    val biometricsEnabled: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_BIOMETRICS_ENABLED] ?: false
    }

    /**
     * Observes current theme preference (SYSTEM, LIGHT, DARK).
     */
    val themeMode: Flow<ThemeMode> = preferencesFlow.map { prefs ->
        val raw = prefs[KEY_THEME_MODE]
        if (raw != null) {
            runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
        } else {
            ThemeMode.SYSTEM
        }
    }

    /**
     * Sets a new Master PIN by generating a unique cryptographic salt and SHA-256 hash.
     */
    suspend fun setPin(pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        dataStore.edit { prefs ->
            prefs[KEY_PIN_SALT] = salt
            prefs[KEY_PIN_HASH] = hash
        }
    }

    /**
     * Verifies the entered PIN against stored salt and hash using constant-time comparison.
     */
    suspend fun verifyPin(pin: String): Boolean {
        val prefs = preferencesFlow.first()
        val storedHash = prefs[KEY_PIN_HASH] ?: return false
        val storedSalt = prefs[KEY_PIN_SALT] ?: return false

        val calculatedHash = hashPin(pin, storedSalt)
        return MessageDigest.isEqual(
            storedHash.toByteArray(Charsets.UTF_8),
            calculatedHash.toByteArray(Charsets.UTF_8)
        )
    }

    /**
     * Changes Master PIN if old PIN matches. Returns true if successful.
     */
    suspend fun changePin(oldPin: String, newPin: String): Boolean {
        if (!verifyPin(oldPin)) return false
        setPin(newPin)
        return true
    }

    /**
     * Removes the Master PIN and salt.
     */
    suspend fun clearPin() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_PIN_HASH)
            prefs.remove(KEY_PIN_SALT)
        }
    }

    /**
     * Updates biometric authentication preference.
     */
    suspend fun setBiometricsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_BIOMETRICS_ENABLED] = enabled
        }
    }

    /**
     * Updates app theme mode.
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    /**
     * Synchronous check if PIN is set.
     */
    suspend fun checkIsPinSet(): Boolean {
        val prefs = preferencesFlow.first()
        return !prefs[KEY_PIN_HASH].isNullOrEmpty() && !prefs[KEY_PIN_SALT].isNullOrEmpty()
    }

    // --- Helper Cryptographic Utilities ---

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return bytesToHex(saltBytes)
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hashedBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt()
            result.append(hexChars[(i shr 4) and 0x0f])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
    }
}
