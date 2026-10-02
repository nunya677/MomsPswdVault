package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PasswordCategory
import com.example.data.PasswordEntity
import com.example.security.CloudBackupManager
import com.example.security.CryptoManager
import com.example.security.SecurityPreferences
import com.example.util.CsvHelper
import com.example.util.PasswordGenerator
import com.example.util.PasswordStrengthAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GoldenKey", appName)
  }

  @Test
  fun `test password generator creates memorable passphrase`() {
    val passphrase = PasswordGenerator.generatePassphrase(3, true)
    assertTrue("Passphrase should contain hyphens", passphrase.contains("-"))
    val strength = PasswordGenerator.evaluateStrength(passphrase)
    assertTrue(strength.level == PasswordGenerator.StrengthLevel.EXCELLENT || strength.level == PasswordGenerator.StrengthLevel.GOOD)
  }

  @Test
  fun `test customizable password generator with length and inclusion options`() {
    // Test custom length 20
    val pass20 = PasswordGenerator.generateRandomPassword(
      length = 20,
      includeUpper = true,
      includeLower = true,
      includeNumbers = true,
      includeSymbols = true,
      excludeAmbiguous = true
    )
    assertEquals(20, pass20.length)
    assertTrue(pass20.any { it.isUpperCase() })
    assertTrue(pass20.any { it.isLowerCase() })
    assertTrue(pass20.any { it.isDigit() })
    // Ensure ambiguous characters (0, O, o, 1, l, I) are excluded
    assertFalse(pass20.contains('0'))
    assertFalse(pass20.contains('O'))
    assertFalse(pass20.contains('1'))
    assertFalse(pass20.contains('l'))
    assertFalse(pass20.contains('I'))

    // Test numbers only
    val numbersOnly = PasswordGenerator.generateRandomPassword(
      length = 10,
      includeUpper = false,
      includeLower = false,
      includeNumbers = true,
      includeSymbols = false,
      excludeAmbiguous = false
    )
    assertEquals(10, numbersOnly.length)
    assertTrue(numbersOnly.all { it.isDigit() })
  }

  @Test
  fun `test password strength analyzer detailed feedback`() {
    val weakAnalysis = PasswordStrengthAnalyzer.analyze("123456")
    assertEquals(PasswordStrengthAnalyzer.StrengthScore.VERY_WEAK, weakAnalysis.score)
    assertTrue(weakAnalysis.suggestions.isNotEmpty())

    val strongAnalysis = PasswordStrengthAnalyzer.analyze("Sunny-Maple-River-42")
    assertEquals(PasswordStrengthAnalyzer.StrengthScore.EXCELLENT, strongAnalysis.score)
    assertTrue(strongAnalysis.requirements.any { it.label.contains("hyphen") && it.isMet })
  }

  @Test
  fun `test pin verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = SecurityPreferences(context)
    prefs.setMasterPin("1234")
    assertTrue(prefs.verifyPin("1234"))
    assertFalse(prefs.verifyPin("9999"))
  }

  @Test
  fun `test csv export and import roundtrip`() {
    val samplePasswords = listOf(
      PasswordEntity(
        id = 1L,
        title = "Chase Bank",
        category = PasswordCategory.BANKING.name,
        username = "user@test.com",
        encryptedPassword = CryptoManager.encrypt("Secret123!"),
        websiteUrl = "https://chase.com",
        notes = "Savings account"
      )
    )

    val csv = CsvHelper.exportToCsv(samplePasswords) { CryptoManager.decrypt(it.encryptedPassword) }
    assertTrue(csv.contains("Chase Bank"))
    assertTrue(csv.contains("Secret123!"))

    val importResult = CsvHelper.parseCsv(csv)
    assertEquals(1, importResult.items.size)
    assertEquals("Chase Bank", importResult.items[0].title)
    assertEquals("Secret123!", importResult.items[0].plainPassword)
    assertEquals(PasswordCategory.BANKING, importResult.items[0].category)
  }

  @Test
  fun `test encrypted cloud backup and restore with recovery key`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val samplePasswords = listOf(
      PasswordEntity(
        id = 10L,
        title = "Medicare Portal",
        category = PasswordCategory.HEALTHCARE.name,
        username = "medicare_mary",
        encryptedPassword = CryptoManager.encrypt("Breeze-River-77"),
        websiteUrl = "https://medicare.gov"
      )
    )

    val backupInfo = CloudBackupManager.createCloudBackup(
      context = context,
      passwords = samplePasswords,
      decryptPassword = { CryptoManager.decrypt(it.encryptedPassword) }
    )

    assertNotNull(backupInfo.recoveryKey)
    assertTrue(backupInfo.recoveryKey.startsWith("GK-"))

    val restored = CloudBackupManager.restoreCloudBackup(
      backupJsonString = backupInfo.backupJson,
      recoveryKey = backupInfo.recoveryKey,
      secondaryCode = backupInfo.secondaryCode
    )

    assertEquals(1, restored.size)
    assertEquals("Medicare Portal", restored[0].title)
    val decryptedRestored = CryptoManager.decrypt(restored[0].encryptedPassword)
    assertEquals("Breeze-River-77", decryptedRestored)
  }
}
