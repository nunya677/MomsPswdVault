package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LockState
import com.example.ui.VaultUiState
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SageTeal

@Composable
fun LockScreen(
    state: VaultUiState,
    onBiometricClick: () -> Unit,
    onPinSubmit: (String) -> Boolean,
    onCompleteSetup: (pin: String, enableBiometric: Boolean) -> Unit,
    onResetPin: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("lock_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Shield Emblem & Logo
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(NavyPrimary)
                    .border(3.dp, GoldAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "GoldenKey Icon",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "GoldenKey",
                style = MaterialTheme.typography.displayMedium,
                color = NavyDark,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Simple & Safe Password Vault",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (state.lockState is LockState.Setup) {
                // Setup Mode for First-Time Users
                FirstTimeSetupContent(
                    isBiometricAvailable = state.isBiometricAvailable,
                    onCompleteSetup = onCompleteSetup
                )
            } else {
                // Standard Unlock Mode
                StandardUnlockContent(
                    isBiometricAvailable = state.isBiometricAvailable && state.isBiometricEnabled,
                    onBiometricClick = onBiometricClick,
                    onPinSubmit = onPinSubmit,
                    onResetPin = onResetPin
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reassurance Badge for Older Adults
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SageTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Stored safely on this phone. No online leaks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StandardUnlockContent(
    isBiometricAvailable: Boolean,
    onBiometricClick: () -> Unit,
    onPinSubmit: (String) -> Boolean,
    onResetPin: (() -> Unit)? = null
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPinInput by remember { mutableStateOf(true) } // Default to visible keypad
    var revealUnlockPin by remember { mutableStateOf(true) } // Default to visible numbers
    var showResetDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isBiometricAvailable) {
                // Large Prominent Biometric Button
                Button(
                    onClick = onBiometricClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("biometric_unlock_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Touch Fingerprint / Face",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // PIN Header & Visibility Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter 4-Digit Master PIN:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { revealUnlockPin = !revealUnlockPin }) {
                    Icon(
                        imageVector = if (revealUnlockPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (revealUnlockPin) "Hide numbers" else "Show numbers",
                        tint = NavyPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Subtitle helper
            Text(
                text = if (revealUnlockPin) "Numbers are visible so you can read what you type" else "Numbers are masked with dots",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Extra-Large, High-Contrast PIN Digit Boxes (64dp x 76dp)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(4) { index ->
                    val hasDigit = index < enteredPin.length
                    val digitChar = if (hasDigit) enteredPin[index].toString() else ""
                    val isCurrentSlot = index == enteredPin.length

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(width = 64.dp, height = 76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    hasDigit -> Color(0xFFFEF3C7) // Soft amber fill for typed numbers
                                    isCurrentSlot -> Color(0xFFE8EEFC) // Active typing slot
                                    else -> Color.White
                                }
                            )
                            .border(
                                width = if (hasDigit || isCurrentSlot) 3.dp else 1.5.dp,
                                color = when {
                                    hasDigit -> GoldAmber
                                    isCurrentSlot -> NavyPrimary
                                    else -> Color(0xFFCBD5E1)
                                },
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasDigit) {
                            Text(
                                text = if (revealUnlockPin) digitChar else "●",
                                fontSize = if (revealUnlockPin) 38.sp else 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF0F172A)
                            )
                        } else {
                            Text(
                                text = "—",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Giant, High-Contrast Keypad
            PinKeypad(
                onDigitClick = { digit ->
                    if (enteredPin.length < 4) {
                        val newPin = enteredPin + digit
                        enteredPin = newPin
                        errorMessage = null
                        if (newPin.length == 4) {
                            val success = onPinSubmit(newPin)
                            if (!success) {
                                errorMessage = "Incorrect PIN. (Tip: Try 1234 or tap 'Reset PIN' below)"
                                enteredPin = ""
                            }
                        }
                    }
                },
                onBackspaceClick = {
                    if (enteredPin.isNotEmpty()) {
                        enteredPin = enteredPin.dropLast(1)
                        errorMessage = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Forgot PIN / Reset helper button
            TextButton(
                onClick = { showResetDialog = true }
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = NavyPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Forgot PIN or having trouble? Tap to Reset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text("Reset Master PIN", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Would you like to reset your Master PIN to 1234?\n\nYou can easily unlock and change it anytime in Settings.",
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetPin?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Reset PIN to 1234 & Unlock", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FirstTimeSetupContent(
    isBiometricAvailable: Boolean,
    onCompleteSetup: (pin: String, enableBiometric: Boolean) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var enableBiometrics by remember { mutableStateOf(isBiometricAvailable) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var revealSetupPin by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to GoldenKey!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NavyDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (step == 1) {
                    "Step 1: Choose a 4-digit Master PIN that is easy for you to remember."
                } else {
                    "Step 2: Enter your 4-digit PIN one more time to confirm."
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick default option for users who want effortless setup
            if (step == 1) {
                OutlinedButton(
                    onClick = {
                        pin1 = "1234"
                        pin2 = "1234"
                        onCompleteSetup("1234", enableBiometrics)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = GoldAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Easy PIN (1234)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            val currentPin = if (step == 1) pin1 else pin2

            // Large, High-Contrast PIN Digit Boxes
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(4) { index ->
                    val hasDigit = index < currentPin.length
                    val digitChar = if (hasDigit) currentPin[index].toString() else ""
                    val isCurrentSlot = index == currentPin.length

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(width = 64.dp, height = 76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    hasDigit -> Color(0xFFFEF3C7)
                                    isCurrentSlot -> Color(0xFFE8EEFC)
                                    else -> Color.White
                                }
                            )
                            .border(
                                width = if (hasDigit || isCurrentSlot) 3.dp else 1.5.dp,
                                color = when {
                                    hasDigit -> GoldAmber
                                    isCurrentSlot -> NavyPrimary
                                    else -> Color(0xFFCBD5E1)
                                },
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasDigit) {
                            Text(
                                text = if (revealSetupPin) digitChar else "●",
                                fontSize = if (revealSetupPin) 38.sp else 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF0F172A)
                            )
                        } else {
                            Text(
                                text = "—",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PinKeypad(
                onDigitClick = { digit ->
                    if (step == 1) {
                        if (pin1.length < 4) {
                            pin1 += digit
                            errorMessage = null
                            if (pin1.length == 4) {
                                step = 2
                            }
                        }
                    } else {
                        if (pin2.length < 4) {
                            pin2 += digit
                            errorMessage = null
                            if (pin2.length == 4) {
                                if (pin1 == pin2) {
                                    onCompleteSetup(pin1, enableBiometrics)
                                } else {
                                    errorMessage = "PINs do not match. Please choose again."
                                    pin1 = ""
                                    pin2 = ""
                                    step = 1
                                }
                            }
                        }
                    }
                },
                onBackspaceClick = {
                    if (step == 1 && pin1.isNotEmpty()) {
                        pin1 = pin1.dropLast(1)
                        errorMessage = null
                    } else if (step == 2 && pin2.isNotEmpty()) {
                        pin2 = pin2.dropLast(1)
                        errorMessage = null
                    } else if (step == 2 && pin2.isEmpty()) {
                        step = 1
                        pin1 = ""
                    }
                }
            )

            if (isBiometricAvailable) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Fingerprint Unlock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Quick, stress-free unlock anytime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enableBiometrics,
                        onCheckedChange = { enableBiometrics = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                    )
                }
            }
        }
    }
}

/**
 * High-Contrast, Giant Digit Keypad with 42sp Numbers
 * Designed specifically for effortless reading without glasses.
 */
@Composable
fun PinKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit
) {
    val keypadLayout = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        for (row in keypadLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (digit in row) {
                    when (digit) {
                        "" -> {
                            Spacer(modifier = Modifier.size(80.dp))
                        }
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(2.dp, Color(0xFF94A3B8), CircleShape)
                                    .clickable { onBackspaceClick() }
                                    .testTag("keypad_del"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete digit",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Text(
                                        text = "DELETE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                        else -> {
                            // Extra-Large, Bold, High-Contrast Digit Button
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.5.dp, Color(0xFF0F172A), CircleShape)
                                    .clickable { onDigitClick(digit) }
                                    .testTag("keypad_$digit"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
