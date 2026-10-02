package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PasswordCategory
import com.example.data.PasswordEntity
import com.example.data.PasswordRepository
import com.example.security.BiometricHelper
import com.example.security.CloudBackupInfo
import com.example.security.CloudBackupManager
import com.example.security.CryptoManager
import com.example.security.SecurityPreferences
import com.example.util.CsvHelper
import com.example.util.CsvImportResult
import com.example.util.PasswordGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class LockState {
    object Setup : LockState()
    object Locked : LockState()
    object Unlocked : LockState()
}

sealed class ActiveDialog {
    object None : ActiveDialog()
    data class AddEdit(val entry: PasswordEntity? = null) : ActiveDialog()
    object Generator : ActiveDialog()
    object SecurityCheckup : ActiveDialog()
    object HelpGuide : ActiveDialog()
    object Settings : ActiveDialog()
    object CaregiverBackup : ActiveDialog()
    object ImportExport : ActiveDialog()
    object CloudBackup : ActiveDialog()
}

data class VaultUiState(
    val lockState: LockState = LockState.Locked,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val isLargeText: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: PasswordCategory? = null,
    val onlyFavorites: Boolean = false,
    val revealedPasswordIds: Set<Long> = emptySet(),
    val activeDialog: ActiveDialog = ActiveDialog.None,
    val bannerMessage: String? = null,
    val autoLockMinutes: Int = 5,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = ""
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val securityPrefs = SecurityPreferences(application)
    private val repository: PasswordRepository

    private val _uiState = MutableStateFlow(
        VaultUiState(
            lockState = if (!securityPrefs.isSetupCompleted()) LockState.Setup else LockState.Locked,
            isBiometricAvailable = BiometricHelper.checkBiometricStatus(application) == BiometricHelper.BiometricStatus.AVAILABLE,
            isBiometricEnabled = securityPrefs.isBiometricEnabled,
            isLargeText = securityPrefs.isLargeTextEnabled,
            autoLockMinutes = securityPrefs.autoLockMinutes,
            emergencyContactName = securityPrefs.emergencyContactName,
            emergencyContactPhone = securityPrefs.emergencyContactPhone
        )
    )
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PasswordRepository(db.passwordDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Filtered passwords flow
    val passwords: StateFlow<List<PasswordEntity>> = combine(
        repository.allPasswords,
        _uiState
    ) { all, state ->
        all.filter { item ->
            val matchesCategory = state.selectedCategory == null || item.category == state.selectedCategory.name
            val matchesFavorite = !state.onlyFavorites || item.isFavorite
            val matchesSearch = state.searchQuery.isBlank() ||
                item.title.contains(state.searchQuery, ignoreCase = true) ||
                item.username.contains(state.searchQuery, ignoreCase = true) ||
                item.notes.contains(state.searchQuery, ignoreCase = true) ||
                item.websiteUrl.contains(state.searchQuery, ignoreCase = true)
            matchesCategory && matchesFavorite && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPasswordsRaw: StateFlow<List<PasswordEntity>> = repository.allPasswords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun decryptPassword(entity: PasswordEntity): String {
        return repository.decryptPassword(entity)
    }

    fun unlockWithBiometrics() {
        securityPrefs.lastActiveTimestamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(lockState = LockState.Unlocked)
        showBanner("Welcome back! Your vault is securely unlocked.")
    }

    fun unlockWithPin(pin: String): Boolean {
        return if (securityPrefs.verifyPin(pin)) {
            securityPrefs.lastActiveTimestamp = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(lockState = LockState.Unlocked)
            showBanner("Unlocked successfully with Master PIN.")
            true
        } else {
            false
        }
    }

    fun completeSetup(pin: String, enableBiometric: Boolean) {
        securityPrefs.setMasterPin(pin)
        securityPrefs.isBiometricEnabled = enableBiometric
        securityPrefs.lastActiveTimestamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            lockState = LockState.Unlocked,
            isBiometricEnabled = enableBiometric
        )
        showBanner("Vault created! Your passwords are now safely protected.")
    }

    fun lockVault() {
        _uiState.value = _uiState.value.copy(
            lockState = LockState.Locked,
            revealedPasswordIds = emptySet(),
            activeDialog = ActiveDialog.None
        )
    }

    fun onAppResume() {
        if (_uiState.value.lockState == LockState.Unlocked) {
            if (securityPrefs.shouldLockDueToInactivity()) {
                lockVault()
            } else {
                securityPrefs.lastActiveTimestamp = System.currentTimeMillis()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCategory(category: PasswordCategory?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = if (_uiState.value.selectedCategory == category) null else category
        )
    }

    fun toggleFavoritesFilter() {
        _uiState.value = _uiState.value.copy(onlyFavorites = !_uiState.value.onlyFavorites)
    }

    fun togglePasswordReveal(id: Long) {
        val current = _uiState.value.revealedPasswordIds
        _uiState.value = _uiState.value.copy(
            revealedPasswordIds = if (current.contains(id)) current - id else current + id
        )
    }

    fun toggleLargeText() {
        val next = !_uiState.value.isLargeText
        securityPrefs.isLargeTextEnabled = next
        _uiState.value = _uiState.value.copy(isLargeText = next)
        showBanner(if (next) "Extra Large Text enabled for easy reading." else "Standard Text size restored.")
    }

    fun setAutoLockMinutes(minutes: Int) {
        securityPrefs.autoLockMinutes = minutes
        _uiState.value = _uiState.value.copy(autoLockMinutes = minutes)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityPrefs.isBiometricEnabled = enabled
        _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
    }

    fun updateEmergencyContact(name: String, phone: String) {
        securityPrefs.emergencyContactName = name.trim()
        securityPrefs.emergencyContactPhone = phone.trim()
        _uiState.value = _uiState.value.copy(
            emergencyContactName = name.trim(),
            emergencyContactPhone = phone.trim()
        )
        showBanner("Caregiver emergency contact saved.")
    }

    fun openDialog(dialog: ActiveDialog) {
        _uiState.value = _uiState.value.copy(activeDialog = dialog)
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(activeDialog = ActiveDialog.None)
    }

    fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, !currentFavorite)
        }
    }

    fun savePassword(
        id: Long = 0L,
        title: String,
        category: PasswordCategory,
        username: String,
        plainPassword: String,
        websiteUrl: String,
        notes: String,
        securityQuestion: String,
        securityAnswer: String,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            repository.savePassword(
                id = id,
                title = title,
                category = category,
                username = username,
                plainPassword = plainPassword,
                websiteUrl = websiteUrl,
                notes = notes,
                securityQuestion = securityQuestion,
                securityAnswer = securityAnswer,
                isFavorite = isFavorite
            )
            closeDialog()
            showBanner(if (id == 0L) "New password saved securely!" else "Password updated successfully!")
        }
    }

    fun deletePassword(entity: PasswordEntity) {
        viewModelScope.launch {
            repository.deletePassword(entity)
            closeDialog()
            showBanner("Entry '${entity.title}' was deleted.")
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String, isSensitive: Boolean = false) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            clip.description.extras?.putBoolean("android.content.extra.IS_SENSITIVE", true)
        }
        clipboard.setPrimaryClip(clip)

        val message = if (isSensitive) {
            "Password copied! Ready to paste. (Will clear in 45s)"
        } else {
            "$label copied to clipboard!"
        }
        showBanner(message)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        // Auto-clear sensitive clipboard after 45 seconds
        if (isSensitive) {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val currentClip = clipboard.primaryClip
                    if (currentClip != null && currentClip.itemCount > 0 && currentClip.getItemAt(0).text == text) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            clipboard.clearPrimaryClip()
                        } else {
                            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                        }
                    }
                } catch (_: Exception) {}
            }, 45_000L)
        }
    }

    fun showBanner(message: String) {
        _uiState.value = _uiState.value.copy(bannerMessage = message)
    }

    fun dismissBanner() {
        _uiState.value = _uiState.value.copy(bannerMessage = null)
    }

    fun exportCsv(): String {
        val items = allPasswordsRaw.value
        return CsvHelper.exportToCsv(items) { decryptPassword(it) }
    }

    fun importFromCsv(csvText: String): CsvImportResult {
        val result = CsvHelper.parseCsv(csvText)
        if (result.items.isNotEmpty()) {
            viewModelScope.launch {
                for (item in result.items) {
                    repository.savePassword(
                        title = item.title,
                        category = item.category,
                        username = item.username,
                        plainPassword = item.plainPassword,
                        websiteUrl = item.websiteUrl,
                        notes = item.notes,
                        securityQuestion = item.securityQuestion,
                        securityAnswer = item.securityAnswer,
                        isFavorite = false
                    )
                }
            }
            showBanner("Imported ${result.items.size} account(s) into your vault!")
        }
        return result
    }

    fun createCloudBackup(): CloudBackupInfo {
        val items = allPasswordsRaw.value
        val info = CloudBackupManager.createCloudBackup(
            getApplication(),
            items,
            { decryptPassword(it) }
        )
        showBanner("Encrypted cloud backup created! Keep your Recovery Key safe.")
        return info
    }

    fun restoreCloudBackup(backupJson: String, recoveryKey: String, secondaryCode: String?): Result<Int> {
        return try {
            val restored = CloudBackupManager.restoreCloudBackup(backupJson, recoveryKey, secondaryCode)
            viewModelScope.launch {
                for (item in restored) {
                    repository.savePassword(
                        title = item.title,
                        category = item.getCategoryEnum(),
                        username = item.username,
                        plainPassword = CryptoManager.decrypt(item.encryptedPassword),
                        websiteUrl = item.websiteUrl,
                        notes = item.notes,
                        securityQuestion = item.securityQuestion,
                        securityAnswer = item.securityAnswer,
                        isFavorite = item.isFavorite
                    )
                }
            }
            showBanner("Restored ${restored.size} accounts from encrypted cloud backup!")
            Result.success(restored.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSavedCloudBackup(): String? {
        return CloudBackupManager.getSavedCloudBackup(getApplication())
    }

    fun getLastCloudBackupInfo(): Triple<Long, Int, String?> {
        return CloudBackupManager.getLastBackupInfo(getApplication())
    }
}
