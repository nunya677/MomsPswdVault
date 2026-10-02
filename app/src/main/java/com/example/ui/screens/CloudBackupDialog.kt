package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.security.CloudBackupInfo
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SageTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudBackupDialog(
    lastBackupTime: Long,
    lastItemCount: Int,
    savedRecoveryKey: String?,
    onDismiss: () -> Unit,
    onCreateBackup: () -> CloudBackupInfo,
    onRestoreBackup: (backupJson: String, recoveryKey: String, secondaryCode: String?) -> Result<Int>,
    getSavedCloudBackup: () -> String?,
    onCopyToClipboard: (label: String, text: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Backup, 1 = Restore
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("cloud_backup_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Encrypted Cloud Vault",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NavyDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Backup vs Restore
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) NavyPrimary else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (selectedTab == 0) Color.White else NavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cloud Backup",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) Color.White else NavyDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) NavyPrimary else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = if (selectedTab == 1) Color.White else NavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Restore Vault",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) Color.White else NavyDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    BackupTabContent(
                        lastBackupTime = lastBackupTime,
                        lastItemCount = lastItemCount,
                        savedRecoveryKey = savedRecoveryKey,
                        onCreateBackup = onCreateBackup,
                        onCopyToClipboard = onCopyToClipboard
                    )
                } else {
                    RestoreTabContent(
                        getSavedCloudBackup = getSavedCloudBackup,
                        onRestoreBackup = onRestoreBackup,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupTabContent(
    lastBackupTime: Long,
    lastItemCount: Int,
    savedRecoveryKey: String?,
    onCreateBackup: () -> CloudBackupInfo,
    onCopyToClipboard: (label: String, text: String) -> Unit
) {
    val context = LocalContext.current
    var latestBackupInfo by remember { mutableStateOf<CloudBackupInfo?>(null) }
    val displayKey = latestBackupInfo?.recoveryKey ?: savedRecoveryKey

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (lastBackupTime > 0) Icons.Default.CloudDone else Icons.Default.Cloud,
                        contentDescription = null,
                        tint = if (lastBackupTime > 0) SageTeal else NavyPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (lastBackupTime > 0) "Cloud Backup Active" else "No Cloud Backup Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = if (lastBackupTime > 0) {
                                val dateStr = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(lastBackupTime))
                                "Last synced: $dateStr ($lastItemCount accounts)"
                            } else {
                                "Back up your vault to safely protect against losing your phone."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Create Backup Button
        Button(
            onClick = {
                latestBackupInfo = onCreateBackup()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("create_cloud_backup_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (lastBackupTime > 0) "Update Cloud Backup Now" else "Create Secure Cloud Backup",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Show Recovery Key Section if available
        if (displayKey != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                border = CardDefaults.outlinedCardBorder().copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(GoldAmber))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = GoldAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YOUR RECOVERY KEY (SAVE THIS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF78350F)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = displayKey,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDark,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    if (latestBackupInfo?.secondaryCode != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Verification Code: ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = latestBackupInfo!!.secondaryCode,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "If you ever switch phones, you will need this key to restore your passwords.",
                        fontSize = 12.sp,
                        color = Color(0xFF451A03),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onCopyToClipboard("Recovery Key", displayKey)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Key", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    val text = "GoldenKey Emergency Recovery Key:\n$displayKey\n\nKeep in a private safe place."
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Save Recovery Key"))
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAmber)
                        ) {
                            Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share / Save", fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestoreTabContent(
    getSavedCloudBackup: () -> String?,
    onRestoreBackup: (backupJson: String, recoveryKey: String, secondaryCode: String?) -> Result<Int>,
    onDismiss: () -> Unit
) {
    var recoveryKeyInput by remember { mutableStateOf("") }
    var secondaryCodeInput by remember { mutableStateOf("") }
    var customJsonInput by remember { mutableStateOf("") }
    var useManualPaste by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successCount by remember { mutableStateOf<Int?>(null) }

    val savedCloudBlob = remember { getSavedCloudBackup() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Restore from Encrypted Cloud",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavyDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter your 16-character Recovery Key to safely unlock and restore all your accounts.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Recovery Key Input
        Text(
            text = "1. Enter Recovery Key *",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = NavyDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = recoveryKeyInput,
            onValueChange = {
                recoveryKeyInput = it.uppercase()
                errorMessage = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recovery_key_input"),
            placeholder = { Text("e.g. GK-7A9B-4F2E-99C1") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Secondary Verification Code (Optional)
        Text(
            text = "2. Secondary Verification Code (Optional)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = NavyDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = secondaryCodeInput,
            onValueChange = {
                secondaryCodeInput = it.trim()
                errorMessage = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("secondary_code_input"),
            placeholder = { Text("6-digit code if generated") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Toggle manual paste if restoring from another device
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { useManualPaste = !useManualPaste }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (useManualPaste) "Using pasted backup code" else "Restoring from another device? Paste backup code",
                fontSize = 13.sp,
                color = NavyPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (useManualPaste) {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = customJsonInput,
                onValueChange = { customJsonInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                placeholder = { Text("Paste cloud backup JSON code here") },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (successCount != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SageTeal.copy(alpha = 0.15f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = SageTeal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Success! Restored $successCount accounts.",
                    fontWeight = FontWeight.Bold,
                    color = SageTeal,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Confirm Restore Button
        Button(
            onClick = {
                val jsonToUse = if (useManualPaste) customJsonInput else (savedCloudBlob ?: "")
                if (recoveryKeyInput.isBlank()) {
                    errorMessage = "Please enter your Recovery Key."
                    return@Button
                }
                if (jsonToUse.isBlank()) {
                    errorMessage = "No cloud backup found on this device. Please paste the backup code above."
                    return@Button
                }

                val result = onRestoreBackup(jsonToUse, recoveryKeyInput, secondaryCodeInput.ifBlank { null })
                if (result.isSuccess) {
                    successCount = result.getOrNull()
                    errorMessage = null
                    onDismiss()
                } else {
                    errorMessage = "Failed to restore: ${result.exceptionOrNull()?.localizedMessage ?: "Invalid Recovery Key or corrupted backup."}"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("execute_restore_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SageTeal)
        ) {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Restore All Passwords",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
