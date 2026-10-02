package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.data.PasswordCategory
import com.example.data.PasswordEntity
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class CloudBackupInfo(
    val recoveryKey: String,
    val timestamp: Long,
    val itemCount: Int,
    val backupJson: String,
    val secondaryCode: String
)

object CloudBackupManager {

    private const val PREFS_NAME = "goldenkey_cloud_backup_prefs"
    private const val KEY_LAST_BACKUP_TIME = "last_cloud_backup_time"
    private const val KEY_LAST_RECOVERY_KEY = "last_recovery_key"
    private const val KEY_LAST_ITEM_COUNT = "last_item_count"
    private const val KEY_SAVED_CLOUD_BLOB = "saved_cloud_blob"
    private const val KEY_SECONDARY_CODE = "secondary_code"

    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128

    // Friendly charset excluding ambiguous characters (0, O, 1, I)
    private const val KEY_CHARSET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

    fun generateRecoveryKey(): String {
        val random = SecureRandom()
        fun randomBlock(len: Int): String {
            val sb = StringBuilder(len)
            repeat(len) {
                sb.append(KEY_CHARSET[random.nextInt(KEY_CHARSET.length)])
            }
            return sb.toString()
        }
        return "GK-${randomBlock(4)}-${randomBlock(4)}-${randomBlock(4)}"
    }

    fun generateSecondaryCode(): String {
        val random = SecureRandom()
        val num = random.nextInt(900000) + 100000
        return num.toString()
    }

    private fun deriveKey(recoveryKey: String, salt: ByteArray): SecretKeySpec {
        val cleanKey = recoveryKey.replace("-", "").uppercase().trim()
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(cleanKey.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, "AES")
    }

    fun createCloudBackup(
        context: Context,
        passwords: List<PasswordEntity>,
        decryptPassword: (PasswordEntity) -> String,
        customRecoveryKey: String? = null
    ): CloudBackupInfo {
        val recoveryKey = customRecoveryKey ?: generateRecoveryKey()
        val secondaryCode = generateSecondaryCode()
        val timestamp = System.currentTimeMillis()

        // 1. Build plain JSON of all passwords
        val jsonArray = JSONArray()
        for (item in passwords) {
            val plain = decryptPassword(item)
            val obj = JSONObject().apply {
                put("title", item.title)
                put("category", item.category)
                put("username", item.username)
                put("password", plain)
                put("websiteUrl", item.websiteUrl)
                put("notes", item.notes)
                put("securityQuestion", item.securityQuestion)
                put("securityAnswer", item.securityAnswer)
                put("isFavorite", item.isFavorite)
            }
            jsonArray.put(obj)
        }
        val plainBytes = jsonArray.toString().toByteArray(Charsets.UTF_8)

        // 2. Encrypt with AES-GCM using derived key from Recovery Key
        val random = SecureRandom()
        val salt = ByteArray(16).apply { random.nextBytes(this) }
        val iv = ByteArray(12).apply { random.nextBytes(this) }

        val secretKey = deriveKey(recoveryKey, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(plainBytes)

        // 3. Compute checksum
        val md = MessageDigest.getInstance("SHA-256")
        val checksum = Base64.encodeToString(md.digest(ciphertext), Base64.NO_WRAP)

        // 4. Wrap into Cloud Backup Envelope
        val envelope = JSONObject().apply {
            put("version", 1)
            put("service", "GoldenKey-Encrypted-Cloud-Backup")
            put("timestamp", timestamp)
            put("itemCount", passwords.size)
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            put("checksum", checksum)
            put("secondaryHash", hashSecondary(secondaryCode))
        }

        val backupJson = envelope.toString(2)

        // Save metadata and backup in Cloud Backup Preferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_LAST_BACKUP_TIME, timestamp)
            .putString(KEY_LAST_RECOVERY_KEY, recoveryKey)
            .putInt(KEY_LAST_ITEM_COUNT, passwords.size)
            .putString(KEY_SAVED_CLOUD_BLOB, backupJson)
            .putString(KEY_SECONDARY_CODE, secondaryCode)
            .apply()

        return CloudBackupInfo(
            recoveryKey = recoveryKey,
            timestamp = timestamp,
            itemCount = passwords.size,
            backupJson = backupJson,
            secondaryCode = secondaryCode
        )
    }

    private fun hashSecondary(code: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(code.trim().toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun restoreCloudBackup(
        backupJsonString: String,
        recoveryKey: String,
        secondaryCode: String? = null
    ): List<PasswordEntity> {
        val envelope = JSONObject(backupJsonString.trim())
        val salt = Base64.decode(envelope.getString("salt"), Base64.NO_WRAP)
        val iv = Base64.decode(envelope.getString("iv"), Base64.NO_WRAP)
        val ciphertext = Base64.decode(envelope.getString("ciphertext"), Base64.NO_WRAP)

        // Optional secondary code verification if provided
        if (!secondaryCode.isNullOrBlank() && envelope.has("secondaryHash")) {
            val expectedHash = envelope.getString("secondaryHash")
            val currentHash = hashSecondary(secondaryCode)
            if (expectedHash != currentHash) {
                throw IllegalArgumentException("Secondary verification code does not match.")
            }
        }

        val secretKey = deriveKey(recoveryKey, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val decryptedBytes = cipher.doFinal(ciphertext)

        val plainJsonStr = String(decryptedBytes, Charsets.UTF_8)
        val jsonArray = JSONArray(plainJsonStr)

        val restored = mutableListOf<PasswordEntity>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val title = obj.optString("title", "Restored Account")
            val category = obj.optString("category", PasswordCategory.OTHER.name)
            val username = obj.optString("username", "")
            val rawPassword = obj.optString("password", "")
            val websiteUrl = obj.optString("websiteUrl", "")
            val notes = obj.optString("notes", "")
            val sq = obj.optString("securityQuestion", "")
            val sa = obj.optString("securityAnswer", "")
            val isFavorite = obj.optBoolean("isFavorite", false)

            // Encrypt using local AndroidKeyStore for local Room storage
            val localEncrypted = CryptoManager.encrypt(rawPassword)

            restored.add(
                PasswordEntity(
                    id = 0L,
                    title = title,
                    category = category,
                    username = username,
                    encryptedPassword = localEncrypted,
                    websiteUrl = websiteUrl,
                    notes = notes,
                    securityQuestion = sq,
                    securityAnswer = sa,
                    isFavorite = isFavorite,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return restored
    }

    fun getSavedCloudBackup(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_CLOUD_BLOB, null)
    }

    fun getLastBackupInfo(context: Context): Triple<Long, Int, String?> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val time = prefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
        val count = prefs.getInt(KEY_LAST_ITEM_COUNT, 0)
        val key = prefs.getString(KEY_LAST_RECOVERY_KEY, null)
        return Triple(time, count, key)
    }
}
