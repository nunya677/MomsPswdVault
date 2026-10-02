package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.security.BiometricHelper
import com.example.ui.ActiveDialog
import com.example.ui.LockState
import com.example.ui.VaultViewModel
import com.example.ui.screens.AddEditPasswordDialog
import com.example.ui.screens.CaregiverBackupDialog
import com.example.ui.screens.CloudBackupDialog
import com.example.ui.screens.HelpGuideDialog
import com.example.ui.screens.ImportExportDialog
import com.example.ui.screens.LockScreen
import com.example.ui.screens.PasswordGeneratorSheet
import com.example.ui.screens.SecurityCheckupDialog
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val passwords by viewModel.passwords.collectAsStateWithLifecycle()
                val allPasswordsRaw by viewModel.allPasswordsRaw.collectAsStateWithLifecycle()

                var pendingGeneratedPassword by remember { mutableStateOf<String?>(null) }

                fun triggerBiometrics() {
                    BiometricHelper.showBiometricPrompt(
                        activity = this@MainActivity,
                        title = "Unlock GoldenKey",
                        subtitle = "Touch fingerprint or glance at screen",
                        negativeButtonText = "Enter Master PIN",
                        onSuccess = {
                            viewModel.unlockWithBiometrics()
                        },
                        onError = { _, _ ->
                            // Biometric prompt cancelled or error, stay on PIN fallback
                        },
                        onFailed = {
                            // Single biometric mismatch
                        }
                    )
                }

                // Auto-prompt biometrics once on start if vault is locked and biometrics are configured
                LaunchedEffect(state.lockState) {
                    if (state.lockState is LockState.Locked && state.isBiometricAvailable && state.isBiometricEnabled) {
                        triggerBiometrics()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (state.lockState) {
                        is LockState.Setup, LockState.Locked -> {
                            LockScreen(
                                state = state,
                                onBiometricClick = { triggerBiometrics() },
                                onPinSubmit = { pin -> viewModel.unlockWithPin(pin) },
                                onCompleteSetup = { pin, enableBiometric ->
                                    viewModel.completeSetup(pin, enableBiometric)
                                },
                                onResetPin = {
                                    viewModel.completeSetup("1234", true)
                                    viewModel.unlockWithPin("1234")
                                }
                            )
                        }
                        is LockState.Unlocked -> {
                            BackHandler {
                                if (state.activeDialog !is ActiveDialog.None) {
                                    viewModel.closeDialog()
                                } else {
                                    viewModel.lockVault()
                                    finish()
                                }
                            }

                            VaultScreen(
                                state = state,
                                passwords = passwords,
                                viewModel = viewModel
                            )

                            // Active Dialog Routing
                            when (val dialog = state.activeDialog) {
                                is ActiveDialog.AddEdit -> {
                                    val initialPlain = remember(dialog.entry, pendingGeneratedPassword) {
                                        pendingGeneratedPassword ?: (dialog.entry?.let { viewModel.decryptPassword(it) } ?: "")
                                    }
                                    AddEditPasswordDialog(
                                        initialEntry = dialog.entry,
                                        initialPlainPassword = initialPlain,
                                        onDismiss = {
                                            pendingGeneratedPassword = null
                                            viewModel.closeDialog()
                                        },
                                        onSave = { title, category, user, plain, web, notes, sq, sa, fav ->
                                            pendingGeneratedPassword = null
                                            viewModel.savePassword(
                                                id = dialog.entry?.id ?: 0L,
                                                title = title,
                                                category = category,
                                                username = user,
                                                plainPassword = plain,
                                                websiteUrl = web,
                                                notes = notes,
                                                securityQuestion = sq,
                                                securityAnswer = sa,
                                                isFavorite = fav
                                            )
                                        },
                                        onOpenGenerator = {
                                            viewModel.openDialog(ActiveDialog.Generator)
                                        }
                                    )
                                }

                                is ActiveDialog.Generator -> {
                                    PasswordGeneratorSheet(
                                        onDismiss = { viewModel.closeDialog() },
                                        onUsePassword = { generated ->
                                            pendingGeneratedPassword = generated
                                            viewModel.openDialog(ActiveDialog.AddEdit(null))
                                        },
                                        onCopyPassword = { generated ->
                                            viewModel.copyToClipboard(
                                                this@MainActivity,
                                                "Generated Password",
                                                generated,
                                                true
                                            )
                                        }
                                    )
                                }

                                is ActiveDialog.SecurityCheckup -> {
                                    SecurityCheckupDialog(
                                        passwords = allPasswordsRaw,
                                        decryptPassword = { viewModel.decryptPassword(it) },
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }

                                is ActiveDialog.HelpGuide -> {
                                    HelpGuideDialog(onDismiss = { viewModel.closeDialog() })
                                }

                                is ActiveDialog.Settings -> {
                                    SettingsDialog(
                                        state = state,
                                        onDismiss = { viewModel.closeDialog() },
                                        onToggleLargeText = { viewModel.toggleLargeText() },
                                        onToggleBiometric = { viewModel.setBiometricEnabled(it) },
                                        onSelectAutoLock = { viewModel.setAutoLockMinutes(it) },
                                        onSaveEmergencyContact = { name, phone ->
                                            viewModel.updateEmergencyContact(name, phone)
                                        },
                                        onOpenCaregiverBackup = {
                                            viewModel.openDialog(ActiveDialog.CaregiverBackup)
                                        },
                                        onOpenImportExport = {
                                            viewModel.openDialog(ActiveDialog.ImportExport)
                                        },
                                        onOpenCloudBackup = {
                                            viewModel.openDialog(ActiveDialog.CloudBackup)
                                        }
                                    )
                                }

                                is ActiveDialog.ImportExport -> {
                                    ImportExportDialog(
                                        onDismiss = { viewModel.closeDialog() },
                                        onExportCsv = { viewModel.exportCsv() },
                                        onImportCsv = { csv -> viewModel.importFromCsv(csv) },
                                        onCopyToClipboard = { label, text ->
                                            viewModel.copyToClipboard(this@MainActivity, label, text, false)
                                        }
                                    )
                                }

                                is ActiveDialog.CloudBackup -> {
                                    val backupInfo = viewModel.getLastCloudBackupInfo()
                                    CloudBackupDialog(
                                        lastBackupTime = backupInfo.first,
                                        lastItemCount = backupInfo.second,
                                        savedRecoveryKey = backupInfo.third,
                                        onDismiss = { viewModel.closeDialog() },
                                        onCreateBackup = { viewModel.createCloudBackup() },
                                        onRestoreBackup = { json, key, sec ->
                                            viewModel.restoreCloudBackup(json, key, sec)
                                        },
                                        getSavedCloudBackup = { viewModel.getSavedCloudBackup() },
                                        onCopyToClipboard = { label, text ->
                                            viewModel.copyToClipboard(this@MainActivity, label, text, true)
                                        }
                                    )
                                }

                                is ActiveDialog.CaregiverBackup -> {
                                    CaregiverBackupDialog(
                                        passwords = allPasswordsRaw,
                                        emergencyContactName = state.emergencyContactName,
                                        emergencyContactPhone = state.emergencyContactPhone,
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }

                                is ActiveDialog.None -> {
                                    // No dialog open
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResume()
    }
}
