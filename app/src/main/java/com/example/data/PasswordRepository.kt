package com.example.data

import com.example.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PasswordRepository(private val dao: PasswordDao) {

    val allPasswords: Flow<List<PasswordEntity>> = dao.getAllPasswords()

    fun getPasswordById(id: Long): Flow<PasswordEntity?> = dao.getPasswordById(id)

    fun decryptPassword(entity: PasswordEntity): String {
        return CryptoManager.decrypt(entity.encryptedPassword)
    }

    suspend fun savePassword(
        id: Long = 0L,
        title: String,
        category: PasswordCategory,
        username: String,
        plainPassword: String,
        websiteUrl: String = "",
        notes: String = "",
        securityQuestion: String = "",
        securityAnswer: String = "",
        isFavorite: Boolean = false
    ): Long {
        val encrypted = CryptoManager.encrypt(plainPassword)
        val entity = PasswordEntity(
            id = id,
            title = title.trim(),
            category = category.name,
            username = username.trim(),
            encryptedPassword = encrypted,
            websiteUrl = websiteUrl.trim(),
            notes = notes.trim(),
            securityQuestion = securityQuestion.trim(),
            securityAnswer = securityAnswer.trim(),
            isFavorite = isFavorite,
            updatedAt = System.currentTimeMillis()
        )
        return if (id == 0L) {
            dao.insertPassword(entity)
        } else {
            dao.updatePassword(entity)
            id
        }
    }

    suspend fun deletePassword(entity: PasswordEntity) {
        dao.deletePassword(entity)
    }

    suspend fun deletePasswordById(id: Long) {
        dao.deletePasswordById(id)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.setFavorite(id, isFavorite)
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getAllPasswords().firstOrNull()
        if (existing.isNullOrEmpty()) {
            // Seed 3 friendly examples so older adults immediately grasp the layout
            savePassword(
                title = "My Main Email (Gmail)",
                category = PasswordCategory.EMAIL,
                username = "grandma.mary@gmail.com",
                plainPassword = "Sunny-Harbor-Cabin-88",
                websiteUrl = "https://mail.google.com",
                notes = "Primary email for family letters and doctor appointment reminders.",
                securityQuestion = "First pet's name",
                securityAnswer = "Barnaby",
                isFavorite = true
            )
            savePassword(
                title = "Patient Doctor Portal (MyChart)",
                category = PasswordCategory.HEALTHCARE,
                username = "mary_smith1948",
                plainPassword = "Cedar-Breeze-Meadow-47",
                websiteUrl = "https://mychart.org",
                notes = "Refill blood pressure prescription on the 1st of every month.",
                securityQuestion = "City where you met spouse",
                securityAnswer = "Savannah",
                isFavorite = true
            )
            savePassword(
                title = "Living Room Wi-Fi Router",
                category = PasswordCategory.UTILITIES_HOME,
                username = "SmithHomeNetwork_5G",
                plainPassword = "Silver-Willow-River-23",
                notes = "The little sticker on the back of the Comcast box in the hallway.",
                isFavorite = false
            )
            savePassword(
                title = "Social Security Portal",
                category = PasswordCategory.GOVERNMENT,
                username = "marysmith_ssa",
                plainPassword = "Golden-Summit-Forest-91",
                websiteUrl = "https://ssa.gov",
                notes = "Direct deposit details and annual benefits statement.",
                isFavorite = false
            )
        }
    }
}
