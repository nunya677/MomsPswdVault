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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PasswordEntity
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SageTeal
import com.example.util.CsvHelper
import com.example.util.CsvImportResult
import com.example.util.CsvPasswordItem

@Composable
fun ImportExportDialog(
    onDismiss: () -> Unit,
    onExportCsv: () -> String,
    onImportCsv: (String) -> CsvImportResult,
    onCopyToClipboard: (label: String, text: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Import, 1 = Export
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("import_export_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.FileDownload else Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Import & Export",
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

                // Tabs
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
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = if (selectedTab == 0) Color.White else NavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Import Passwords",
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
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = if (selectedTab == 1) Color.White else NavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export Passwords",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) Color.White else NavyDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                if (selectedTab == 0) {
                    ImportTabContent(
                        onImportCsv = onImportCsv,
                        onDismiss = onDismiss
                    )
                } else {
                    ExportTabContent(
                        onExportCsv = onExportCsv,
                        onCopyToClipboard = onCopyToClipboard,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportTabContent(
    onImportCsv: (String) -> CsvImportResult,
    onDismiss: () -> Unit
) {
    var csvInputText by remember { mutableStateOf("") }
    var previewResult by remember { mutableStateOf<CsvImportResult?>(null) }
    var importFinished by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Step-by-step instructions card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "How to Import from Chrome or Another App:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavyDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. In Google Chrome or Safari, go to Settings → Passwords → Export Passwords (CSV).\n2. Open or copy the file, and paste the text into the box below.\n3. Tap 'Preview & Import' to review and add them to your vault.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Paste CSV Text Here:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyDark
            )

            // "Load Sample" helper for demonstration and test
            OutlinedButton(
                onClick = {
                    csvInputText = CsvHelper.getSampleCsv()
                    previewResult = CsvHelper.parseCsv(csvInputText)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Fill Sample CSV", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = csvInputText,
            onValueChange = {
                csvInputText = it
                previewResult = null
                importFinished = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .testTag("csv_input_field"),
            placeholder = { Text("Title,Category,Username,Password…\nPaste your spreadsheet or exported CSV rows here") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preview & Validate Button
        Button(
            onClick = {
                if (csvInputText.isNotBlank()) {
                    previewResult = CsvHelper.parseCsv(csvInputText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("preview_csv_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = NavyDark,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "1. Preview Accounts in CSV",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyDark
            )
        }

        // Preview Section
        if (previewResult != null) {
            val result = previewResult!!
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (result.items.isNotEmpty()) Color(0xFFEFF6FF) else Color(0xFFFEF2F2))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (result.items.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (result.items.isNotEmpty()) SageTeal else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (result.items.isNotEmpty()) {
                                "Found ${result.items.size} account(s) ready to import!"
                            } else {
                                "No accounts could be read from this text."
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyDark
                        )
                    }

                    if (result.items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (item in result.items.take(3)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NavyDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = item.username.ifBlank { "(No username)" },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (result.items.size > 3) {
                                Text(
                                    text = "…and ${result.items.size - 3} more account(s)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Final Confirm Import Button
                        Button(
                            onClick = {
                                onImportCsv(csvInputText)
                                importFinished = true
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("confirm_import_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageTeal)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2. Add All ${result.items.size} Accounts to Vault",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportTabContent(
    onExportCsv: () -> String,
    onCopyToClipboard: (label: String, text: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showPreview by remember { mutableStateOf(false) }
    val exportedCsv = remember { onExportCsv() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Warning Banner for Safety
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFEF3C7))
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = GoldAmber,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Security Notice:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF78350F)
                )
                Text(
                    text = "CSV files store your passwords in readable text. Only share or save this file to a trusted private drive or delete it after use.",
                    fontSize = 13.sp,
                    color = Color(0xFF451A03)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Share/Save Button
        Button(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, exportedCsv)
                    putExtra(Intent.EXTRA_TITLE, "GoldenKey_Passwords_Export.csv")
                    type = "text/csv"
                }
                val chooser = Intent.createChooser(sendIntent, "Save or Send Passwords CSV")
                context.startActivity(chooser)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("share_csv_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Save or Share CSV File",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Copy CSV to Clipboard
        OutlinedButton(
            onClick = {
                onCopyToClipboard("Passwords CSV", exportedCsv)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("copy_csv_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy CSV Text to Clipboard", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // View CSV Preview
        OutlinedButton(
            onClick = { showPreview = !showPreview },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (showPreview) "Hide CSV Preview" else "View CSV Preview", fontSize = 14.sp)
        }

        AnimatedVisibility(visible = showPreview) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = exportedCsv,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NavyDark,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
