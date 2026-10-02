package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val category: String = PasswordCategory.OTHER.name,
    val username: String = "",
    val encryptedPassword: String = "",
    val websiteUrl: String = "",
    val notes: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = "",
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getCategoryEnum(): PasswordCategory {
        return try {
            PasswordCategory.valueOf(category)
        } catch (_: Exception) {
            PasswordCategory.OTHER
        }
    }
}
