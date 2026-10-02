package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PasswordCategory
import com.example.data.PasswordEntity
import com.example.ui.ActiveDialog
import com.example.ui.VaultUiState
import com.example.ui.VaultViewModel
import com.example.ui.components.PasswordCard
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SageTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    state: VaultUiState,
    passwords: List<PasswordEntity>,
    viewModel: VaultViewModel
) {
    val context = LocalContext.current
    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary)
                                .border(2.dp, GoldAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GoldenKey",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "${passwords.size} accounts saved",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Big Text Size Toggle
                    IconButton(
                        onClick = { viewModel.toggleLargeText() },
                        modifier = Modifier.testTag("text_size_toggle")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (state.isLargeText) NavyPrimary else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.isLargeText) "A+" else "A",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isLargeText) Color.White else NavyDark
                            )
                        }
                    }

                    // Security Checkup
                    IconButton(
                        onClick = { viewModel.openDialog(ActiveDialog.SecurityCheckup) },
                        modifier = Modifier.testTag("security_checkup_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Checkup",
                            tint = SageTeal,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Password Generator
                    IconButton(
                        onClick = { viewModel.openDialog(ActiveDialog.Generator) },
                        modifier = Modifier.testTag("generator_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Password Generator",
                            tint = GoldAmber,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Cloud Backup
                    IconButton(
                        onClick = { viewModel.openDialog(ActiveDialog.CloudBackup) },
                        modifier = Modifier.testTag("cloud_backup_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Cloud Backup",
                            tint = NavyPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Import & Export
                    IconButton(
                        onClick = { viewModel.openDialog(ActiveDialog.ImportExport) },
                        modifier = Modifier.testTag("import_export_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ImportExport,
                            contentDescription = "Import & Export",
                            tint = GoldAmber,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Help Guide
                    IconButton(
                        onClick = { viewModel.openDialog(ActiveDialog.HelpGuide) },
                        modifier = Modifier.testTag("help_guide_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help Guide",
                            tint = NavyPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Settings
                    IconButton(
                        onClick = { viewModel.openDialog(ActiveDialog.Settings) },
                        modifier = Modifier.testTag("settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NavyDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Lock Now
                    IconButton(
                        onClick = { viewModel.lockVault() },
                        modifier = Modifier.testTag("lock_vault_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Vault",
                            tint = GoldAmber,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openDialog(ActiveDialog.AddEdit(null)) },
                modifier = Modifier
                    .height(60.dp)
                    .testTag("add_password_fab"),
                shape = RoundedCornerShape(18.dp),
                containerColor = NavyPrimary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Password",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Notification Banner for Copy Confirmation
            AnimatedVisibility(visible = state.bannerMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SageTeal)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.bannerMessage ?: "",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.dismissBanner() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Dismiss",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_field"),
                placeholder = {
                    Text(
                        text = "Search by account, doctor, or email…",
                        fontSize = if (state.isLargeText) 17.sp else 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Chip
                item {
                    val isAll = state.selectedCategory == null && !state.onlyFavorites
                    CategoryChip(
                        label = "All Accounts",
                        isSelected = isAll,
                        icon = null,
                        color = NavyPrimary,
                        onClick = {
                            viewModel.selectCategory(null)
                            if (state.onlyFavorites) viewModel.toggleFavoritesFilter()
                        }
                    )
                }

                // Favorites Chip
                item {
                    CategoryChip(
                        label = "Favorites ⭐",
                        isSelected = state.onlyFavorites,
                        icon = Icons.Default.Star,
                        color = GoldAmber,
                        onClick = { viewModel.toggleFavoritesFilter() }
                    )
                }

                // Individual Categories
                items(PasswordCategory.values()) { cat ->
                    val isSelected = state.selectedCategory == cat
                    CategoryChip(
                        label = cat.shortName,
                        isSelected = isSelected,
                        icon = cat.icon,
                        color = cat.color,
                        unselectedBg = cat.backgroundTint,
                        onClick = { viewModel.selectCategory(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Password Cards List
            if (passwords.isEmpty()) {
                // Friendly Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (state.searchQuery.isNotBlank() || state.selectedCategory != null) {
                                    "No matching accounts found"
                                } else {
                                    "Your vault is ready!"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (state.searchQuery.isNotBlank()) {
                                    "Try clearing the search or checking other categories."
                                } else {
                                    "Tap the '+ Add Password' button below to safely store your first login."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("passwords_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(passwords, key = { it.id }) { item ->
                        PasswordCard(
                            entry = item,
                            decryptedPassword = { viewModel.decryptPassword(item) },
                            isRevealed = state.revealedPasswordIds.contains(item.id),
                            isLargeText = state.isLargeText,
                            onToggleReveal = { viewModel.togglePasswordReveal(item.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(item.id, item.isFavorite) },
                            onCopyUsername = {
                                viewModel.copyToClipboard(context, "Username", item.username, false)
                            },
                            onCopyPassword = {
                                val plain = viewModel.decryptPassword(item)
                                viewModel.copyToClipboard(context, "Password", plain, true)
                            },
                            onEdit = { viewModel.openDialog(ActiveDialog.AddEdit(item)) },
                            onDelete = { viewModel.deletePassword(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    color: Color,
    unselectedBg: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color else unselectedBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else NavyDark
            )
        }
    }
}
