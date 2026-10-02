package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.SageTeal

object PasswordStrengthAnalyzer {

    enum class StrengthScore(
        val label: String,
        val color: Color,
        val progress: Float
    ) {
        VERY_WEAK("Very Weak", Color(0xFFE56B6F), 0.2f),
        FAIR("Fair", Color(0xFFE8985E), 0.45f),
        GOOD("Good & Safe", GoldAmber, 0.75f),
        EXCELLENT("Excellent & Bulletproof", SageTeal, 1.0f)
    }

    data class RequirementItem(
        val label: String,
        val isMet: Boolean
    )

    data class AnalysisResult(
        val score: StrengthScore,
        val numericScore: Int, // 0 - 100
        val summary: String,
        val requirements: List<RequirementItem>,
        val suggestions: List<String>
    )

    private val COMMON_PASSWORDS = setOf(
        "123456", "password", "12345678", "qwerty", "123456789", "12345", "1234",
        "111111", "1234567", "dragon", "welcome", "admin", "abc123", "password1",
        "football", "monkey", "letmein", "master", "sunshine", "princess", "iloveyou"
    )

    fun analyze(password: String): AnalysisResult {
        if (password.isBlank()) {
            return AnalysisResult(
                score = StrengthScore.VERY_WEAK,
                numericScore = 0,
                summary = "Please enter a password.",
                requirements = listOf(
                    RequirementItem("At least 8 characters", false),
                    RequirementItem("At least 12 characters (Recommended)", false),
                    RequirementItem("Capital and lowercase letters", false),
                    RequirementItem("Numbers (0–9)", false),
                    RequirementItem("Symbols (@, #, $) or word hyphens", false)
                ),
                suggestions = listOf("Choose a password with 10+ letters or use our 'Suggest Safe Password' button.")
            )
        }

        val trimmed = password.trim()
        val isCommon = COMMON_PASSWORDS.contains(trimmed.lowercase())

        val hasLength8 = trimmed.length >= 8
        val hasLength12 = trimmed.length >= 12
        val hasUpper = trimmed.any { it.isUpperCase() }
        val hasLower = trimmed.any { it.isLowerCase() }
        val hasDigits = trimmed.any { it.isDigit() }
        val hasSymbols = trimmed.any { !it.isLetterOrDigit() }
        val isPassphrase = trimmed.contains("-") && trimmed.length >= 12

        var rawScore = 0
        if (hasLength8) rawScore += 20
        if (hasLength12) rawScore += 20
        if (hasUpper && hasLower) rawScore += 20
        if (hasDigits) rawScore += 15
        if (hasSymbols) rawScore += 15
        if (isPassphrase) rawScore += 20 // Bonus for high-entropy NIST passphrases

        if (isCommon) {
            rawScore = 10
        }

        val clampedScore = rawScore.coerceIn(5, 100)

        val scoreLevel = when {
            clampedScore >= 80 -> StrengthScore.EXCELLENT
            clampedScore >= 60 -> StrengthScore.GOOD
            clampedScore >= 35 -> StrengthScore.FAIR
            else -> StrengthScore.VERY_WEAK
        }

        val suggestions = mutableListOf<String>()
        if (isCommon) {
            suggestions.add("⚠️ This is a commonly used password that hackers try first. Please change it.")
        }
        if (!hasLength8) {
            suggestions.add("Add at least ${8 - trimmed.length} more characters to reach the 8-character minimum.")
        } else if (!hasLength12) {
            suggestions.add("Aim for 12 or more characters for stronger safety against computer guessing.")
        }
        if (!hasUpper || !hasLower) {
            suggestions.add("Mix both UPPERCASE and lowercase letters.")
        }
        if (!hasDigits) {
            suggestions.add("Include at least one or two numbers (e.g. 42).")
        }
        if (!hasSymbols && !isPassphrase) {
            suggestions.add("Add a symbol like @, #, $, or use hyphens between words.")
        }

        val summary = when (scoreLevel) {
            StrengthScore.EXCELLENT -> "Excellent! This password is very difficult for hackers or automated scanners to crack."
            StrengthScore.GOOD -> "Good and safe! Suitable for most accounts."
            StrengthScore.FAIR -> "Fair. We recommend adding a few more letters, numbers, or symbols."
            StrengthScore.VERY_WEAK -> "Weak. Tap 'Suggest Easy, Safe Password' for a much stronger suggestion."
        }

        val requirements = listOf(
            RequirementItem("At least 8 characters", hasLength8),
            RequirementItem("12+ characters (High Security)", hasLength12),
            RequirementItem("Capital & lowercase letters", hasUpper && hasLower),
            RequirementItem("Numbers (0–9)", hasDigits),
            RequirementItem("Symbols or word hyphens (-)", hasSymbols || isPassphrase)
        )

        return AnalysisResult(
            score = scoreLevel,
            numericScore = clampedScore,
            summary = summary,
            requirements = requirements,
            suggestions = suggestions
        )
    }
}
