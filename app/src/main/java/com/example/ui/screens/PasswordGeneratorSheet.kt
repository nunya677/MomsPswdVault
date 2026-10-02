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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import com.example.ui.components.ReadablePasswordText
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SageTeal
import com.example.util.GeneratorConfig
import com.example.util.GeneratorMode
import com.example.util.PasswordGenerator
import com.example.util.PasswordStrengthAnalyzer

@Composable
fun PasswordGeneratorSheet(
    onDismiss: () -> Unit,
    onUsePassword: (String) -> Unit,
    onCopyPassword: (String) -> Unit
) {
    var mode by remember { mutableStateOf(GeneratorMode.CHARACTERS) }

    // Character Mode Settings
    var length by remember { mutableStateOf(14) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var excludeAmbiguous by remember { mutableStateOf(true) }

    // Passphrase Mode Settings
    var wordCount by remember { mutableStateOf(3) }
    var includeNumberInPass by remember { mutableStateOf(true) }
    var separator by remember { mutableStateOf("-") }
    var capitalizeWords by remember { mutableStateOf(true) }

    var showHistory by remember { mutableStateOf(false) }
    val history = remember { mutableStateListOf<String>() }

    var currentPassword by remember { mutableStateOf("") }

    fun refreshPassword() {
        val config = GeneratorConfig(
            mode = mode,
            length = length,
            includeUpper = includeUpper,
            includeLower = includeLower,
            includeNumbers = includeNumbers,
            includeSymbols = includeSymbols,
            excludeAmbiguous = excludeAmbiguous,
            wordCount = wordCount,
            includeNumberInPassphrase = includeNumberInPass,
            passphraseSeparator = separator,
            capitalizeWords = capitalizeWords
        )
        val generated = PasswordGenerator.generateWithConfig(config)
        currentPassword = generated
        if (history.isEmpty() || history.first() != generated) {
            history.add(0, generated)
            if (history.size > 5) history.removeAt(history.lastIndex)
        }
    }

    LaunchedEffect(
        mode,
        length,
        includeUpper,
        includeLower,
        includeNumbers,
        includeSymbols,
        excludeAmbiguous,
        wordCount,
        includeNumberInPass,
        separator,
        capitalizeWords
    ) {
        refreshPassword()
    }

    val analysis = remember(currentPassword) {
        PasswordStrengthAnalyzer.analyze(currentPassword)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("password_generator_sheet"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = GoldAmber,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Password Generator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NavyDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Selector Tabs (Large touch targets)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (mode == GeneratorMode.CHARACTERS) NavyPrimary else Color.Transparent)
                            .clickable { mode = GeneratorMode.CHARACTERS }
                            .testTag("tab_characters"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Custom Characters",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mode == GeneratorMode.CHARACTERS) Color.White else NavyDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (mode == GeneratorMode.PASSPHRASE) NavyPrimary else Color.Transparent)
                            .clickable { mode = GeneratorMode.PASSPHRASE }
                            .testTag("tab_passphrase"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Memorable Words",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mode == GeneratorMode.PASSPHRASE) Color.White else NavyDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Generated Password Display Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(GoldAmber))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "YOUR GENERATED PASSWORD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render characters with high contrast and highlighted numbers
                        ReadablePasswordText(
                            text = currentPassword,
                            fontSize = if (currentPassword.length > 20) 22.sp else 26.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Strength Meter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = analysis.score.color,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = analysis.score.label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = analysis.score.color
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Visual Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(analysis.score.progress)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(analysis.score.color)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Roll Button (Big 58dp button with dice)
                Button(
                    onClick = { refreshPassword() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("roll_dice_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = NavyDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Roll Another Password 🎲",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CONTROLS SECTION
                if (mode == GeneratorMode.CHARACTERS) {
                    // 1. Password Length Stepper & Slider
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Password Length",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "Adjust how many letters and digits",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Stepper with big buttons and high-contrast number badge
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE2E8F0))
                                            .clickable { if (length > 8) length-- }
                                            .testTag("btn_decrease_length"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Remove, "Decrease length", tint = NavyDark, modifier = Modifier.size(24.dp))
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .border(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 14.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$length",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF92400E)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(NavyPrimary)
                                            .clickable { if (length < 32) length++ }
                                            .testTag("btn_increase_length"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, "Increase length", tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Slider
                            Slider(
                                value = length.toFloat(),
                                onValueChange = { length = it.toInt() },
                                valueRange = 8f..32f,
                                steps = 23,
                                colors = SliderDefaults.colors(
                                    thumbColor = NavyPrimary,
                                    activeTrackColor = NavyPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Character Inclusion Switches (Min 56dp tall with large text)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Character Types Included:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NavyDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LargeToggleRow(
                                title = "Uppercase Letters (A, B, C…)",
                                subtitle = "Capital letters for strength",
                                checked = includeUpper,
                                onCheckedChange = {
                                    if (!it && !includeLower && !includeNumbers && !includeSymbols) return@LargeToggleRow
                                    includeUpper = it
                                },
                                testTag = "toggle_upper"
                            )

                            LargeToggleRow(
                                title = "Lowercase Letters (a, b, c…)",
                                subtitle = "Standard small letters",
                                checked = includeLower,
                                onCheckedChange = {
                                    if (!it && !includeUpper && !includeNumbers && !includeSymbols) return@LargeToggleRow
                                    includeLower = it
                                },
                                testTag = "toggle_lower"
                            )

                            LargeToggleRow(
                                title = "Numbers (0, 1, 2, 3…)",
                                subtitle = "Numerical digits",
                                checked = includeNumbers,
                                onCheckedChange = {
                                    if (!it && !includeUpper && !includeLower && !includeSymbols) return@LargeToggleRow
                                    includeNumbers = it
                                },
                                testTag = "toggle_numbers"
                            )

                            LargeToggleRow(
                                title = "Symbols (@, #, $, %, &…)",
                                subtitle = "Punctuation and special marks",
                                checked = includeSymbols,
                                onCheckedChange = {
                                    if (!it && !includeUpper && !includeLower && !includeNumbers) return@LargeToggleRow
                                    includeSymbols = it
                                },
                                testTag = "toggle_symbols"
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Senior-Friendly Ambiguity Filter
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Avoid Confusing Letters",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF78350F)
                                        )
                                        Text(
                                            text = "Removes 0 vs O and 1 vs l so they are easy to read",
                                            fontSize = 12.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                    Switch(
                                        checked = excludeAmbiguous,
                                        onCheckedChange = { excludeAmbiguous = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = GoldAmber)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // PASSPHRASE CONTROLS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Word Count Stepper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Number of Words",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "3 or 4 real words is ideal",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE2E8F0))
                                            .clickable { if (wordCount > 2) wordCount-- },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Remove, "Fewer words", tint = NavyDark, modifier = Modifier.size(24.dp))
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .border(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 14.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$wordCount",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF92400E)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(NavyPrimary)
                                            .clickable { if (wordCount < 6) wordCount++ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, "More words", tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Separator Options
                            Text(
                                text = "Word Separator:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = NavyDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val separatorOptions = listOf(
                                "-" to "Hyphen (-)",
                                "." to "Dot (.)",
                                "_" to "Underscore (_)",
                                " " to "Space ( )"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for ((sepChar, sepLabel) in separatorOptions) {
                                    val isSelected = separator == sepChar
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) NavyPrimary else Color(0xFFF1F5F9))
                                            .clickable { separator = sepChar },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sepChar,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else NavyDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LargeToggleRow(
                                title = "Add 2 Numbers at the End",
                                subtitle = "e.g. -42 (Extra security boost)",
                                checked = includeNumberInPass,
                                onCheckedChange = { includeNumberInPass = it }
                            )

                            LargeToggleRow(
                                title = "Capitalize Each Word",
                                subtitle = "e.g. Sunny-River vs sunny-river",
                                checked = capitalizeWords,
                                onCheckedChange = { capitalizeWords = it }
                            )
                        }
                    }
                }

                // Recent Rolls History Section
                if (history.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHistory = !showHistory }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showHistory) "Hide Recent Roll History" else "Accidentally rolled past one? View Previous (${history.size - 1})",
                            fontSize = 13.sp,
                            color = NavyPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(visible = showHistory) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (past in history.drop(1)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .clickable { currentPassword = past }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = past,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = NavyDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Select",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Buttons: Copy & Use
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCopyPassword(currentPassword) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("copy_generated_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onUsePassword(currentPassword) },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(56.dp)
                            .testTag("use_generated_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text(
                            text = "Use This Password",
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

@Composable
private fun LargeToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDark
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
        )
    }
}
