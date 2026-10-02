package com.example.util

import com.example.data.PasswordCategory
import com.example.data.PasswordEntity
import java.io.BufferedReader
import java.io.StringReader

data class CsvPasswordItem(
    val title: String,
    val category: PasswordCategory,
    val username: String,
    val plainPassword: String,
    val websiteUrl: String = "",
    val notes: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = ""
)

data class CsvImportResult(
    val items: List<CsvPasswordItem>,
    val totalRowsParsed: Int,
    val skippedCount: Int,
    val notes: List<String>
)

object CsvHelper {

    fun exportToCsv(
        passwords: List<PasswordEntity>,
        decryptPassword: (PasswordEntity) -> String
    ): String {
        val sb = StringBuilder()
        // Standard universal CSV header
        sb.append("Title,Category,Username,Password,Website,Notes,SecurityQuestion,SecurityAnswer\r\n")

        for (item in passwords) {
            val decrypted = decryptPassword(item)
            val row = listOf(
                escapeCsv(item.title),
                escapeCsv(item.getCategoryEnum().displayName),
                escapeCsv(item.username),
                escapeCsv(decrypted),
                escapeCsv(item.websiteUrl),
                escapeCsv(item.notes),
                escapeCsv(item.securityQuestion),
                escapeCsv(item.securityAnswer)
            )
            sb.append(row.joinToString(",")).append("\r\n")
        }

        return sb.toString()
    }

    fun parseCsv(csvContent: String): CsvImportResult {
        val lines = splitCsvLines(csvContent)
        if (lines.isEmpty()) {
            return CsvImportResult(emptyList(), 0, 0, listOf("The provided CSV content is empty."))
        }

        // Header mapping
        val headerTokens = parseCsvRow(lines[0]).map { it.trim().lowercase() }
        var titleCol = -1
        var usernameCol = -1
        var passwordCol = -1
        var urlCol = -1
        var notesCol = -1
        var categoryCol = -1
        var secQCol = -1
        var secACol = -1

        for ((idx, col) in headerTokens.withIndex()) {
            when {
                col in listOf("title", "name", "account", "service", "item") -> titleCol = idx
                col in listOf("username", "login", "email", "user", "login_username") -> usernameCol = idx
                col in listOf("password", "pass", "login_password", "secret") -> passwordCol = idx
                col in listOf("url", "website", "uri", "login_uri", "link") -> urlCol = idx
                col in listOf("notes", "note", "memo", "comments") -> notesCol = idx
                col in listOf("category", "folder", "group", "type") -> categoryCol = idx
                col in listOf("securityquestion", "security_question", "question") -> secQCol = idx
                col in listOf("securityanswer", "security_answer", "answer") -> secACol = idx
            }
        }

        // Fallback default index positions if no recognized headers
        if (titleCol == -1 && passwordCol == -1) {
            titleCol = 0
            if (headerTokens.size > 1) usernameCol = 1
            if (headerTokens.size > 2) passwordCol = 2
            if (headerTokens.size > 3) urlCol = 3
        }

        val parsedItems = mutableListOf<CsvPasswordItem>()
        var skipped = 0
        val notes = mutableListOf<String>()

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            val tokens = parseCsvRow(line)
            val title = if (titleCol in tokens.indices) tokens[titleCol].trim() else ""
            val password = if (passwordCol in tokens.indices) tokens[passwordCol].trim() else ""
            val username = if (usernameCol in tokens.indices) tokens[usernameCol].trim() else ""
            val url = if (urlCol in tokens.indices) tokens[urlCol].trim() else ""
            val note = if (notesCol in tokens.indices) tokens[notesCol].trim() else ""
            val categoryRaw = if (categoryCol in tokens.indices) tokens[categoryCol].trim() else ""
            val secQ = if (secQCol in tokens.indices) tokens[secQCol].trim() else ""
            val secA = if (secACol in tokens.indices) tokens[secACol].trim() else ""

            if (title.isBlank() && password.isBlank()) {
                skipped++
                continue
            }

            val finalTitle = title.ifBlank { url.ifBlank { "Imported Account #$i" } }
            val matchedCategory = inferCategory(categoryRaw, finalTitle, url)

            parsedItems.add(
                CsvPasswordItem(
                    title = finalTitle,
                    category = matchedCategory,
                    username = username,
                    plainPassword = password,
                    websiteUrl = url,
                    notes = note,
                    securityQuestion = secQ,
                    securityAnswer = secA
                )
            )
        }

