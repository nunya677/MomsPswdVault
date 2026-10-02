package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.UUID

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("golden_key_sec_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_SETUP_COMPLETED = "setup_completed"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_TEXT_SIZE_LARGE = "text_size_large"
        private const val KEY_AUTO_LOCK_MINUTES = "auto_lock_minutes"
        private const val KEY_EMERGENCY_NAME = "emergency_name"
        private const val KEY_EMERGENCY_PHONE = "emergency_phone"
        private const val KEY_LAST_ACTIVE_TIME = "last_active_time"
    }

    private fun hashWithSalt(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:$pin".toByteArray(Charsets.UTF_8)
        val digest = md.digest(combined)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun isSetupCompleted(): Boolean {
        return prefs.getBoolean(KEY_SETUP_COMPLETED, false)
    }

    fun setMasterPin(pin: String) {
        val salt = UUID.randomUUID().toString()
        val hash = hashWithSalt(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_SETUP_COMPLETED, true)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val currentHash = hashWithSalt(pin, salt)
        return savedHash == currentHash
    }

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var isLargeTextEnabled: Boolean
        get() = prefs.getBoolean(KEY_TEXT_SIZE_LARGE, false)
        set(value) = prefs.edit().putBoolean(KEY_TEXT_SIZE_LARGE, value).apply()

    var autoLockMinutes: Int
        get() = prefs.getInt(KEY_AUTO_LOCK_MINUTES, 5) // default 5 minutes
        set(value) = prefs.edit().putInt(KEY_AUTO_LOCK_MINUTES, value).apply()

    var emergencyContactName: String
        get() = prefs.getString(KEY_EMERGENCY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EMERGENCY_NAME, value).apply()

    var emergencyContactPhone: String
        get() = prefs.getString(KEY_EMERGENCY_PHONE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EMERGENCY_PHONE, value).apply()

    var lastActiveTimestamp: Long
        get() = prefs.getLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis())
        set(value) = prefs.edit().putLong(KEY_LAST_ACTIVE_TIME, value).apply()

    fun shouldLockDueToInactivity(): Boolean {
        val minutes = autoLockMinutes
        if (minutes == 0) return true // Immediate
        val elapsedMillis = System.currentTimeMillis() - lastActiveTimestamp
        val timeoutMillis = minutes * 60 * 1000L
        return elapsedMillis > timeoutMillis
    }
}
