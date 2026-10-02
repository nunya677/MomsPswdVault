package com.example.util

import kotlin.random.Random

enum class GeneratorMode(val title: String) {
    CHARACTERS("Custom Characters"),
    PASSPHRASE("Memorable Words")
}

data class GeneratorConfig(
    val mode: GeneratorMode = GeneratorMode.CHARACTERS,
    val length: Int = 14,
    val includeUpper: Boolean = true,
    val includeLower: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = true,
    val wordCount: Int = 3,
    val includeNumberInPassphrase: Boolean = true,
    val passphraseSeparator: String = "-",
    val capitalizeWords: Boolean = true
)

object PasswordGenerator {

    private val FRIENDLY_WORDS = listOf(
        "Apple", "Badge", "Baker", "Beach", "Berry", "Birch", "Breeze", "Bridge",
        "Cabin", "Canyon", "Cedar", "Clover", "Copper", "Coral", "Cottage", "Crown",
        "Daisy", "Dolphin", "Eagle", "Falcon", "Forest", "Garden", "Golden", "Grove",
        "Harbor", "Haven", "Hazel", "Honey", "Island", "Jasper", "Lakeside", "Lantern",
        "Maple", "Meadow", "Morning", "Noble", "Ocean", "Orchard", "Palace", "Patio",
        "Pebble", "Pine", "Prairie", "River", "Robin", "Sailboat", "Sandy", "Silver",
        "Sparrow", "Spring", "Star", "Stream", "Summit", "Sunny", "Sunset", "Timber",
        "Topaz", "Trail", "Valley", "Willow", "Windmill", "Winter"
    )

    // Standard character sets
    private const val ALL_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val ALL_LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val ALL_NUMBERS = "0123456789"
    private const val ALL_SYMBOLS = "@#$%&*+!?="

    // Non-ambiguous sets (excluding 0, O, o, 1, l, I, |)
    private const val CLEAN_UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val CLEAN_LOWER = "abcdefghjkmnpqrstuvwxyz"
    private const val CLEAN_NUMBERS = "23456789"
    private const val CLEAN_SYMBOLS = "@#$%&*+!"

    fun generateWithConfig(config: GeneratorConfig): String {
        return if (config.mode == GeneratorMode.PASSPHRASE) {
            generatePassphrase(
                wordCount = config.wordCount,
                includeNumber = config.includeNumberInPassphrase,
                separator = config.passphraseSeparator,
                capitalize = config.capitalizeWords
            )
        } else {
            generateRandomPassword(
                length = config.length,
                includeUpper = config.includeUpper,
                includeLower = config.includeLower,
                includeNumbers = config.includeNumbers,
                includeSymbols = config.includeSymbols,
                excludeAmbiguous = config.excludeAmbiguous
            )
        }
    }

    fun generatePassphrase(
        wordCount: Int = 3,
        includeNumber: Boolean = true,
        separator: String = "-",
        capitalize: Boolean = true
    ): String {
        val selectedWords = mutableListOf<String>()
        val available = FRIENDLY_WORDS.toMutableList()
        repeat(wordCount.coerceIn(2, 6)) {
            if (available.isEmpty()) available.addAll(FRIENDLY_WORDS)
            val index = Random.nextInt(available.size)
            val word = available.removeAt(index)
            selectedWords.add(if (capitalize) word else word.lowercase())
        }
        val base = selectedWords.joinToString(separator)
        return if (includeNumber) {
            val num = Random.nextInt(11, 99)
            "$base$separator$num"
        } else {
            base
        }
    }

    fun generateRandomPassword(
        length: Int = 14,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true,
        excludeAmbiguous: Boolean = true
    ): String {
        val upperSet = if (excludeAmbiguous) CLEAN_UPPER else ALL_UPPER
        val lowerSet = if (excludeAmbiguous) CLEAN_LOWER else ALL_LOWER
        val numSet = if (excludeAmbiguous) CLEAN_NUMBERS else ALL_NUMBERS
        val symSet = if (excludeAmbiguous) CLEAN_SYMBOLS else ALL_SYMBOLS

        val requiredChars = mutableListOf<Char>()
        val pool = StringBuilder()

        if (includeUpper) {
            pool.append(upperSet)
            requiredChars.add(upperSet[Random.nextInt(upperSet.length)])
        }
        if (includeLower) {
            pool.append(lowerSet)
            requiredChars.add(lowerSet[Random.nextInt(lowerSet.length)])
        }
        if (includeNumbers) {
            pool.append(numSet)
            requiredChars.add(numSet[Random.nextInt(numSet.length)])
        }
        if (includeSymbols) {
            pool.append(symSet)
            requiredChars.add(symSet[Random.nextInt(symSet.length)])
        }

        if (pool.isEmpty()) {
            return generatePassphrase(3)
        }

        val poolStr = pool.toString()
        val targetLength = length.coerceIn(6, 40)
        val result = mutableListOf<Char>()
        result.addAll(requiredChars)

        while (result.size < targetLength) {
            result.add(poolStr[Random.nextInt(poolStr.length)])
        }

        // Shuffle characters so guaranteed inclusions aren't always at the beginning
        result.shuffle()
        return result.take(targetLength).joinToString("")
    }

    enum class StrengthLevel(val label: String, val score: Int) {
        WEAK("Needs Improvement", 1),
        FAIR("Fair", 2),
        GOOD("Good & Safe", 3),
        EXCELLENT("Very Strong & Safe", 4)
    }

    data class PasswordStrength(
        val level: StrengthLevel,
        val advice: String
    )

    fun evaluateStrength(password: String): PasswordStrength {
        if (password.length < 6) {
            return PasswordStrength(StrengthLevel.WEAK, "Too short. Try using at least 8 to 12 characters.")
        }

        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        if (password.contains("-") && password.length >= 14) score += 2

        return when {
            score >= 4 -> PasswordStrength(
                StrengthLevel.EXCELLENT,
                "Excellent! This is very difficult for computer hackers or scammers to guess."
            )
            score == 3 -> PasswordStrength(
                StrengthLevel.GOOD,
                "Good and safe for everyday websites."
            )
            score == 2 -> PasswordStrength(
                StrengthLevel.FAIR,
                "Fair strength. Consider adding another word, number, or symbol."
            )
            else -> PasswordStrength(
                StrengthLevel.WEAK,
                "This password is a bit weak. Tap 'Generate Safe Password' for a stress-free suggestion."
            )
        }
    }
}