        if (parsedItems.isEmpty()) {
            notes.add("No valid password entries could be recognized from the CSV.")
        } else {
            notes.add("Successfully found ${parsedItems.size} password account(s).")
        }

        return CsvImportResult(
            items = parsedItems,
            totalRowsParsed = lines.size - 1,
            skippedCount = skipped,
            notes = notes
        )
    }

    private fun inferCategory(categoryRaw: String, title: String, url: String): PasswordCategory {
        val combined = "$categoryRaw $title $url".lowercase()
        return when {
            combined.contains("bank") || combined.contains("chase") || combined.contains("wells") ||
                combined.contains("citi") || combined.contains("credit") || combined.contains("money") ||
                combined.contains("fidelity") || combined.contains("schwab") -> PasswordCategory.BANKING

            combined.contains("health") || combined.contains("doctor") || combined.contains("mychart") ||
                combined.contains("medicare") || combined.contains("clinic") || combined.contains("pharmacy") ||
                combined.contains("hospital") || combined.contains("walgreens") || combined.contains("cvs") -> PasswordCategory.HEALTHCARE

            combined.contains("mail") || combined.contains("gmail") || combined.contains("yahoo") ||
                combined.contains("outlook") || combined.contains("aol") -> PasswordCategory.EMAIL

            combined.contains("netflix") || combined.contains("hulu") || combined.contains("tv") ||
                combined.contains("disney") || combined.contains("youtube") || combined.contains("prime video") -> PasswordCategory.ENTERTAINMENT

            combined.contains("amazon") || combined.contains("shop") || combined.contains("ebay") ||
                combined.contains("walmart") || combined.contains("target") || combined.contains("costco") -> PasswordCategory.SHOPPING

            combined.contains("wifi") || combined.contains("wi-fi") || combined.contains("router") ||
                combined.contains("electric") || combined.contains("utility") || combined.contains("water") -> PasswordCategory.UTILITIES_HOME

            combined.contains("social security") || combined.contains("ssa") || combined.contains("pension") ||
                combined.contains("irs") || combined.contains("gov") -> PasswordCategory.GOVERNMENT

            else -> PasswordCategory.OTHER
        }
    }

    private fun escapeCsv(value: String): String {
        if (!value.contains(",") && !value.contains("\"") && !value.contains("\n") && !value.contains("\r")) {
            return value
        }
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun parseCsvRow(row: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < row.length) {
            val c = row[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < row.length && row[i + 1] == '\"') {
                        sb.append('\"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    tokens.add(sb.toString())
                    sb.clear()
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        tokens.add(sb.toString())
        return tokens
    }

    private fun splitCsvLines(content: String): List<String> {
        val lines = mutableListOf<String>()
        val reader = BufferedReader(StringReader(content))
        var line: String?
        val currentLine = StringBuilder()
        var insideQuotes = false

        while (reader.readLine().also { line = it } != null) {
            val l = line ?: break
            val quoteCount = l.count { it == '\"' }
            if (currentLine.isNotEmpty()) {
                currentLine.append("\n")
            }
            currentLine.append(l)

            if (quoteCount % 2 != 0) {
                insideQuotes = !insideQuotes
            }

            if (!insideQuotes) {
                lines.add(currentLine.toString())
                currentLine.clear()
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    fun getSampleCsv(): String {
        return """Title,Category,Username,Password,Website,Notes,SecurityQuestion,SecurityAnswer
Chase Checking,Banking,john.doe@email.com,Maple-River-42,https://chase.com,Account ending in 4102,First Pet,Barnaby
Medicare Portal,Healthcare,johndoe1948,Cedar-Breeze-88,https://medicare.gov,Check statement monthly,Birth City,Savannah
Personal Gmail,Email,johndoe@gmail.com,Sunny-Willow-51,https://mail.google.com,Primary family email,,
"""
    }
}
