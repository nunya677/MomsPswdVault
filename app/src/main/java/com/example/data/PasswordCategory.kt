package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CategoryBank
import com.example.ui.theme.CategoryEmail
import com.example.ui.theme.CategoryGov
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryStreaming
import com.example.ui.theme.CategoryWifi
import com.example.ui.theme.PastelPillAqua
import com.example.ui.theme.PastelPillBlue
import com.example.ui.theme.PastelPillIris
import com.example.ui.theme.PastelPillLavender
import com.example.ui.theme.PastelPillMint
import com.example.ui.theme.PastelPillPeach
import com.example.ui.theme.PastelPillRose
import com.example.ui.theme.PastelPillSlate

enum class PasswordCategory(
    val displayName: String,
    val shortName: String,
    val color: Color,
    val backgroundTint: Color = color.copy(alpha = 0.15f)
) {
    BANKING("Banking & Money", "Banking", CategoryBank, PastelPillBlue),
    HEALTHCARE("Doctors & Health", "Health", CategoryHealth, PastelPillMint),
    EMAIL("Email & Messages", "Email", CategoryEmail, PastelPillPeach),
    ENTERTAINMENT("TV & Streaming", "TV / Media", CategoryStreaming, PastelPillLavender),
    SHOPPING("Shopping & Stores", "Shopping", CategoryShopping, PastelPillRose),
    UTILITIES_HOME("Wi-Fi & Home", "Wi-Fi / Home", CategoryWifi, PastelPillAqua),
    GOVERNMENT("Pension & Social Security", "Gov / Benefits", CategoryGov, PastelPillSlate),
    OTHER("Other Accounts", "Other", CategoryOther, PastelPillIris);

    val icon: ImageVector
        get() = when (this) {
            BANKING -> Icons.Default.AccountBalance
            HEALTHCARE -> Icons.Default.LocalHospital
            EMAIL -> Icons.Default.Email
            ENTERTAINMENT -> Icons.Default.Tv
            SHOPPING -> Icons.Default.ShoppingCart
            UTILITIES_HOME -> Icons.Default.Wifi
            GOVERNMENT -> Icons.Default.Policy
            OTHER -> Icons.Default.Lock
        }
}
